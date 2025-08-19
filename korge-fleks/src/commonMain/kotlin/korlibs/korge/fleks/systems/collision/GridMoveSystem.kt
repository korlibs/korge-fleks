package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.assets.data.gameObject.CollisionData
import korlibs.korge.fleks.components.Collision
import korlibs.korge.fleks.components.Collision.Companion.CollisionComponent
import korlibs.korge.fleks.components.DebugCollisionShapes
import korlibs.korge.fleks.components.DebugCollisionShapes.Companion.DebugCollisionShapesComponent
import korlibs.korge.fleks.components.Grid
import korlibs.korge.fleks.components.Grid.Companion.GridComponent
import korlibs.korge.fleks.components.Motion
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.State.Companion.StateComponent
import korlibs.korge.fleks.logic.collision.checker.CollisionChecker
import korlibs.korge.fleks.logic.collision.checker.PlatformerCollisionChecker
import korlibs.korge.fleks.logic.collision.resolver.CollisionResolver
import korlibs.korge.fleks.logic.collision.resolver.PlatformerCollisionResolver
import korlibs.korge.fleks.prefab.Prefab
import korlibs.korge.fleks.utils.AppConfig
import korlibs.korge.fleks.utils.DebugPointPool
import korlibs.math.isAlmostEquals
import kotlin.math.abs
import kotlin.math.ceil


