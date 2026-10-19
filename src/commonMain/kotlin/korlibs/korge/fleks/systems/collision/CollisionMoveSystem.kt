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
    private val guardMove = 0f // 0.001f  TODO check if we can keep this zero
    private val guard = 0.01f
    protected fun Float.isNotAlmostZero(): Boolean = this.absoluteValue > guard

    override fun onTickEntity(entity: Entity) {
        // Iterate over all entities that have a Grid, Collision and MotionComponent
        val motionComponent = entity[MotionComponent]
        val positionComponent = entity[PositionComponent]
        val collisionComponent = entity[CollisionComponent]
        val collisionRect = collisionComponent.rect

        val debugShapesComponent: DebugCollisionShapes? = entity.getOrNull(DebugCollisionShapesComponent)
        // Free debug points before we create new ones
        debugShapesComponent?.cleanup()

//        if (motionComponent.velocityY < -0.01f || motionComponent.velocityY > 0.01f) {
//            println("GridMoveSystem: '${world.nameOf(entity)}' velocityY before gravity: ${motionComponent.velocityY}")
//        }

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

        // Return true if there is a collision
        val isColliding = processCollisionSteps(steps, overallMovementX, overallMovementY, motionComponent.frictionX) { movementX, movementY ->
            checkCollision(movementX, movementY, positionComponent, motionComponent, collisionComponent, gridSize, debugShapesComponent)
        }

        // TODO
        // Depending of type of the entity we have to stop further collision steps (e.g. for shoots, because they get destroyed)
        // Or collision checking continues when one direction is already colliding (e.g. the main character, which is grounded but still can move horizontally)

//        motionComponent.velocityX *= motionComponent.frictionX

        if (motionComponent.velocityX.isAlmostEquals(0.0005f)) {
            // Velocity values smaller than 0.0005f are zeroed out
            motionComponent.velocityX = 0f
        }

//        // Friction only in down direction (falling)
//        if (motionComponent.velocityY > 0f) {
//            motionComponent.velocityY *= motionComponent.frictionY
//        }

        if (motionComponent.velocityY.isAlmostEquals(0.0005f)) {
            // Velocity values smaller than 0.0005f are zeroed out
            motionComponent.velocityY = 0f
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Step 7: Impact Reaction & Playfield Clamping
        //
        // Now finally we can move our entity normally when there was no collision
        if (!isColliding) {
            positionComponent.x += overallMovementX
            positionComponent.y += overallMovementY
        }

        // Keep entity positions strictly within level boundaries (with 16px guard margin).
        checkPlayfieldBoundaries(positionComponent)
    }

    /**
     * Check for horizontal collisions and resolve them if necessary.
     * The entity is moved in the X direction and the collision is checked.
     * If a collision is detected, it is resolved and the collision flags are set accordingly.
     *
     * @param movementX The amount of movement in pixels to check for collisions.
     * @param positionComponent The position component of the entity. It contains the position (pivot point) of the entity.
     * @param motionComponent The motion component of the entity.
     * @param collisionComponent The collision component of the entity.
     * @param gridSize The size of the grid.
     * @param debugShapesComponent Optional debug shapes component for visual debugging.
     *
     * @return `true` if a collision was detected, `false` otherwise.
     */
    protected fun checkCollision(
        movementX: Float,
        movementY: Float,
        positionComponent: Position,
        motionComponent: Motion,
        collisionComponent: Collision,
        gridSize: Float,
        debugShapesComponent: DebugCollisionShapes?
    ): Boolean {
        val guard = 0f // 0.001f

        // Define position of moved collision box edges
        val xPosMovedLeft = positionComponent.x + positionComponent.offsetX + collisionComponent.rect.x + movementX
        val yPosMovedTop = positionComponent.y + positionComponent.offsetY + collisionComponent.rect.y + movementY

        // Leading edge coordinates for probing
        val xLeadingEdge = if (movementX > 0f) xPosMovedLeft + collisionComponent.rect.width - guard else xPosMovedLeft
        val yLeadingEdge = if (movementY > 0f) yPosMovedTop + collisionComponent.rect.height - guard else yPosMovedTop

        // Span lengths along active edges (guarded to stay strictly within collider bounds)
        val horizSpan = max(0f, collisionComponent.rect.width - guard)
        val vertSpan = max(0f, collisionComponent.rect.height - guard)

        val horizSteps = (horizSpan / gridSize).toIntCeil()
        val vertSteps = (vertSpan / gridSize).toIntCeil()

        // Maximum steps needed to cover the longest edge
        val steps = max(horizSteps, vertSteps)

        for (i in 0..steps) {
            // Horizontal movement probe: sample y along the vertical leading edge (xLeadingEdge)
            val isCollidingHor = if (movementX.isNotAlmostZero() && i <= vertSteps) {
                val yProbe = if (movementY < 0f) {
                    max(yPosMovedTop, yPosMovedTop + vertSpan - i * gridSize)
                } else {
                    min(yPosMovedTop + i * gridSize, yPosMovedTop + vertSpan)
                }
                val cxProbe = (xLeadingEdge / gridSize).toIntFloor()
                val cyProbe = (yProbe / gridSize).toIntFloor()

                debugShapesComponent?.let { debugSaveRatioPointX(it, xLeadingEdge, yProbe) }
                debugShapesComponent?.let { debugSaveGridCell(it, cxProbe, cyProbe, worldMapData) }
                worldMapData.hasCollision(cxProbe, cyProbe)
            } else false

            // Vertical movement probe: sample x along the horizontal leading edge (yLeadingEdge)
            val isCollidingVer = if (movementY.isNotAlmostZero() && i <= horizSteps) {
                val xProbe = if (movementX < 0f) {
                    max(xPosMovedLeft, xPosMovedLeft + horizSpan - i * gridSize)
                } else {
                    min(xPosMovedLeft + i * gridSize, xPosMovedLeft + horizSpan)
                }
                val cxProbe = (xProbe / gridSize).toIntFloor()
                val cyProbe = (yLeadingEdge / gridSize).toIntFloor()

                debugShapesComponent?.let { debugSaveRatioPointY(it, xProbe, yLeadingEdge) }
                debugShapesComponent?.let { debugSaveGridCell(it, cxProbe, cyProbe, worldMapData) }
                worldMapData.hasCollision(cxProbe, cyProbe)
            } else false

            if (isCollidingHor && !isCollidingVer) {
                val xPos = positionComponent.x + positionComponent.offsetX +
                    if (movementX > 0f) collisionComponent.rect.x + collisionComponent.rect.width else collisionComponent.rect.x.toFloat()
                val cxMoved = (xLeadingEdge / gridSize).toIntFloor()
                resolveCollisionHorizontally(movementX, movementY, xPos, cxMoved, positionComponent, motionComponent, collisionComponent, gridSize)
                return true
            } else if (!isCollidingHor && isCollidingVer) {
                val yPos = positionComponent.y + positionComponent.offsetY +
                    if (movementY > 0f) collisionComponent.rect.y + collisionComponent.rect.height else collisionComponent.rect.y.toFloat()
                val cyMoved = (yLeadingEdge / gridSize).toIntFloor()
                resolveCollisionVertically(movementX, movementY, yPos, cyMoved, positionComponent, motionComponent, collisionComponent, gridSize)
                return true
            } else if (isCollidingHor) {
                val cxMoved = (xLeadingEdge / gridSize).toIntFloor()
                val cyMoved = (yLeadingEdge / gridSize).toIntFloor()
                val xDistance = if (movementX > 0f) xLeadingEdge - (cxMoved * gridSize) else ((cxMoved + 1) * gridSize) - xLeadingEdge
                val yDistance = if (movementY > 0f) yLeadingEdge - (cyMoved * gridSize) else ((cyMoved + 1) * gridSize) - yLeadingEdge

                if (xDistance > yDistance) {
                    val xPos = positionComponent.x + positionComponent.offsetX +
                        if (movementX > 0f) collisionComponent.rect.x + collisionComponent.rect.width else collisionComponent.rect.x.toFloat()
                    resolveCollisionHorizontally(movementX, movementY, xPos, cxMoved, positionComponent, motionComponent, collisionComponent, gridSize)
                } else {
                    val yPos = positionComponent.y + positionComponent.offsetY +
                        if (movementY > 0f) collisionComponent.rect.y + collisionComponent.rect.height else collisionComponent.rect.y.toFloat()
                    resolveCollisionVertically(movementX, movementY, yPos, cyMoved, positionComponent, motionComponent, collisionComponent, gridSize)
                }
                return true
            }
        }
        return false
    }

    protected fun resolveCollisionHorizontally(
        xMovement: Float,
        yMovement: Float,
        xPos: Float,
        cxMoved: Int,
        positionComponent: Position,
        motionComponent: Motion,
        collisionComponent: Collision,
        gridSize: Float
    ) {
        // Stop movement along the collision axis (X)
        motionComponent.velocityX = 0f

        // Apply frictionY along the orthogonal axis (Y)
        motionComponent.velocityY *= motionComponent.frictionY
//        if (motionComponent.frictionY >= HIGH_FRICTION) {
//            motionComponent.velocityY = 0f
//        } else if (motionComponent.frictionY > 0f) {
//            motionComponent.velocityY *= max(0f, 1f - motionComponent.frictionY)  // ????
//        }

        // Resolve the collision - move the entity out of the collider cell horizontally and
        // move the entity vertically only by ratio of the horizontal movement
        val xRatio: Float
        val isMovingRight = xMovement > 0f
        if (isMovingRight) {
            // Movement in right direction
            val collisionRectOffsetX = collisionComponent.rect.width + collisionComponent.rect.x
            val cxCell = cxMoved
            // New position left to the collider cell
            positionComponent.x = cxCell * gridSize - guardMove - collisionRectOffsetX
            collisionComponent.right = true
            // Ratio to the left edge of the collider cell
            val xDistance = cxCell * gridSize - xPos
            xRatio = if (xMovement.isNotAlmostZero()) xDistance / xMovement else 0f

        } else {
            // Movement in left direction
            val collisionRectOffsetX = -collisionComponent.rect.x
            val cxCell = cxMoved + 1
            // New position right to the collider cell
            positionComponent.x = cxCell * gridSize + guardMove + collisionRectOffsetX
            collisionComponent.left = true
            // Ratio to the right edge of the collider cell
            val xDistance = cxCell * gridSize - xPos
            xRatio = if (xMovement.isNotAlmostZero()) xDistance / xMovement else 0f
        }
        // Move the entity vertically to the correct position by applying only ratio of the movement
        // Ratio is always positive because above xDistance and movementX has always same sign
        if (xMovement.isNotAlmostZero()) positionComponent.y += yMovement * xRatio
    }

    protected fun resolveCollisionVertically(
        xMovement: Float,
        yMovement: Float,
        yPos: Float,
        cyMoved: Int,
        positionComponent: Position,
        motionComponent: Motion,
        collisionComponent: Collision,
        gridSize: Float
    ) {
        // Stop movement along the collision axis (Y)
        motionComponent.velocityY = 0f

        // Apply frictionX along the orthogonal axis (X)
        motionComponent.velocityX *= motionComponent.frictionX
//        if (motionComponent.frictionX >= HIGH_FRICTION) {
//            motionComponent.velocityX = 0f
//        } else if (motionComponent.frictionX > 0f) {
//            motionComponent.velocityX *= max(0f, 1f - motionComponent.frictionX)  // ???? maybe set HIGH_FRICTION to 1f
//        }

        // Resolve the collision - move the entity out of the collider cell vertically and
        // move the entity horizontally only by ratio of the vertical movement
        val yRatio: Float
        val isMovingDown = yMovement > 0f
        if (isMovingDown) {
            // Movement in bottom direction - Y Axis inverted !!!
            val collisionRectOffsetY = collisionComponent.rect.height + collisionComponent.rect.y
            val cyCell = cyMoved
            // New Position on top of the collider cell
            positionComponent.y = cyCell * gridSize - guardMove - collisionRectOffsetY
            collisionComponent.isGrounded = true
            // Ratio to the top edge of the collider cell
            val yDistance = cyCell * gridSize - yPos
            yRatio = if (yMovement.isNotAlmostZero()) yDistance / yMovement else 0f

        } else {
            // Movement in top direction
            val collisionRectOffsetY = -collisionComponent.rect.y  // Y Axis inverted - move the object downwards
            val cyCell = cyMoved + 1
            // New position bottom below the collider cell
            positionComponent.y = cyCell * gridSize + guardMove + collisionRectOffsetY
            collisionComponent.isCollidingAbove = true
            // Ratio to the bottom edge of the collider cell
            val yDistance = cyCell * gridSize - yPos
            yRatio = if (yMovement.isNotAlmostZero()) yDistance / yMovement else 0f
        }
        // Move the entity horizontally to the correct position by applying only ratio of the movement
        // Ratio is always positive because above yDistance and movementY has always same sign
        if (yMovement.isNotAlmostZero()) positionComponent.x += xMovement * yRatio
    }

    /**
     * Check if the object is within the playfield boundaries.
     * If the object is outside the boundaries, it is moved back to the nearest boundary.
     * If the object falls off the playfield at the bottom, the object is considered dead.
     *
     * @param positionComponent The grid component of the entity to check.
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
     * Calculate the number of steps needed to move the entity in relation to the step size (usually 16x16 pixels or smaller).
     *
     * @param horizontalMovement The horizontal movement of the entity.
     * @param verticalMovement The vertical movement of the entity.
     * @param gridSize The size of a step. Can be smaller than the grid size.
     * @return The number of steps needed for collision checking.
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
     * Process the collision steps and call the callback function for each step.
     *
     * In case of friction set to INFINITE_FRICTION the callback function can return true.
     * This is used for collision handling of objects which are destroyed on collision.
     *
     * @param steps The number of steps to process.
     * @param overallMovementX The overall movement in the X direction.
     * @param overallMovementY The overall movement in the Y direction.
     * @param callback The callback function to call for each step. It takes the movement in the X and Y
     *        direction as parameters and returns a boolean value indicating whether to stop processing
     *        the steps. If the callback returns `true`, the processing is stopped.
     *
     * @return `true` if the callback returned `true` for any step, `false` otherwise.
     * @see getCollisionSteps
     */
    protected fun processCollisionSteps(
        steps: Int,
        overallMovementX: Float,
        overallMovementY: Float,
        friction: Float,
        callback: (movementX: Float, movementY: Float) -> Boolean
    ): Boolean {
        if (steps > 0) {
            // Init movement and take into account that overall movement can be negative
            val movementX: Float = overallMovementX / steps
            val movementY: Float = overallMovementY / steps
            var i = 0

            while (i < steps) {
                val result = callback(movementX, movementY)
//                if (result && friction == INFINITE_FRICTION) return true
                if (result) return true
                i++
            }
        }
        return false
    }
}
