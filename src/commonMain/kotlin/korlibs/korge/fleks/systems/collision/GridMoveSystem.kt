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
import kotlin.math.min


class GridMoveSystem : IteratingSystem(
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

            counterDgb++
//            println("T: $counterDgb - old x: ${positionComponent.x} y: ${positionComponent.y}")
        }

//        val newX = positionComponent.x

        processCollisionSteps(steps, overallMovementX, overallMovementY) { movementX, movementY ->

            // First move and check the object horizontally to see if it collides with the level map horizontally
            // If yes, don't move it into the wall (e.g.)
            val isCollidingX = checkCollisionHorizontally(movementX, positionComponent, motionComponent, collisionComponent, gridSize, debugShapesComponent)

//            println("T: $counterDgb - new x: ${positionComponent.x}")

            // Then move and check the object vertically to see if it collides with the level map below
            // If yes, don't move it into the ground (e.g.)
            val isCollidingY = checkCollisionVertically(movementY, positionComponent, motionComponent, collisionComponent, gridSize, debugShapesComponent)

//            if (newX != positionComponent.x) println("T: $counterDgb - new x: ${newX} - collision x: ${positionComponent.x}")
//            println("T: $counterDgb - new x: ${positionComponent.x} y: ${positionComponent.y}")

            // TODO main sprite must not break here

            // Return true if there is a collision -- so that collision processing is stopped
            isCollidingX || isCollidingY
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

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        //println("GridMoveSystem: onAlphaEntity: ${entity.id} alpha: $alpha")
//        entity[GridComponent].interpolationAlpha = alpha
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
    private fun checkCollisionHorizontally(
        movementX: Float,
        positionComponent: Position,
        motionComponent: Motion,
        collisionComponent: Collision,
        gridSize: Float,
        debugShapesComponent: DebugCollisionShapes?
    ): Boolean {
        // If no movement in X direction, no need to check for collisions
        if (movementX == 0f) return false

        // Set X position of the collision box edge
        val collisionBoxOffsetX: Float = if (movementX > 0f) collisionComponent.rect.width + collisionComponent.rect.x.toFloat() else collisionComponent.rect.x.toFloat()
        val x = positionComponent.x + positionComponent.offsetX + collisionBoxOffsetX + movementX
        //             pivot point + collision rect offset + movement

        // Run the collision check for the entire height of the collision rect
        // Set Y positions of the collision box edge in gridSize steps (at least bottom and top edges)
        repeat( (collisionComponent.rect.height / gridSize).toIntCeil()) { i ->
            // Bottom edge of collision box and going up by gridSize
            val y = positionComponent.y + positionComponent.offsetY + collisionComponent.rect.y + i * gridSize

//            debugShapesComponent?.let { debugSaveRatioPointX(it, x, y) }  // Visualize the collider point

            if (isCollidingInXDirection(x, y, movementX, positionComponent, motionComponent, collisionComponent, gridSize, debugShapesComponent)) return true
        }

        // Top edge of collision box
        val y = positionComponent.y + positionComponent.offsetY + collisionComponent.rect.y + collisionComponent.rect.height

//        debugShapesComponent?.let { debugSaveRatioPointX(it, x, y) }  // Visualize the collider point
        debugShapesComponent?.let {
            it.vx = x
            it.vyt = positionComponent.y + positionComponent.offsetY + collisionComponent.rect.y  // Top
            it.vyb = positionComponent.y + positionComponent.offsetY + collisionComponent.rect.y + collisionComponent.rect.height  // Bottom
        }


        return isCollidingInXDirection(x, y, movementX, positionComponent, motionComponent, collisionComponent, gridSize, debugShapesComponent)
    }

    fun isCollidingInXDirection(
        x: Float,
        y: Float,
        movementX: Float,
        positionComponent: Position,
        motionComponent: Motion,
        collisionComponent: Collision,
        gridSize: Float,
        debugShapesComponent: DebugCollisionShapes?
    ): Boolean {
        val cx = (x / gridSize).toIntFloor()
        val cy = (y / gridSize).toIntFloor()

        val colliding = worldMapData.hasCollision(cx, cy)

        debugShapesComponent?.let { debugSaveGridCell(it, cx, cy) }  // Visualize the collider cell

        if (colliding) {
            // We are in front of a collider cell (wall) - thus we stop the motion
            motionComponent.velocityX = 0f

            // Resolve the collision - move the entity out of the collider cell
            if (movementX > 0f) {  // Movement to right
                // Left to the collider cell
                val collisionRectOffsetX = collisionComponent.rect.width + collisionComponent.rect.x
                positionComponent.x = cx * gridSize - 0.001f - collisionRectOffsetX
                collisionComponent.right = true
            } else {  // Movement to left
                // Right to the collider cell
                val collisionRectOffsetX = -collisionComponent.rect.x
                positionComponent.x = (cx + 1) * gridSize + 0.001f + collisionRectOffsetX
                collisionComponent.left = true
            }
            return true
        } else return false
    }

    /**
     * Check for vertical collisions and resolve them if necessary.
     * The entity is moved in the Y direction and the collision is checked.
     * If a collision is detected, it is resolved and the collision flags are set accordingly.
     *
     * Note: Before invoking this function the object was already moved horizontally. That is
     * reflected in its position component X property.
     *
     * @param movementY The amount of movement in pixels to check for collisions.
     * @param positionComponent The position component of the entity. It contains the position (pivot point) of the entity.
     * @param motionComponent The motion component of the entity.
     * @param collisionComponent The collision component of the entity.
     * @param gridSize The size of the grid.
     * @param debugShapesComponent Optional debug shapes component for visual debugging.
     *
     * @return `true` if a collision was detected, `false` otherwise.
     */
    fun checkCollisionVertically(
        movementY: Float,
        positionComponent: Position,
        motionComponent: Motion,
        collisionComponent: Collision,
        gridSize: Float,
        debugShapesComponent: DebugCollisionShapes?
    ): Boolean {
        // If no movement in X direction, no need to check for collisions
        if (movementY == 0f) return false

        // Set Y position of the collision box edge
        val collisionBoxOffsetY: Float = if (movementY > 0f) {  // positive movement == going down!!!
            // Bottom edge of collision box
            collisionComponent.rect.y.toFloat() + collisionComponent.rect.height
        } else {
            // Top edge of collision box
            collisionComponent.rect.y.toFloat()
        }
        val y = positionComponent.y + positionComponent.offsetY + collisionBoxOffsetY + movementY
        //                                         pivot point + collision rect offset + movement

        // Run the collision check for the entire height of the collision rect
        // Set Y positions of the collision box edge in gridSize steps (at least bottom and top edges)
        repeat( (collisionComponent.rect.width / gridSize).toIntCeil()) { i ->
            // Left edge of collision box and going right by gridSize
            val x = positionComponent.x + positionComponent.offsetX + collisionComponent.rect.x + i * gridSize

//            debugShapesComponent?.let { debugSaveRatioPointY(it, x, y) }  // Visualize the collider point

            if (isCollidingInYDirection(x, y, movementY, positionComponent, motionComponent, collisionComponent, gridSize, debugShapesComponent)) return true
        }

        // Right edge of collision box
        val x = positionComponent.x + positionComponent.offsetX + collisionComponent.rect.x + collisionComponent.rect.width

//        debugShapesComponent?.let { debugSaveRatioPointY(it, x, y) }  // Visualize the collider point

        return isCollidingInYDirection(x, y, movementY, positionComponent, motionComponent, collisionComponent, gridSize, debugShapesComponent)
    }

    fun isCollidingInYDirection(
        x: Float,
        y: Float,
        movementY: Float,
        positionComponent: Position,
        motionComponent: Motion,
        collisionComponent: Collision,
        gridSize: Float,
        debugShapesComponent: DebugCollisionShapes?
    ): Boolean {
        val cx = (x / gridSize).toIntFloor()
        val cy = (y / gridSize).toIntFloor()

        val colliding = worldMapData.hasCollision(cx, cy)

        debugShapesComponent?.let { debugSaveGridCell(it, cx, cy) }  // Visualize the collider cell

        if (colliding) {
            // We are in front of a collider cell (wall) - thus we stop the motion
            motionComponent.velocityY = 0f

            // Resolve the collision - move the entity out of the collider cell
            if (movementY > 0f) {  // Movement to bottom - Y Axis inverted !!!
                // New Position: on top of the collider cell
                val collisionRectOffsetY = collisionComponent.rect.height + collisionComponent.rect.y
                positionComponent.y = cy * gridSize - 0.001f - collisionRectOffsetY
                collisionComponent.right = true
            } else {  // Movement in top direction
                // bottom below the collider cell
                val collisionRectOffsetY = -collisionComponent.rect.y  // Y Axis inverted - move the object downwards
                positionComponent.y = (cy + 1) * gridSize + 0.001f + collisionRectOffsetY
                collisionComponent.left = true
            }
            return true
        } else return false
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

fun resolveXCollision(
    positionComponent: Position,
    motionComponent: Motion,
    dir: Int
) {

}