class GridMoveSystem : IteratingSystem(
    family = World.family { all(MotionComponent, GridComponent, CollisionComponent, StateComponent)
        .any(DebugCollisionShapesComponent) },
    interval = Fixed(1 / 30f)
) {
    val assetStore = world.inject<AssetStore>("AssetStore")

    var collisionChecker: CollisionChecker = PlatformerCollisionChecker(world.inject<DebugPointPool>("DebugPointPool"))
    var collisionResolver: CollisionResolver = PlatformerCollisionResolver()

    override fun onTickEntity(entity: Entity) {
        // Iterate over all entities that have a Grid, Collision and MotionComponent
        val motionComponent = entity[MotionComponent]
        val gridComponent = entity[GridComponent]
        val collisionComponent = entity[CollisionComponent]
        val stateComponent = entity[StateComponent]
        val collisionBox = assetStore.getGameObjectStateConfig(stateComponent.name).getCollisionData(stateComponent.current)

        val debugShapesComponent: DebugCollisionShapes? = entity.getOrNull(DebugCollisionShapesComponent)
        // Free debug points before we create new ones
        debugShapesComponent?.cleanup()

//        val gravityComponent = entity.getOrNull(GravityComponent)
        // Apply gravity to the entity if it has a GravityComponent
//        if (gravityComponent != null) {
// TODO enable gravity again
//            motionComponent.velocityX += gravityComponent.calculateDeltaXGravity()
//            motionComponent.velocityY += gravityComponent.calculateDeltaYGravity()
//        }

        // Set the last pixel position to the current grid position
        gridComponent.lastPx = gridComponent.attachX
        gridComponent.lastPy = gridComponent.attachY

        /**
         * Any movement greater than [Grid.maxGridMovementPercent] will increase the number of steps here.
         * The steps will break down the movement into smaller iterators to avoid jumping over grid collisions
         */
        val overallMovementX = motionComponent.velocityX * deltaTime
        val overallMovementY = motionComponent.velocityY * deltaTime  // We need to invert the Y velocity because the Y axis is inverted in the grid system
        // Calculate the number of steps needed to move the entity in relation to the grid size (here 16x16 pixels)
        val steps = ceil((abs(overallMovementX) + abs(overallMovementY)) / AppConfig.GRID_CELL_SIZE)  // TODO for more steps within one grid cell:   / AppConfig.maxGridMovementPercent)
        if (steps > 0) {
            var i = 0
            while (i < steps) {
                // Reset collision flags
                collisionComponent.right = false
                collisionComponent.left = false
                collisionComponent.wasGroundedLastFrame = collisionComponent.isGrounded
                collisionComponent.isGrounded = false
                collisionComponent.isCollidingAbove = false

                checkCollisionHorizontally(overallMovementX / steps, gridComponent, motionComponent, collisionComponent, collisionBox, debugShapesComponent)
                checkCollisionVertically(overallMovementY / steps, gridComponent, motionComponent, collisionComponent, collisionBox, debugShapesComponent)

                i++
            }
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

//        gridComponent.zr += motionComponent.velocityZ
//        if (gridComponent.zr > 0 && gravityComponent != null) {
//            motionComponent.velocityZ -= gravityComponent.calculateDeltaZGravity()
//        }
//        if (gridComponent.zr < 0) {
//            gridComponent.zr = 0f
//            motionComponent.velocityZ = 0f
//            entity.configure {
//                it += gridCollisionZComponent {
//                    dir = 0
//                }
//            }
//        }
//        motionComponent.velocityZ *= motionComponent.frictionZ
//        if (motionComponent.velocityZ.isAlmostEquals(0.0005f)) {
//            motionComponent.velocityZ = 0f
//        }

//        positionComponent.x = grid.x
//        positionComponent.y = grid.y
//        println("GridMoveSystem: cx, cy: ${gridComponent.cx}, ${gridComponent.cy} xr, yr: ${gridComponent.xr}, ${gridComponent.yr}")

        checkPlayfieldBoundaries(gridComponent)
    }

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        //println("GridMoveSystem: onAlphaEntity: ${entity.id} alpha: $alpha")
        entity[GridComponent].interpolationAlpha = alpha
    }

    /**
     * Check for horizontal collisions and resolve them if necessary.
     * The entity is moved in the X direction and the collision is checked.
     * If a collision is detected, it is resolved and the collision flags are set accordingly.
     *
     * @param movement The amount of movement in pixels to check for collisions.
     * @param gridComponent The grid component of the entity.
     * @param motionComponent The motion component of the entity.
     * @param collisionComponent The collision component of the entity.
     * @param collisionBox The collision data for the entity.
     * @param debugShapesComponent Optional debug shapes component for visual debugging.
     *
     * @see CollisionChecker.checkXCollision
     * @see CollisionResolver.resolveXCollision
     * @see DebugCollisionShapes
     * @see AssetStore.CollisionData
     */
    private fun checkCollisionHorizontally(
        movement: Float,
        gridComponent: Grid,
        motionComponent: Motion,
        collisionComponent: Collision,
        collisionBox: CollisionData,
        debugShapesComponent: DebugCollisionShapes?
    ) {
        // Move the entity in the X direction
        gridComponent.xr += movement / AppConfig.GRID_CELL_SIZE

        if (motionComponent.velocityX != 0f) {
            // Check if collision happens within the next grid cell where the entity is moving
            val result = collisionChecker.checkXCollision(
                gridComponent.cx,  // Position of entity (pivot point)
                gridComponent.cy,
                gridComponent.xr,
                gridComponent.yr,
                motionComponent.velocityX,
                motionComponent.velocityY,
                collisionBox,
                debugShapesComponent
            )
            if (result != 0) {
                collisionResolver.resolveXCollision(gridComponent, motionComponent, collisionBox, result)
                if (result == 1) collisionComponent.right = true
                else if (result == -1) collisionComponent.left = true
            }
        }
        // Normalize grid coordinates - adjust the xr value to ensure it stays within the grid cell bounds
        while (gridComponent.xr > 1) {
            gridComponent.xr--
            gridComponent.cx++
        }
        while (gridComponent.xr < 0) {
            gridComponent.xr++
            gridComponent.cx--
        }
    }

    /**
     * Check for vertical collisions and resolve them if necessary.
     * The entity is moved in the Y direction and the collision is checked.
     * If a collision is detected, it is resolved and the collision flags are set accordingly.
     *
     * @param movement The amount of movement in pixels to check for collisions.
     * @param gridComponent The grid component of the entity.
     * @param motionComponent The motion component of the entity.
     * @param collisionComponent The collision component of the entity.
     * @param collisionBox The collision data for the entity.
     * @param debugShapesComponent Optional debug shapes component for visual debugging.
     *
     * @see CollisionChecker.checkYCollision
     * @see CollisionResolver.resolveYCollision
     * @see DebugCollisionShapes
     * @see AssetStore.CollisionData
     */
    fun checkCollisionVertically(
        movement: Float,
        gridComponent: Grid,
        motionComponent: Motion,
        collisionComponent: Collision,
        collisionBox: CollisionData,
        debugShapesComponent: DebugCollisionShapes?
    ) {
        // Move the entity in the Y direction
        gridComponent.yr += movement / AppConfig.GRID_CELL_SIZE

        if (motionComponent.velocityY != 0f) {
            // Check if collision happens within the next grid cell where the entity is moving
            val result = collisionChecker.checkYCollision(
                gridComponent.cx,  // Position of entity (pivot point)
                gridComponent.cy,
                gridComponent.xr,
                gridComponent.yr,
                motionComponent.velocityX,
                motionComponent.velocityY,
                collisionBox,
                debugShapesComponent
            )
            if (result != 0) {
                collisionResolver.resolveYCollision(gridComponent, motionComponent, collisionBox, result)
                if (result == 1) collisionComponent.isGrounded = true
                else if (result == -1) collisionComponent.isCollidingAbove = true
            }
        }
        // Normalize grid coordinates - adjust the yr value to ensure it stays within the grid cell bounds
        while (gridComponent.yr > 1) {
            gridComponent.yr--
            gridComponent.cy++
        }
        while (gridComponent.yr < 0) {
            gridComponent.yr++
            gridComponent.cy--
        }
    }

    /**
     * Check if the object is within the playfield boundaries.
     * If the object is outside the boundaries, it is moved back to the nearest boundary.
     * If the object falls off the playfield at the bottom, the object is considered dead.
     *
     * @param gridComponent The grid component of the entity to check.
     */
    private fun checkPlayfieldBoundaries(gridComponent: Grid) {
        // Keep the player sprite inside the level
        if (gridComponent.x <= 16f) gridComponent.x = 16f
        else if (gridComponent.x >= Prefab.levelData.width - 16f) gridComponent.x = Prefab.levelData.width - 16f

        // Check if player falls off the playfield at the bottom
        if (gridComponent.y > Prefab.levelData.height) {
            // TODO: handle death of object
            println("ERROR: PositionSystem - Player falls off the playfield!")
        }
    }
}
