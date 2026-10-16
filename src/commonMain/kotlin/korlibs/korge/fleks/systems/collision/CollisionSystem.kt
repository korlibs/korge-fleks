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
import korlibs.korge.fleks.components.Grid
import korlibs.korge.fleks.components.LifeCycle.Companion.LifeCycleComponent
import korlibs.korge.fleks.components.Motion
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.data.Point.Companion.point
import korlibs.korge.fleks.logic.collision.checker.CollisionChecker
import korlibs.korge.fleks.logic.collision.checker.PlatformerCollisionChecker
import korlibs.korge.fleks.logic.collision.resolver.CollisionResolver
import korlibs.korge.fleks.logic.collision.resolver.PlatformerCollisionResolver
import korlibs.korge.fleks.utils.CollisionUtils.getCollisionSteps
import korlibs.korge.fleks.utils.CollisionUtils.processCollisionSteps
import korlibs.korge.fleks.utils.DebugPointPool
import korlibs.math.isAlmostEquals
import korlibs.math.toIntCeil
import korlibs.math.toIntFloor
import kotlin.math.max
import kotlin.math.min


class CollisionSystem : IteratingSystem(
    family = World.family { all(PositionComponent, MotionComponent, CollisionComponent)
        .any(PositionComponent, DebugCollisionShapesComponent) },
    interval = Fixed(1 / 60f)
) {
    val assetStore = world.inject<AssetStore>("AssetStore")

    private val worldMapData = assetStore.worldMapData

    var collisionChecker: CollisionChecker = PlatformerCollisionChecker(world.inject<DebugPointPool>("DebugPointPool"))
    var collisionResolver: CollisionResolver = PlatformerCollisionResolver()

    private val gridComponent: Grid = Grid.staticGridComponent {}

    private var counterDgb = 0
    private val guardRegion = 0.001f

    data class CellBoderCheckProps(
        var xPos: Float,
        var xPosMoved: Float,
        var cxMoved: Int,
        var movedIntoNewCell: Boolean
    )

    private val cellBorderCheckProps = CellBoderCheckProps(0f, 0f, 0, false)

    override fun onTickEntity(entity: Entity) {
//        val overallTime = TimeProvider.now().milliseconds

        // Iterate over all entities that have a Grid, Collision and MotionComponent
        val motionComponent = entity[MotionComponent]
        val positionComponent = entity[PositionComponent]
        val collisionComponent = entity[CollisionComponent]
        val collisionBox = collisionComponent.rect

        val debugShapesComponent: DebugCollisionShapes? = entity.getOrNull(DebugCollisionShapesComponent)
        // Free debug points before we create new ones
        debugShapesComponent?.cleanup()

//        if (motionComponent.velocityY < -0.01f || motionComponent.velocityY > 0.01f) {
//            println("GridMoveSystem: '${world.nameOf(entity)}' velocityY before gravity: ${motionComponent.velocityY}")
//        }

        val gravityComponent = entity.getOrNull(GravityComponent)
        // Apply gravity to the entity if it has a GravityComponent
        if (gravityComponent != null) {
// TODO add back on Y collision            motionComponent.velocityY += -gravityComponent.calculateDeltaGravity() * deltaTime
        }

        val overallMovementX = motionComponent.velocityX * deltaTime
        val overallMovementY = motionComponent.velocityY * deltaTime  // Negative sign for upward movement - Y Axis is mirrored in the game
        val gridSize = worldMapData.tileSize.toFloat()

        // The number of steps which an object travels through a grid cell is defined either by its collision rect size or the grid size
        // I.e. if the collision rect is smaller than the grid size we need to check "more often" within a cell otherwise
        // it is enough to check once per grid cell
        val stepSize = min(min(collisionBox.width, collisionBox.height), gridSize)

        // Check if movement in X or Y direction is greater than the grid size and set the number of steps accordingly
        // If we have at least a small movement there will be at least one step executed
        val steps: Int = getCollisionSteps(overallMovementX, overallMovementY, stepSize)

        if (steps > 0) {
            // Reset collision flags if the entity is moving
            collisionComponent.right = false
            collisionComponent.left = false
            collisionComponent.isGrounded = false
            collisionComponent.isCollidingAbove = false
        }

        // Return true if there is a collision -- collision processing is stopped
        // TODO we need to be able to specify that collision checking should not be interrupted
        val isColliding = processCollisionSteps(steps, overallMovementX, overallMovementY) { movementX, movementY ->
            checkCollision(movementX, movementY, positionComponent, motionComponent, collisionComponent, gridSize, debugShapesComponent)
        }

            // TODO
            // Depending of type of the entity we have to stop further collision steps (e.g. for shoots, because they get destroyed)
            // Or collision checking continues when one direction is already colliding (e.g. the main character, which is grounded but still can move horizontally)

        if (isColliding) {
            // Shoots need to be destroyed the next frame
            val healthComponent = entity.getOrNull(LifeCycleComponent)
            healthComponent?.healthCounter = 0
        }

//        motionComponent.velocityX *= motionComponent.frictionX
        if (motionComponent.velocityX.isAlmostEquals(0.0005f)) {
            motionComponent.velocityX = 0f
        }
//
//        // Friction only in down direction (falling)
//        if (motionComponent.velocityY > 0f) {
//            motionComponent.velocityY *= motionComponent.frictionY
//        }
        if (motionComponent.velocityY.isAlmostEquals(0.0005f)) {
            motionComponent.velocityY = 0f
        }

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
    private fun checkCollision(
        movementX: Float,
        movementY: Float,
        positionComponent: Position,
        motionComponent: Motion,
        collisionComponent: Collision,
        gridSize: Float,
        debugShapesComponent: DebugCollisionShapes?
    ): Boolean {
        // Define position of moved collision box edges
        val xPosMovedLeft = positionComponent.x + positionComponent.offsetX + collisionComponent.rect.x + movementX
        val xPosMovedRight = positionComponent.x + positionComponent.offsetX + collisionComponent.rect.x + collisionComponent.rect.width + movementX
        val yPosMovedTop = positionComponent.y + positionComponent.offsetY + collisionComponent.rect.y + movementY
        val yPosMovedBottom = positionComponent.y + positionComponent.offsetY + collisionComponent.rect.y + collisionComponent.rect.height + movementY


        // Position of the top-left corner of the collision box: (xPosMoved, yPosMoved)

        // Get the steps number from the longer edge of the collision box
        // Set positions of the collision box edge in gridSize steps (at least bottom and top edges)
        val longEdge = max(collisionComponent.rect.width, collisionComponent.rect.height)
        val steps = (longEdge / gridSize).toIntCeil()
        for (i in 0..steps) {
            // Horizontal check - get position for the collision check depending on which direction we are moving
            val yPosMovedStepHor =
                // Moving in the bottom direction - Check horizontal collision edge from the top or bottom
                if (movementY > 0f) yPosMovedTop + if (i == steps) collisionComponent.rect.height else i * gridSize
                // Moving in the top direction - Check horizontal collision edge from the bottom or top
                else yPosMovedBottom - if (i == steps) collisionComponent.rect.height else i * gridSize
            val cyMovedStepHor = (yPosMovedStepHor / gridSize).toIntFloor()
            val xPosMovedStepHor = if (movementX > 0f) xPosMovedLeft + collisionComponent.rect.width else xPosMovedLeft
            val cxMovedStepHor = (xPosMovedStepHor / gridSize).toIntFloor()

            // Do collision check at position (xPosMovedStepHor, yPosMovedStepHor) in the level map
            debugShapesComponent?.let { debugSaveRatioPointX(it, xPosMovedStepHor, yPosMovedStepHor) }  // Visualize the collider point
            debugShapesComponent?.let { debugSaveGridCell(it, cxMovedStepHor, cyMovedStepHor) }  // Visualize the collider cell
            val isCollidingHor = worldMapData.hasCollision(cxMovedStepHor, cyMovedStepHor)

            // Vertical check - get position for the collision check depending on which direction we are moving
            val xPosMovedStepVer =
                // Moving in the right direction - Check horizontal collision edge from the left or right
                if (movementX > 0f) xPosMovedLeft + if (i == steps) collisionComponent.rect.width else i * gridSize
                // Moving in the left direction - Check horizontal collision edge from the right or left
                else xPosMovedRight - if (i == steps) collisionComponent.rect.width else i * gridSize
            val cxMovedStepVer = (xPosMovedStepVer / gridSize).toIntFloor()
            val yPosMovedStepVer = if (movementY > 0f) yPosMovedTop + collisionComponent.rect.height else yPosMovedTop
            val cyMovedStepVer = (yPosMovedStepVer / gridSize).toIntFloor()

            // Do collision check at position (xPosMovedStepVer, yPosMovedStepVer) in the leve map
            debugShapesComponent?.let { debugSaveRatioPointY(it, xPosMovedStepVer, yPosMovedStepVer) }  // Visualize the collider point
            debugShapesComponent?.let { debugSaveGridCell(it, cxMovedStepVer, cyMovedStepVer) }  // Visualize the collider cell
            val isCollidingVer = worldMapData.hasCollision(cxMovedStepVer, cyMovedStepVer)

            // Check in which combination we are colliding: horizontally or vertically or in both directions
            if (isCollidingHor && !isCollidingVer) {
                // Resolve collision only horizontally
                // x position of the collision box edge - not moved
                val xPos = positionComponent.x + positionComponent.offsetX +
                    if (movementX > 0f) collisionComponent.rect.x + collisionComponent.rect.width else collisionComponent.rect.x.toFloat()
                val cxMoved = cxMovedStepHor
                resolveCollisionHorizontally(movementX, movementY, xPos, cxMoved, positionComponent, motionComponent, collisionComponent, gridSize)
                return true
            } else if (!isCollidingHor && isCollidingVer) {
                // Resolve collision only vertically
                // y position of the collision box edge - not moved
                val yPos = positionComponent.y + positionComponent.offsetY +
                    if (movementY > 0f) collisionComponent.rect.y + collisionComponent.rect.height else collisionComponent.rect.y.toFloat()
                val cyMoved = cyMovedStepVer
                resolveCollisionVertically(movementX, movementY, yPos, cyMoved, positionComponent, motionComponent, collisionComponent, gridSize)
                return true
            } else if (isCollidingHor) {
                // We are colliding in both directions - check in which direction we are colliding earlier on movement
                val xDistance = if (movementX > 0f) xPosMovedStepHor - (cxMovedStepHor * gridSize) else ((cxMovedStepHor + 1) * gridSize) - xPosMovedStepHor
                val yDistance = if (movementY > 0f) yPosMovedStepHor - (cyMovedStepHor * gridSize) else ((cyMovedStepHor + 1) * gridSize) - yPosMovedStepHor

                if (xDistance > yDistance) {
                    // Resolve collision only horizontally
                    // x position of the collision box edge - not moved
                    val xPos = positionComponent.x + positionComponent.offsetX +
                        if (movementX > 0f) collisionComponent.rect.x + collisionComponent.rect.width else collisionComponent.rect.x.toFloat()
                    val cxMoved = cxMovedStepHor
                    resolveCollisionHorizontally(movementX, movementY, xPos, cxMoved, positionComponent, motionComponent, collisionComponent, gridSize)
                } else {
                    // Resolve collision only vertically
                    // y position of the collision box edge - not moved
                    val yPos = positionComponent.y + positionComponent.offsetY +
                        if (movementY > 0f) collisionComponent.rect.y + collisionComponent.rect.height else collisionComponent.rect.y.toFloat()
                    val cyMoved = cyMovedStepVer
                    resolveCollisionVertically(movementX, movementY, yPos, cyMoved, positionComponent, motionComponent, collisionComponent, gridSize)
                }
                return true
            }
        }
        return false
    }

    private fun resolveCollisionHorizontally(
        xMovement: Float,
        yMovement: Float,
        xPos: Float,
        cxMoved: Int,
        positionComponent: Position,
        motionComponent: Motion,
        collisionComponent: Collision,
        gridSize: Float
    ) {

        // We are in front of a collider cell (wall) - thus we stop the motion
        motionComponent.velocityX = 0f
        motionComponent.velocityY = 0f

        // Resolve the collision - move the entity out of the collider cell horizontally and
        // move the entity vertically only by ratio of the horizontal movement
        val xRatio: Float
        val isMovingRight = xMovement > 0f
        if (isMovingRight) {
            // Movement in right direction
            val collisionRectOffsetX = collisionComponent.rect.width + collisionComponent.rect.x
            val cxCell = cxMoved
            // New position left to the collider cell
            positionComponent.x = cxCell * gridSize - 0.001f - collisionRectOffsetX
            collisionComponent.right = true
            // Ratio to the left edge of the collider cell
            val xDistance = cxCell * gridSize - xPos
            xRatio = xDistance / xMovement

        } else {
            // Movement in left direction
            val collisionRectOffsetX = -collisionComponent.rect.x
            val cxCell = cxMoved + 1
            // New position right to the collider cell
            positionComponent.x = cxCell * gridSize + 0.001f + collisionRectOffsetX
            collisionComponent.left = true
            // Ratio to the right edge of the collider cell
            val xDistance = cxCell * gridSize - xPos
            xRatio = xDistance / xMovement
        }
        // Move the entity vertically to the correct position by applying only ratio of the movement
        // Ratio is always positive because above xDistance and movementX has always same sign
        positionComponent.y += yMovement * xRatio
    }

    fun resolveCollisionVertically(
        xMovement: Float,
        yMovement: Float,
        yPos: Float,
        cyMoved: Int,
        positionComponent: Position,
        motionComponent: Motion,
        collisionComponent: Collision,
        gridSize: Float
    ) {
        // We are in front of a collider cell (wall) - thus we stop the motion
        motionComponent.velocityX = 0f
        motionComponent.velocityY = 0f

        // Resolve the collision - move the entity out of the collider cell vertically and
        // move the entity horizontally only by ratio of the vertical movement
        val yRatio: Float
        val isMovingDown = yMovement > 0f
        if (isMovingDown) {
            // Movement in bottom direction - Y Axis inverted !!!
            val collisionRectOffsetY = collisionComponent.rect.height + collisionComponent.rect.y
            val cyCell = cyMoved
            // New Position on top of the collider cell
            positionComponent.y = cyCell * gridSize - 0.001f - collisionRectOffsetY
            collisionComponent.isGrounded = true
            // Ratio to the top edge of the collider cell
            val yDistance = cyCell * gridSize - yPos
            yRatio = yDistance / yMovement

        } else {
            // Movement in top direction
            val collisionRectOffsetY = -collisionComponent.rect.y  // Y Axis inverted - move the object downwards
            val cyCell = cyMoved + 1
            // New position bottom below the collider cell
            positionComponent.y = cyCell * gridSize + 0.001f + collisionRectOffsetY
            collisionComponent.isCollidingAbove = true
            // Ratio to the bottom edge of the collider cell
            val yDistance = cyCell * gridSize - yPos
            yRatio = yDistance / yMovement
        }
        // Move the entity horizontally to the correct position by applying only ratio of the movement
        // Ratio is always positive because above yDistance and movementY has always same sign
        positionComponent.x += xMovement * yRatio
    }


        /**
     * Check if the object is within the playfield boundaries.
     * If the object is outside the boundaries, it is moved back to the nearest boundary.
     * If the object falls off the playfield at the bottom, the object is considered dead.
     *
     * @param positionComponent The grid component of the entity to check.
     */
    private fun checkPlayfieldBoundaries(positionComponent: Position) {
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

    private fun debugSaveGridCell(debugShapesComponent: DebugCollisionShapes, cx: Int, cy: Int) {
        debugShapesComponent.gridCells.add(
            point {
                x = cx.toFloat() * worldMapData.tileSize
                y = cy.toFloat() * worldMapData.tileSize
            }
        )
    }

    private fun debugSaveRatioPointX(debugShapesComponent: DebugCollisionShapes, x: Float, y: Float) {
        debugShapesComponent.ratioPositionsX.add(
            point {
                this.x = x
                this.y = y
            }
        )
    }
    private fun debugSaveRatioPointY(debugShapesComponent: DebugCollisionShapes, x: Float, y: Float) {
        debugShapesComponent.ratioPositionsY.add(
            point {
                this.x = x
                this.y = y
            }
        )
    }

}
