package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.Collision
import korlibs.korge.fleks.components.Collision.Companion.CollisionComponent
import korlibs.korge.fleks.components.DebugCollisionShapes
import korlibs.korge.fleks.components.DebugCollisionShapes.Companion.DebugCollisionShapesComponent
import korlibs.korge.fleks.components.Gravity.Companion.GravityComponent
import korlibs.korge.fleks.components.Motion
import korlibs.korge.fleks.components.Motion.Companion.GROUND_WALKING
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.logic.collision.CollisionHandler
import korlibs.korge.fleks.logic.collision.CollisionResolver
import korlibs.korge.fleks.logic.collision.NO_COLLISION_SLOPE
import korlibs.math.isAlmostEquals
import korlibs.math.toIntCeil
import korlibs.math.toIntFloor
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

/**
 * Collision system for the game. This system is responsible for handling collision between an
 * entity and static level map cells.
 *
 * @see [https://github.com/korlibs/korge-fleks/wiki/Collision-System]
 */
open class CollisionMoveSystem : IteratingSystem(
    family = World.family { all(PositionComponent, MotionComponent, CollisionComponent)
        .any(PositionComponent, DebugCollisionShapesComponent) },
    interval = Fixed(1 / 60f)
) {
    private val worldMapData = world.inject<AssetStore>("AssetStore").worldMapData
    protected val collisionHandler = CollisionHandler(worldMapData)
    protected val collisionResolver = CollisionResolver()

    protected fun Double.isNotAlmostZero(): Boolean = this.absoluteValue > 0.01

    // Size of a tile grid cell in pixels (e.g. 16.0).
    private val gridSize = worldMapData.tileSize.toDouble()
    private val gridSizeHalf = gridSize * 0.5

    // Internal states during this frame
    private var wasGroundedLastFrame = false
    private var slopeValueLastFrame = NO_COLLISION_SLOPE

    override fun onTickEntity(entity: Entity) {
        // Iterate over all entities that have a Grid, Collision and MotionComponent
        val motionComponent = entity[MotionComponent]
        val positionComponent = entity[PositionComponent]
        val collisionComponent = entity[CollisionComponent]
        val collisionRect = collisionComponent.rect

        val debugShapesComponent: DebugCollisionShapes? = entity.getOrNull(DebugCollisionShapesComponent)
        // Free debug points before we create new ones
        debugShapesComponent?.cleanup()

        val gravityComponent = entity.getOrNull(GravityComponent)
        // Apply gravity to the entity if it has a GravityComponent
        if (gravityComponent != null) {
            motionComponent.velocityY += gravityComponent.calculateDeltaGravity() * deltaTime
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Step 1: Integrated Frame Displacement Calculation
        //
        // Total frame displacement is calculated from velocity and frame time (deltaTime)
        val xVelocity = motionComponent.velocityX * motionComponent.damping
        val overallMovementX = xVelocity * deltaTime
        val overallMovementY = motionComponent.velocityY * deltaTime  // Negative sign for upward movement - Y Axis is mirrored in the game

        // The number of steps which an object travels through a grid cell is defined either by its collision
        // rect size or the grid size, i.e. if the collision rect is smaller than the grid size we need to
        // check "more often" within a cell otherwise it is enough to check once per grid cell
        val stepSize = min(min(collisionRect.width, collisionRect.height), gridSize)

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Step 2: Movement Sub-Stepping (getCollisionSteps & processCollisionSteps)
        //
        // Check if movement in X or Y direction is greater than the grid size and set the number of
        // steps accordingly. If we have at least a small movement there will be at least one step executed.
        val steps: Int = getCollisionSteps(overallMovementX, overallMovementY, stepSize)

        if (steps > 0) {
            // Reset collision flags if the entity is moving
            collisionComponent.right = false
            collisionComponent.left = false
            wasGroundedLastFrame = collisionComponent.isGrounded
            collisionComponent.isGrounded = false
            collisionComponent.isCollidingAbove = false
            slopeValueLastFrame = collisionComponent.slopeValue
        }

        processCollisionSteps(steps, overallMovementX, overallMovementY) { movementX, movementY ->
            checkCollision(movementX, movementY, positionComponent, motionComponent, collisionComponent, debugShapesComponent)
        }

        // Calculate target damping factor depending on slope movement direction
        val isOnSlope = collisionComponent.slopeValue != NO_COLLISION_SLOPE
        val wasOnSlopeLastFrame = slopeValueLastFrame != NO_COLLISION_SLOPE
        val currentSlopeValue = collisionComponent.slopeValue
        val targetDamping = if (isOnSlope && wasOnSlopeLastFrame) {
            if (currentSlopeValue > slopeValueLastFrame) 0.5       // Running UP a slope -> decrease speed
            else if (currentSlopeValue < slopeValueLastFrame) 1.5  // Running DOWN a slope -> increase speed
            else 1.0
        } else {
            1.0  // Flat ground or in air -> normal speed
        }

        // Smoothly adjust damping towards targetDamping
        val lerpRate = (10.0 * deltaTime).coerceAtMost(1.0)
        motionComponent.damping += (targetDamping - motionComponent.damping) * lerpRate

        if (motionComponent.damping.isAlmostEquals(1.0, 0.0001)) {
            // Damping values with distance < 0.0001 are rounded to 1.0
            motionComponent.damping = 1.0
        }
        //if (motionComponent.damping != 1.0) println("Damping: ${motionComponent.damping}")

        if (motionComponent.velocityX.isAlmostEquals(0.0005)) {
            // Velocity values smaller than 0.0005 are zeroed out
            motionComponent.velocityX = 0.0
        }

        if (motionComponent.velocityY.isAlmostEquals(0.0005)) {
            // Velocity values smaller than 0.0005 are zeroed out
            motionComponent.velocityY = 0.0
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Step 7: Playfield Clamping
        //
        // Keep entity positions strictly within level boundaries (with 16px guard margin).
        checkPlayfieldBoundaries(positionComponent)
    }

    /**
     * Orchestrates two-phase horizontal and vertical probing and resolution for a single sub-step displacement.
     *
     * Probes horizontal movement first and resolves horizontal position snapping if colliding, or applies [xMovement] directly.
     * Then probes vertical movement using updated positions and resolves vertical position snapping if colliding.
     * If non-walking surface reactions are configured, partial displacement ratio is calculated and applied to the non-colliding axis.
     *
     * @param xMovement Horizontal movement delta for the sub-step in pixels.
     * @param yMovement Vertical movement delta for the sub-step in pixels.
     * @param positionComponent The position component containing entity world coordinates and offsets.
     * @param motionComponent The motion component containing velocities and surface reaction modes.
     * @param collisionComponent The collision component containing the bounding box rect and collision flags.
     * @param debugShapesComponent Optional component for rendering debug collision probe points and cells.
     * @return `true` if either a horizontal or vertical collision was detected and resolved; `false` otherwise.
     */
    protected fun checkCollision(
        xMovement: Double,
        yMovement: Double,
        positionComponent: Position,
        motionComponent: Motion,
        collisionComponent: Collision,
        debugShapesComponent: DebugCollisionShapes?
    ): Boolean {
        val xPositionSaved = positionComponent.x

        // Set collision rect size with a small guard distance
        val collisionRectSizeGuard = 0.0  //0.001
        val collisionRectWidthGuarded = collisionComponent.rect.width - collisionRectSizeGuard
        val collisionRectHeightGuarded = collisionComponent.rect.height - collisionRectSizeGuard

        // Define initial (not moved) position of the entity collision box edges
        val xPosInitialLeft = positionComponent.x + positionComponent.offsetX + collisionComponent.rect.x
        val xPosInitialRight = xPosInitialLeft + collisionComponent.rect.width
        var yPosInitialTop = positionComponent.y + positionComponent.offsetY + collisionComponent.rect.y
        var yPosInitialBottom = yPosInitialTop + collisionComponent.rect.height

        // Initial position of the leading edge of the collision box
        val xPosInitialLeadingEdge = if (xMovement > 0.0) xPosInitialRight else xPosInitialLeft

        // Span lengths along active edges (guarded to stay strictly within collider bounds)
        val horizontalSpan = max(0.0, collisionRectWidthGuarded)
        val verticalSpan = max(0.0, collisionRectHeightGuarded)

        val horizontalSteps = (horizontalSpan / gridSize).toIntCeil()
        val verticalSteps = (verticalSpan / gridSize).toIntCeil()

        var isCollidingHor: Boolean
        var isCollidingVer: Boolean
        val ySlopeValue: Double  // [0.0 ... <16.0]  --> 0.0 = no collision --> 0.01 ... 15.99 = inside a slope tile

        // Define position of moved collision box edges
        val xPosMoveProbeLeft = xPosInitialLeft + xMovement
        val xPosMoveProbeRight = xPosMoveProbeLeft + collisionRectWidthGuarded
        // Leading collision box edge coordinates for probing
        val xPosMovedLeadingEdge = if (xMovement > 0.0) xPosMoveProbeRight else xPosMoveProbeLeft

        // First check if we are running into a slope tile --> move up the object by the slope value
        ySlopeValue = collisionHandler.probeHorizontalSlopes(
            yMovement,
            xPosMoveProbeLeft,  // probe at the moved X position
            xPosMoveProbeRight,
            yPosInitialTop,     // probe at the not yet moved Y position
            yPosInitialBottom,
            debugShapesComponent
        )

        collisionComponent.slopeValue = ySlopeValue

        // Check if we were on a slope last frame, are not moving up and the current cell is no slope
        // then we need to ground the object otherwise at the end of a slope down we will fall
        // or at the end of a slope up we will hit a wall
        val wasOnSlopeLastFrame = slopeValueLastFrame != NO_COLLISION_SLOPE
        if (wasOnSlopeLastFrame && yMovement > 0.0 && ySlopeValue == NO_COLLISION_SLOPE) {  // Y-Axis inverted!

            // Sample the slope value at position of the last frame (initial position)
            val yr = collisionHandler.probeHorizontalSlopes(
                yMovement,
                xPosInitialLeft,
                xPosInitialRight,
                yPosInitialTop,
                yPosInitialBottom
            )

            // running up slop -> move collision rect up to cell bounds (current cell 0)
            // running down slope -> move collision rect down to cell bounds (next cell +1)
            val cyBottomEdge = (yPosInitialBottom / gridSize).toIntFloor() + if (yr > gridSizeHalf) 0 else 1

            // Move collision rect to the top of the collider cell
            val guardMove = 0.01
            yPosInitialBottom = cyBottomEdge * gridSize - guardMove  // Y-Axis inverted!
            yPosInitialTop = yPosInitialBottom - collisionComponent.rect.height
        }

        // Probe horizontally
        isCollidingHor = collisionHandler.probeCollisionHorizontally(
            yPosInitialTop,
            yPosInitialBottom,
            xPosMovedLeadingEdge,
            verticalSteps,
            ySlopeValue,
            debugShapesComponent
        )

        // Set horizontal position depending on collision - here we detect slopes when running up
        if (isCollidingHor) {  // resolve only at solid collision
            collisionResolver.resolveCollisionHorizontally(
                xMovement,
                xPosMovedLeadingEdge,
                positionComponent,
                motionComponent,
                collisionComponent,
                gridSize
            )
        } else {  // Not colliding with solid tile horizontally
            positionComponent.x += xMovement
        }

        // Define position of moved collision box edges
        val yPosMovedTop = yPosInitialTop + yMovement
        val yPosMovedBottom = yPosMovedTop + collisionRectHeightGuarded

        // Leading collision box edge coordinates for probing
        val yPosMovedLeadingEdge = if (yMovement > 0.0) yPosMovedBottom else yPosMovedTop

        val xPosMovedLeft = positionComponent.x + positionComponent.offsetX + collisionComponent.rect.x
        val xPosMovedRight = xPosMovedLeft + collisionComponent.rect.width

        // Subtract the slope value from the probe position - we check collision against solid tiles
        // If there is no slope (== 0.0) it has no influence, but if we are already on a slope, we
        // need to remove the slope offset and do the collision check "normally" and add the slope value back
        val yPosMovedProbe = yPosMovedLeadingEdge + ySlopeValue  // Y-Axis inverted!

        // Now probe vertically
        isCollidingVer = collisionHandler.probeCollisionVertically(
            xPosMovedLeft,
            xPosMovedRight,
            yPosMovedProbe,
            horizontalSteps,
            debugShapesComponent
        )

        val slopeBeneath = collisionHandler.probeSlopeBeneath(
            xPosMovedLeft,
            xPosMovedRight,
            yPosMovedProbe,
            debugShapesComponent
        )

        // Vertically first check if we are on a slope
        if (slopeBeneath != NO_COLLISION_SLOPE) {
            collisionResolver.resolveCollisionVertically(
                yMovement,
                yPosMovedProbe + 16.0,
                positionComponent,
                motionComponent,
                collisionComponent,
                gridSize,
                slopeBeneath
            )
        } else if (isCollidingVer) {
            collisionResolver.resolveCollisionVertically(
                yMovement,
                yPosMovedProbe,
                positionComponent,
                motionComponent,
                collisionComponent,
                gridSize,
                ySlopeValue
            )

            // Apply friction to the horizontal movement - possibly correct the horizontal movement with only a fraction of the total movement based on frictionX
            if (motionComponent.surfaceReactionX != GROUND_WALKING) {
                val yPosInitialLeadingEdge = if (yMovement > 0.0) yPosInitialBottom else yPosInitialTop
                val yRatio = calculateRatio(yMovement, yPosInitialLeadingEdge, yPosMovedLeadingEdge, gridSize)
                val xDeltaMove = xMovement * yRatio
                positionComponent.x = xPositionSaved + xDeltaMove
            }

        } else {  // No collision vertically
            if (isCollidingHor) {
                if (motionComponent.surfaceReactionY == GROUND_WALKING) {
                    positionComponent.y += yMovement
                } else {
                    // Apply friction to the horizontal movement - Ratio to the left edge of the collider cell
                    val xRatio = calculateRatio(xMovement, xPosInitialLeadingEdge, xPosMovedLeadingEdge, gridSize)
                    val yDeltaMove = yMovement * xRatio
                    positionComponent.y += yDeltaMove
                }
            } else {  // No collision horizontally - we don't need to resolve Y
                positionComponent.y += yMovement
            }
        }

        return isCollidingHor || isCollidingVer
    }

    /**
     * Calculates the fractional entry ratio ($ratio \in [0..1]$) traversed along an axis before the leading edge hits a target cell boundary.
     *
     * Used to compute partial movement deltas for non-colliding axes when an impact occurs mid-step.
     *
     * @param movement Displacement delta in pixels along the axis.
     * @param posInitialLeadingEdge Unmoved coordinate of the leading collision box edge.
     * @param posMovedLeadingEdge Target coordinate of the leading edge after sub-step movement.
     * @param gridSize Size of a tile grid cell in pixels.
     * @return Fractional ratio in $[0..1]$ representing progress before impact, or `0.0` if movement is zero.
     */
    protected fun calculateRatio(
        movement: Double,
        posInitialLeadingEdge: Double,
        posMovedLeadingEdge: Double,
        gridSize: Double
    ): Double {
        val cellMovedLeadingEdge = (posMovedLeadingEdge / gridSize).toIntFloor()
        return if (movement > 0.0) {  // Move to right side
            val xDistance = cellMovedLeadingEdge * gridSize - posInitialLeadingEdge
            if (movement.isNotAlmostZero()) xDistance / movement else 0.0
        } else {  // Move to left side
            val xDistance = (cellMovedLeadingEdge + 1) * gridSize - posInitialLeadingEdge
            if (movement.isNotAlmostZero()) xDistance / movement else 0.0
        }
    }

    /**
     * Enforces level boundaries by clamping the entity's position strictly within map bounds
     * with a guard margin (`guardBorder = worldMapData.tileSize.toDouble()`).
     *
     * @param positionComponent The position component of the entity to clamp.
     */
    protected fun checkPlayfieldBoundaries(positionComponent: Position) {
        // Keep the player sprite inside the level
        val guardBorder = worldMapData.tileSize.toDouble()
        // Check left and right boundaries with a guard border to prevent the playfield to render outside the visible area, e.g. if the camera "shakes" on explosions etc.
        if (positionComponent.x + positionComponent.offsetX <= guardBorder) positionComponent.x = guardBorder - positionComponent.offsetX
        else if (positionComponent.x + positionComponent.offsetX >= worldMapData.worldWidth - guardBorder) positionComponent.x = worldMapData.worldWidth - guardBorder - positionComponent.offsetX

        // Check if player falls off the playfield at the bottom
        if (positionComponent.y + positionComponent.offsetY > worldMapData.worldHeight - guardBorder) {
            // TODO: handle death of object
            positionComponent.y = worldMapData.worldHeight - guardBorder - positionComponent.offsetY  // Move the player back to the bottom boundary (can be removed when death is implemented)
            //println("ERROR: PositionSystem - Player falls off the playfield!")
        }
    }

    /**
     * Calculates the number of sub-step iterations required for frame displacement to prevent tunneling through tiles.
     *
     * Computes $\lceil \max(|dx|, |dy|) / \text{gridSize} \rceil$ clamped to a maximum of 100 steps.
     *
     * @param horizontalMovement Total horizontal frame displacement in pixels.
     * @param verticalMovement Total vertical frame displacement in pixels.
     * @param gridSize Size of a sub-step or tile grid cell in pixels.
     * @return The integer count of sub-steps required for collision checking.
     */
    protected fun getCollisionSteps(horizontalMovement: Double, verticalMovement: Double, gridSize: Double): Int {
        if (gridSize < 1.0 && gridSize > -1.0) {
            println("ERROR: Step denominator is very small (${gridSize})!")
            return 0
        }
        val steps = (max(abs(horizontalMovement), abs(verticalMovement)) / gridSize).toIntCeil()
        if (steps > 100) {
            println("ERROR: Steps number is very high (${steps})!")
            return 100
        }
        return steps
    }

    /**
     * Divides total displacement into uniform sub-step slices and passes each sub-step delta to the callback.
     *
     * Halts sub-stepping loop early if the callback returns `true` (e.g., when an entity is destroyed on impact).
     *
     * @param steps The number of sub-steps to process.
     * @param overallMovementX The total horizontal displacement for the frame.
     * @param overallMovementY The total vertical displacement for the frame.
     * @param callback Function called for each sub-step taking (`movementX`, `movementY`) and returning `true` to halt further steps.
     * @see getCollisionSteps
     */
    protected fun processCollisionSteps(
        steps: Int,
        overallMovementX: Double,
        overallMovementY: Double,
        callback: (movementX: Double, movementY: Double) -> Boolean
    ) {
        if (steps > 0) {
            // Init movement and take into account that overall movement can be negative
            val movementX: Double = overallMovementX / steps
            val movementY: Double = overallMovementY / steps
            var i = 0

            while (i < steps) {
                val result = callback(movementX, movementY)
                if (result) return
                i++
            }
        }
        return
    }
}
