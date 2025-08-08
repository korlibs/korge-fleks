package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.Collision.Companion.CollisionComponent
import korlibs.korge.fleks.components.Gravity.Companion.GravityComponent
import korlibs.korge.fleks.components.Grid
import korlibs.korge.fleks.components.Grid.Companion.GridComponent
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.collision.GridCollisionResult.Companion.gridCollisionXComponent
import korlibs.korge.fleks.components.collision.GridCollisionResult.Companion.gridCollisionYComponent
import korlibs.korge.fleks.logic.collision.checker.CollisionChecker
import korlibs.korge.fleks.logic.collision.checker.PlatformerCollisionChecker
import korlibs.korge.fleks.logic.collision.resolver.CollisionResolver
import korlibs.korge.fleks.logic.collision.resolver.PlatformerCollisionResolver
import korlibs.korge.fleks.utils.AppConfig
import korlibs.korge.fleks.utils.DebugPointPool
import korlibs.math.isAlmostEquals
import kotlin.math.abs
import kotlin.math.ceil


class GridMoveSystem : IteratingSystem(
    family = World.family { all(GridComponent, MotionComponent, CollisionComponent) /*.any(LevelMapComponent)*/ },
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
        val assetStore = world.inject<AssetStore>(name = "AssetStore")
        val collisionBox = assetStore.getCollisionData(collisionComponent.name)
        val gravityComponent = entity.getOrNull(GravityComponent)

        // Apply gravity to the entity if it has a GravityComponent
        if (gravityComponent != null) {
// TODO enable gravity again
//            motionComponent.velocityX += gravityComponent.calculateDeltaXGravity()
            motionComponent.velocityY += gravityComponent.calculateDeltaYGravity()
        }

        // Set the last pixel position to the current grid position
        gridComponent.lastPx = gridComponent.attachX
        gridComponent.lastPy = gridComponent.attachY

        /**
         * Any movement greater than [Grid.maxGridMovementPercent] will increase the number of steps here.
         * The steps will break down the movement into smaller iterators to avoid jumping over grid collisions
         */
        val overallMovementX = motionComponent.velocityX * deltaTime
        val overallMovementY = motionComponent.velocityY * deltaTime
        // Calculate the number of steps needed to move the entity in relation to the grid size (here 16x16 pixels)
        val steps = ceil((abs(overallMovementX) + abs(overallMovementY)) / AppConfig.GRID_CELL_SIZE)  // TODO for more steps within one grid cell:   / AppConfig.maxGridMovementPercent)
        if (steps > 0) {
            var i = 0
            while (i < steps) {
                // Move the entity in the X direction
                gridComponent.xr += overallMovementX / steps / AppConfig.GRID_CELL_SIZE

                if (motionComponent.velocityX != 0f) {

//                    collisionChecker.preXCheck(
//                        grid.cx,
//                        grid.cy,
//                        grid.xr,
//                        grid.yr,
//                        motionComponent.velocityX,
//                        motionComponent.velocityY,
//                        colWidth,
//                        colHeight,
//                        AppConfig.GRID_CELL_SIZE
//                    )

                    // Check if collision happens within the next grid cell where the entity is moving
                    val result = collisionChecker.checkXCollision(
                        gridComponent.cx,  // Position of entity (pivot point)
                        gridComponent.cy,
                        gridComponent.xr,
                        gridComponent.yr,
                        motionComponent.velocityX,
                        motionComponent.velocityY,
                        collisionBox
                    )
                    if (result != 0) {
//                        collisionResolver.resolveXCollision(grid, motionComponent, collisionChecker, result)
                        collisionResolver.resolveXCollision(gridComponent, motionComponent, collisionBox, result)
                        entity.configure {
                            it += gridCollisionXComponent {
                                dir = result
                            }
                        }
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

                // Move the entity in the Y direction
                gridComponent.yr += motionComponent.velocityY / steps
                if (motionComponent.velocityY != 0f) {
/*
                    collisionChecker.preYCheck(
                        gridComponent.cx,  // Position of entity (pivot point)
                        gridComponent.cy,
                        gridComponent.xr,
                        gridComponent.yr,
                        motionComponent.velocityX,
                        motionComponent.velocityY,
                        collisionBox
                    )
*/
                    val result = collisionChecker.checkYCollision(
                        gridComponent.cx,  // Position of entity (pivot point)
                        gridComponent.cy,
                        gridComponent.xr,
                        gridComponent.yr,
                        motionComponent.velocityX,
                        motionComponent.velocityY,
                        collisionBox
                    )
                    if (result != 0) {
                        collisionResolver.resolveYCollision(gridComponent, motionComponent, collisionBox, result)
                        entity.configure {
                            it += gridCollisionYComponent {
                                dir = result
                            }
                        }
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

                i++
            }
        }
        motionComponent.velocityX *= motionComponent.frictionX
        if (motionComponent.velocityX.isAlmostEquals(0.0005f)) {
            motionComponent.velocityX = 0f
        }

        // Friction only in down direction (falling)
        if (motionComponent.velocityY > 0f) {
            motionComponent.velocityY *= motionComponent.frictionY
            if (motionComponent.velocityY.isAlmostEquals(0.0005f)) {
                motionComponent.velocityY = 0f
            }
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
    }

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        //println("GridMoveSystem: onAlphaEntity: ${entity.id} alpha: $alpha")
        entity[GridComponent].interpolationAlpha = alpha
    }
}
