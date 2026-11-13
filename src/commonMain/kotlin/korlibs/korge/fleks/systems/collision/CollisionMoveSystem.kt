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
import korlibs.korge.fleks.components.Motion.Companion.EXPLOSION_SLIDING
import korlibs.korge.fleks.components.Motion.Companion.BOUNCE
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.utils.CollisionUtils.debugSaveGridCell
import korlibs.korge.fleks.utils.CollisionUtils.debugSaveRatioPointX
import korlibs.korge.fleks.utils.CollisionUtils.debugSaveRatioPointY
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

    // Check for divider of movement in X and Y direction
    private val guardMove = 0.001f  // TODO check if we can keep this zero
    private val guard = 0.01f

    /**
     * Checks if a floating point value exceeds the deadzone threshold (`guard = 0.01f`).
     *
     * @return `true` if the absolute value is greater than `0.01f`, `false` otherwise.
     */
    protected fun Float.isNotAlmostZero(): Boolean = this.absoluteValue > guard

    /**
     * Iterates over all entities with [PositionComponent], [MotionComponent], and [CollisionComponent] to calculate frame displacement,
     * apply gravity, execute sub-sampled collision checks against level geometry, and clamp positions within map boundaries.
     *
     * @param entity The entity currently being processed by the system.
     */
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
        val overallMovementX = motionComponent.velocityX * deltaTime
        val overallMovementY = motionComponent.velocityY * deltaTime  // Negative sign for upward movement - Y Axis is mirrored in the game
        val gridSize = worldMapData.tileSize.toFloat()

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
            collisionComponent.isGrounded = false
            collisionComponent.isCollidingAbove = false
        }

        processCollisionSteps(steps, overallMovementX, overallMovementY) { movementX, movementY ->
            checkCollision(movementX, movementY, positionComponent, motionComponent, collisionComponent, gridSize, debugShapesComponent)
        }

        if (motionComponent.velocityX.isAlmostEquals(0.0005f)) {
            // Velocity values smaller than 0.0005f are zeroed out
            motionComponent.velocityX = 0f
        }

        if (motionComponent.velocityY.isAlmostEquals(0.0005f)) {
            // Velocity values smaller than 0.0005f are zeroed out
            motionComponent.velocityY = 0f
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
     * @param gridSize Size of a tile grid cell in pixels (e.g., 16.0f).
     * @param debugShapesComponent Optional component for rendering debug collision probe points and cells.
     * @return `true` if either a horizontal or vertical collision was detected and resolved; `false` otherwise.
     */
    protected fun checkCollision(
        xMovement: Float,
        yMovement: Float,
        positionComponent: Position,
        motionComponent: Motion,
        collisionComponent: Collision,
        gridSize: Float,
        debugShapesComponent: DebugCollisionShapes?
    ): Boolean {
        val xPositionSaved = positionComponent.x

        // Set collision rect size with a small guard distance
        val collisionRectSizeGuard = 0f  //0.001f
        val collisionRectWidthGuarded = collisionComponent.rect.width - collisionRectSizeGuard
        val collisionRectHeightGuarded = collisionComponent.rect.height - collisionRectSizeGuard

        // Define initial (not moved) position of the entity collision box edges
        val xPosInitialLeft = positionComponent.x + positionComponent.offsetX + collisionComponent.rect.x
        val xPosInitialRight = xPosInitialLeft + collisionComponent.rect.width
        val yPosInitialTop = positionComponent.y + positionComponent.offsetY + collisionComponent.rect.y
        val yPosInitialBottom = yPosInitialTop + collisionComponent.rect.height

        // Initial position of the leading edge of the collision box
        val xPosInitialLeadingEdge = if (xMovement > 0f) xPosInitialRight else xPosInitialLeft

        // Span lengths along active edges (guarded to stay strictly within collider bounds)
        val horizontalSpan = max(0f, collisionRectWidthGuarded)
        val verticalSpan = max(0f, collisionRectHeightGuarded)

        val horizontalSteps = (horizontalSpan / gridSize).toIntCeil()
        val verticalSteps = (verticalSpan / gridSize).toIntCeil()

        var isCollidingHor: Boolean
        var isCollidingVer: Boolean

        // Define position of moved collision box edges
        val xPosMoveProbeLeft = xPosInitialLeft + xMovement
        val xPosMoveProbeRight = xPosMoveProbeLeft + collisionRectWidthGuarded
        // Leading collision box edge coordinates for probing
        val xPosMovedLeadingEdge = if (xMovement > 0f) xPosMoveProbeRight else xPosMoveProbeLeft

        // Probe first horizontally and then vertically
        isCollidingHor = probeCollisionHorizontally(xMovement, yMovement, yPosInitialTop, yPosInitialBottom, xPosMovedLeadingEdge, verticalSteps, gridSize, debugShapesComponent)
        // Set horizontal position depending on collision
        if (isCollidingHor) {
            resolveCollisionHorizontally(xMovement, xPosMovedLeadingEdge, positionComponent, motionComponent, collisionComponent, gridSize)
        } else {  // No collision horizontally - we don't need to resolve
            positionComponent.x += xMovement
        }

        // Define position of moved collision box edges
        val yPosMovedTop = yPosInitialTop + yMovement
        val yPosMovedBottom = yPosMovedTop + collisionRectHeightGuarded
        // Leading collision box edge coordinates for probing
        val yPosMovedLeadingEdge = if (yMovement > 0f) yPosMovedBottom else yPosMovedTop

        val xPosMovedLeft = positionComponent.x + positionComponent.offsetX + collisionComponent.rect.x
        val xPosMovedRight = xPosMovedLeft + collisionComponent.rect.width

        // Now probe vertically
        isCollidingVer = probeCollisionVertically(xMovement, yMovement, xPosMovedLeft, xPosMovedRight, yPosMovedLeadingEdge, horizontalSteps, gridSize, debugShapesComponent)
        if (isCollidingVer) {
            resolveCollisionVertically(yMovement, yPosMovedLeadingEdge, positionComponent, motionComponent, collisionComponent, gridSize)

            // Apply friction to the horizontal movement - possibly correct the horizontal movement with only a fraction of the total movement based on frictionX
            if (motionComponent.surfaceReactionX != GROUND_WALKING) {
                val yPosInitialLeadingEdge = if (yMovement > 0f) yPosInitialBottom else yPosInitialTop
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
            } else {  // No collision horizontally - we don't need to resolve
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
     * @return Fractional ratio in $[0..1]$ representing progress before impact, or `0f` if movement is zero.
     */
    protected fun calculateRatio(
        movement: Float,
        posInitialLeadingEdge: Float,
        posMovedLeadingEdge: Float,
        gridSize: Float
    ): Float {
        val cellMovedLeadingEdge = (posMovedLeadingEdge / gridSize).toIntFloor()
        return if (movement > 0f) {  // Move to right side
            val xDistance = cellMovedLeadingEdge * gridSize - posInitialLeadingEdge
            if (movement.isNotAlmostZero()) xDistance / movement else 0f
        } else {  // Move to left side
            val xDistance = (cellMovedLeadingEdge + 1) * gridSize - posInitialLeadingEdge
            if (movement.isNotAlmostZero()) xDistance / movement else 0f
        }
    }

    /**
     * Probes the vertical leading edge of the moved collision box across [verticalSteps] sample points against static level geometry.
     *
     * Samples along the $Y$-axis from top-to-bottom (when moving down or stationary) or bottom-to-top (when moving up)
     * to check if any probed point intersects a solid tile cell in `worldMapData`.
     *
     * @param xMovement Horizontal movement delta for the sub-step in pixels.
     * @param yMovement Vertical movement delta for the sub-step in pixels.
     * @param yPosTop Top boundary coordinate of the collision box.
     * @param yPosBottom Bottom boundary coordinate of the collision box.
     * @param xPosMovedLeadingEdge Target $X$-coordinate of the leading vertical edge after horizontal movement.
     * @param verticalSteps Number of vertical sub-step sample points along the leading edge.
     * @param gridSize Size of a tile grid cell in pixels.
     * @param debugShapesComponent Optional component for visual debugging of probe points and checked grid cells.
     * @return `true` if any probed point intersects a solid level tile; `false` otherwise.
     */
    protected fun probeCollisionHorizontally(
        xMovement: Float,
        yMovement: Float,
        yPosTop: Float,
        yPosBottom: Float,
        xPosMovedLeadingEdge: Float,
        verticalSteps: Int,
        gridSize: Float,
        debugShapesComponent: DebugCollisionShapes?
    ): Boolean {
        var isCollidingHor = false
        for (i in 0..verticalSteps) {
            // Horizontal movement probe: sample y along the vertical leading edge (xLeadingEdge)
            isCollidingHor = if (!isCollidingHor && xMovement.isNotAlmostZero()) {
                val yPosMovedProbe = if (yMovement < 0f) {  // Moving up - Y Axis inverted
                    val xPosMovedProbeFromBottom = yPosBottom - i * gridSize
                    max(xPosMovedProbeFromBottom, yPosTop)
                } else {  // Moving down - Y Axis inverted
                    val xPosMovedProbeFromTop = yPosTop + i * gridSize
                    min(xPosMovedProbeFromTop, yPosBottom)
                }
                // Calculate the cell index and probe the cell for collision
                val cxProbe = (xPosMovedLeadingEdge / gridSize).toIntFloor()
                val cyProbe = (yPosMovedProbe / gridSize).toIntFloor()
                debugShapesComponent?.let { debugSaveRatioPointX(it, xPosMovedLeadingEdge, yPosMovedProbe) }
                debugShapesComponent?.let { debugSaveGridCell(it, cxProbe, cyProbe, worldMapData) }
                worldMapData.hasCollision(cxProbe, cyProbe)
            } else isCollidingHor
        }
        return isCollidingHor
    }

    /**
     * Probes the horizontal leading edge of the moved collision box across [horizontalSteps] sample points against static level geometry.
     *
     * Samples along the $X$-axis from left-to-right (when moving right or stationary) or right-to-left (when moving left)
     * to check if any probed point intersects a solid tile cell in `worldMapData`.
     *
     * @param xMovement Horizontal movement delta for the sub-step in pixels.
     * @param yMovement Vertical movement delta for the sub-step in pixels.
     * @param xPosMovedLeft Left boundary $X$-coordinate of the moved collision box.
     * @param xPosMovedRight Right boundary $X$-coordinate of the moved collision box.
     * @param yPosMovedLeadingEdge Target $Y$-coordinate of the leading horizontal edge after vertical movement.
     * @param horizontalSteps Number of horizontal sub-step sample points along the leading edge.
     * @param gridSize Size of a tile grid cell in pixels.
     * @param debugShapesComponent Optional component for visual debugging of probe points and checked grid cells.
     * @return `true` if any probed point intersects a solid level tile; `false` otherwise.
     */
    protected fun probeCollisionVertically(
        xMovement: Float,
        yMovement: Float,
        xPosMovedLeft: Float,
        xPosMovedRight: Float,
        yPosMovedLeadingEdge: Float,
        horizontalSteps: Int,
        gridSize: Float,
        debugShapesComponent: DebugCollisionShapes?
    ): Boolean {
        var isCollidingVer = false
        for (i in 0..horizontalSteps) {
            // Vertical movement probe: sample x along the horizontal leading edge (yLeadingEdge)
            // e.g. when the player is standing on a platform --> we collide here
            isCollidingVer = if (!isCollidingVer && yMovement.isNotAlmostZero()) {
                val xPosMovedProbe = if (xMovement < 0f) {  // Move to left side
                    val xPosMovedProbeFromRight = xPosMovedRight - i * gridSize
                    max(xPosMovedProbeFromRight, xPosMovedLeft)
                } else {  // Move to right side
                    val xPosMovedProbeFromLeft = xPosMovedLeft + i * gridSize
                    min(xPosMovedProbeFromLeft, xPosMovedRight)
                }
                // Calculate the cell index and probe the cell for collision
                val cxProbe = (xPosMovedProbe / gridSize).toIntFloor()
                val cyProbe = (yPosMovedLeadingEdge / gridSize).toIntFloor()
                debugShapesComponent?.let { debugSaveRatioPointY(it, xPosMovedProbe, yPosMovedLeadingEdge) }
                debugShapesComponent?.let { debugSaveGridCell(it, cxProbe, cyProbe, worldMapData) }
                worldMapData.hasCollision(cxProbe, cyProbe)
            } else isCollidingVer
        }
        return isCollidingVer
    }

    /**
     * Resolves a horizontal collision by adjusting horizontal velocity based on [Motion.surfaceReactionX],
     * snapping the entity's $X$-position outside the colliding tile cell, and updating collision flags (`right` or `left`).
     *
     * Surface reaction modes:
     * - [GROUND_WALKING]: Sets `velocityX = 0f` (retains vertical velocity).
     * - [EXPLOSION_SLIDING]: Dampens `velocityX *= 0.07f` and `velocityY *= 0.1f` to slide along/into walls.
     * - [BOUNCE]: Inverts velocity `velocityX *= -1f`.
     *
     * @param xMovement Horizontal movement delta for the sub-step in pixels.
     * @param xPosMovedLeadingEdge Target $X$-coordinate of the leading vertical edge after horizontal movement.
     * @param positionComponent The position component containing entity world coordinates.
     * @param motionComponent The motion component containing velocities and surface reaction modes.
     * @param collisionComponent The collision component containing rect dimensions and collision flags.
     * @param gridSize Size of a tile grid cell in pixels.
     */
    protected fun resolveCollisionHorizontally(
        xMovement: Float,
        xPosMovedLeadingEdge: Float,
        positionComponent: Position,
        motionComponent: Motion,
        collisionComponent: Collision,
        gridSize: Float
    ) {
        // Stop movement along the collision axis (X) or bounce off the surface
        when (motionComponent.surfaceReactionX) {
            GROUND_WALKING -> {
                motionComponent.velocityX = 0f
                // velocityY not changed
            }
            EXPLOSION_SLIDING -> {
                motionComponent.velocityX *= 0.07f  // Let the explosions move a bit into the wall
                motionComponent.velocityY *= 0.1f   // and slide faster on the wall
            }
            BOUNCE -> {
                motionComponent.velocityX *= -1f  // Bounce off the surface
            }
        }

        val cxMovedLeadingEdge = (xPosMovedLeadingEdge / gridSize).toIntFloor()

        // Resolve the collision - move the entity out of the collider cell horizontally and
        // move the entity vertically only by ratio of the horizontal movement
        val isMovingRight = xMovement > 0f
        if (isMovingRight) {
            // Movement in right direction
            val collisionRectOffsetX = collisionComponent.rect.width + collisionComponent.rect.x
            val cxCell = cxMovedLeadingEdge
            // New position left to the collider cell
            positionComponent.x = cxCell * gridSize - guardMove - collisionRectOffsetX
            collisionComponent.right = true
        } else {
            // Movement in left direction
            val collisionRectOffsetX = -collisionComponent.rect.x
            val cxCell = cxMovedLeadingEdge + 1
            // New position right to the collider cell
            positionComponent.x = cxCell * gridSize + guardMove + collisionRectOffsetX
            collisionComponent.left = true
        }
    }

    /**
     * Resolves a vertical collision by adjusting vertical velocity based on [Motion.surfaceReactionY],
     * snapping the entity's $Y$-position outside the colliding tile cell, and updating collision flags (`isGrounded` or `isCollidingAbove`).
     *
     * Surface reaction modes:
     * - [GROUND_WALKING]: Sets `velocityY = 0f` (retains horizontal velocity).
     * - [EXPLOSION_SLIDING]: Dampens `velocityX *= 0.1f` and `velocityY *= 0.07f`.
     * - [BOUNCE]: Inverts velocity `velocityY *= -1f`.
     *
     * @param yMovement Vertical movement delta for the sub-step in pixels.
     * @param yPosMovedLeadingEdge Target $Y$-coordinate of the leading horizontal edge after vertical movement.
     * @param positionComponent The position component containing entity world coordinates.
     * @param motionComponent The motion component containing velocities and surface reaction modes.
     * @param collisionComponent The collision component containing rect dimensions and collision flags.
     * @param gridSize Size of a tile grid cell in pixels.
     */
    protected fun resolveCollisionVertically(
        yMovement: Float,
        yPosMovedLeadingEdge: Float,
        positionComponent: Position,
        motionComponent: Motion,
        collisionComponent: Collision,
        gridSize: Float
    ) {
        // Stop movement along the collision axis (Y) or bounce off the surface
        when (motionComponent.surfaceReactionY) {
            GROUND_WALKING -> {
                // velocityX not changed
                motionComponent.velocityY = 0f
            }
            EXPLOSION_SLIDING -> {
                motionComponent.velocityX *= 0.1f   // Let the explosions slide on the ground (or ceiling)
                motionComponent.velocityY *= 0.07f  // and move them a bit into the ground
            }
            BOUNCE -> {
                motionComponent.velocityY *= -1f  // Bounce off the surface
            }
        }

        val cyMovedLeadingEdge = (yPosMovedLeadingEdge / gridSize).toIntFloor()

        // Resolve the collision - move the entity out of the collider cell vertically and
        // move the entity horizontally only by ratio of the vertical movement
        val isMovingDown = yMovement > 0f
        if (isMovingDown) {  // Movement in bottom direction - Y Axis inverted !!!
            val collisionRectOffsetY = collisionComponent.rect.height + collisionComponent.rect.y
            val cyCell = cyMovedLeadingEdge
            // New Position on top of the collider cell
            positionComponent.y = cyCell * gridSize - guardMove - collisionRectOffsetY
            collisionComponent.isGrounded = true
        } else {  // Movement in top direction
            val collisionRectOffsetY = -collisionComponent.rect.y  // Y Axis inverted - move the object downwards
            val cyCell = cyMovedLeadingEdge + 1
            // New position bottom below the collider cell
            positionComponent.y = cyCell * gridSize + guardMove + collisionRectOffsetY
            collisionComponent.isCollidingAbove = true
        }
    }

    /**
     * Enforces level boundaries by clamping the entity's position strictly within map bounds
     * with a guard margin (`guardBorder = worldMapData.tileSize.toFloat()`).
     *
     * @param positionComponent The position component of the entity to clamp.
     */
    protected fun checkPlayfieldBoundaries(positionComponent: Position) {
        // Keep the player sprite inside the level
        val guardBorder = worldMapData.tileSize.toFloat()
        // Check left and right boundaries with a guard border to prevent the playfield to render outside of the visible area, e.g. if the camera "shakes" on explosions etc.
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
    protected fun getCollisionSteps(horizontalMovement: Float, verticalMovement: Float, gridSize: Float): Int {
        if (gridSize < 1f && gridSize > -1f) {
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
        overallMovementX: Float,
        overallMovementY: Float,
        callback: (movementX: Float, movementY: Float) -> Boolean
    ) {
        if (steps > 0) {
            // Init movement and take into account that overall movement can be negative
            val movementX: Float = overallMovementX / steps
            val movementY: Float = overallMovementY / steps
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
