package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.Collision.Companion.CollisionComponent
import korlibs.korge.fleks.components.Gravity.Companion.GravityComponent
import korlibs.korge.fleks.components.Grid
import korlibs.korge.fleks.components.Grid.Companion.GridComponent
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.collision.GridCollisionResult.Companion.gridCollisionXComponent
import korlibs.korge.fleks.components.collision.GridCollisionResult.Companion.gridCollisionYComponent
import korlibs.korge.fleks.components.collision.GridCollisionResult.Companion.gridCollisionZComponent
import korlibs.korge.fleks.logic.collision.checker.CollisionChecker
import korlibs.korge.fleks.logic.collision.checker.PlatformerCollisionChecker
import korlibs.korge.fleks.logic.collision.resolver.CollisionResolver
import korlibs.korge.fleks.logic.collision.resolver.SimpleCollisionResolver
import korlibs.korge.fleks.tags.RenderLayerTag.MAIN_LEVELMAP
import korlibs.korge.fleks.utils.AppConfig
import korlibs.math.isAlmostEquals
import kotlin.math.abs
import kotlin.math.ceil


class GridMoveSystem(
) : IteratingSystem(
    family = World.family { all(PositionComponent, GridComponent, MotionComponent, CollisionComponent) /*.any(LevelMapComponent)*/ },
    interval = Fixed(1 / 30f)
) {
    val assetStore = world.inject<AssetStore>("AssetStore")

    var collisionChecker: CollisionChecker = PlatformerCollisionChecker()
    var collisionResolver: CollisionResolver = SimpleCollisionResolver(16, 16)

    override fun onTickEntity(entity: Entity) {
        // Iterate over all entities that have a GridComponent and MotionComponent
        val positionComponent = entity[PositionComponent]
        val motionComponent = entity[MotionComponent]
        val gridComponent = entity[GridComponent]
        val collisionComponent = entity[CollisionComponent]
        val assetStore = world.inject<AssetStore>(name = "AssetStore")
        val collisionData = assetStore.getCollisionData(collisionComponent.name)
        val gravityComponent = entity.getOrNull(GravityComponent)
        gridComponent.lastPx = gridComponent.attachX
        gridComponent.lastPy = gridComponent.attachY
        if (gravityComponent != null) {
// TODO enable gravity again
//            motionComponent.velocityX += gravityComponent.calculateDeltaXGravity()
//            motionComponent.velocityY += gravityComponent.calculateDeltaYGravity()
        }
        /**
         * Any movement greater than [Grid.maxGridMovementPercent] will increase the number of steps here.
         * The steps will break down the movement into smaller iterators to avoid jumping over grid collisions
         */
        val steps = ceil(abs(motionComponent.velocityX) + abs(motionComponent.velocityY) / AppConfig.maxGridMovementPercent)
        if (steps > 0) {
            var i = 0
            while (i < steps) {
                // Move the entity in the X direction
                gridComponent.xr += motionComponent.velocityX / steps
                if (motionComponent.velocityX != 0f) {
                    collisionChecker.preXCheck(
                        gridComponent.cx,
                        gridComponent.cy,
                        gridComponent.xr,
                        gridComponent.yr,
                        motionComponent.velocityX,
                        motionComponent.velocityY,
                        collisionData.width,
                        collisionData.height,
                        AppConfig.GRID_CELL_SIZE
                    )
                    // Check if collision happens within the next grid cell where the entity is moving
                    val result = collisionChecker.checkXCollision(
                        gridComponent.cx,
                        gridComponent.cy,
                        gridComponent.xr,
                        gridComponent.yr,
                        motionComponent.velocityX,
                        motionComponent.velocityY,
                        collisionData.width,
                        collisionData.height,
                        AppConfig.GRID_CELL_SIZE
                    )
                    if (result != 0) {
                        collisionResolver.resolveXCollision(gridComponent, motionComponent, collisionChecker, result)
                        entity.configure {
                            it += gridCollisionXComponent {
                                dir = result
                            }
                        }
                    }
                }
                // Adjust the xr value to ensure it stays within the grid cell bounds
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
                    collisionChecker.preYCheck(
                        gridComponent.cx,
                        gridComponent.cy,
                        gridComponent.xr,
                        gridComponent.yr,
                        motionComponent.velocityX,
                        motionComponent.velocityY,
                        collisionData.width,
                        collisionData.height,
                        AppConfig.GRID_CELL_SIZE
                    )
                    val result = collisionChecker.checkYCollision(
                        gridComponent.cx,
                        gridComponent.cy,
                        gridComponent.xr,
                        gridComponent.yr,
                        motionComponent.velocityX,
                        motionComponent.velocityY,
                        collisionData.width,
                        collisionData.height,
                        AppConfig.GRID_CELL_SIZE
                    )
                    if (result != 0) {
                        collisionResolver.resolveYCollision(gridComponent, motionComponent, collisionChecker, result)
                        entity.configure {
                            it += gridCollisionYComponent {
                                dir = result
                            }
                        }
                    }
                }
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
        motionComponent.velocityY *= motionComponent.frictionY
        if (motionComponent.velocityY.isAlmostEquals(0.0005f)) {
            motionComponent.velocityY = 0f
        }
        gridComponent.zr += motionComponent.velocityZ
        if (gridComponent.zr > 0 && gravityComponent != null) {
            motionComponent.velocityZ -= gravityComponent.calculateDeltaZGravity()
        }
        if (gridComponent.zr < 0) {
            gridComponent.zr = 0f
            motionComponent.velocityZ = 0f
            entity.configure {
                it += gridCollisionZComponent {
                    dir = 0
                }
            }
        }
        motionComponent.velocityZ *= motionComponent.frictionZ
        if (motionComponent.velocityZ.isAlmostEquals(0.0005f)) {
            motionComponent.velocityZ = 0f
        }

        positionComponent.x = gridComponent.x
        positionComponent.y = gridComponent.y
//        println("GridMoveSystem: cx, cy: ${gridComponent.cx}, ${gridComponent.cy} xr, yr: ${gridComponent.xr}, ${gridComponent.yr}")
    }

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        entity[GridComponent].interpolationAlpha = alpha
    }
}
