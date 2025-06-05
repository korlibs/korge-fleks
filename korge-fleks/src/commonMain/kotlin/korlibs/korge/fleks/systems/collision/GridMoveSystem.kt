package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.assets.WorldData
import korlibs.korge.fleks.components.Collision.Companion.CollisionComponent
import korlibs.korge.fleks.components.Gravity.Companion.GravityComponent
import korlibs.korge.fleks.components.Grid
import korlibs.korge.fleks.components.Grid.Companion.GridComponent
import korlibs.korge.fleks.components.LevelMap.Companion.LevelMapComponent
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.collision.GridCollisionResult.Companion.GridCollisionYComponent
import korlibs.korge.fleks.components.collision.GridCollisionResult.Companion.GridCollisionZComponent
import korlibs.korge.fleks.components.collision.GridCollisionResult.Companion.gridCollisionXComponent
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
) : IntervalSystem(
    interval = Fixed(1 / 30f)
) {
    private val levelFamily = world.family { all(MAIN_LEVELMAP, LevelMapComponent) }
    private val entityFamily = World.family { all(MotionComponent, GridComponent).any(GridComponent, CollisionComponent) }

    val assetStore = world.inject<AssetStore>("AssetStore")
    var levelData: WorldData = assetStore.getWorldData(levelName)
    var collisionChecker: CollisionChecker = PlatformerCollisionChecker(levelData)
    var collisionResolver: CollisionResolver = SimpleCollisionResolver(16, 16)

    override fun onTick() {
        // Run this system only if there is a level map in the world
        levelFamily.firstOrNull()?.let { levelEntity ->
            val levelMapComponent = levelEntity[LevelMapComponent]
            // Iterate over all entities that have a GridComponent and MotionComponent
            entityFamily.forEach { entity ->
                val motionComponent = entity[MotionComponent]
                val gridComponent = entity[GridComponent]

                val collisionData = if (entity has CollisionComponent) {
                    val collisionComponent = entity[CollisionComponent]
                    val assetStore = world.inject<AssetStore>(name = "AssetStore")
                    assetStore.getCollisionData(collisionComponent.name)
                } else null

                val gravityComponent = entity.getOrNull(GravityComponent)

                gridComponent.lastPx = gridComponent.attachX
                gridComponent.lastPy = gridComponent.attachY

                if (gravityComponent != null) {
                    motionComponent.velocityX += gravityComponent.calculateDeltaXGravity()
                    motionComponent.velocityY += gravityComponent.calculateDeltaYGravity()
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

                        if (collisionData != null) {
                            if (motionComponent.velocityX != 0f) {
                                levelMapComponent.collisionChecker.preXCheck(
                                    gridComponent.cx,
                                    gridComponent.cy,
                                    gridComponent.xr,
                                    gridComponent.yr,
                                    motionComponent.velocityX,
                                    motionComponent.velocityY,
                                    collisionData.width,
                                    collisionData.height,
                                    AppConfig.gridCellSize
                                )
                                // Check if collision happens within a grid cell
                                val result = levelMapComponent.collisionChecker.checkXCollision(
                                    gridComponent.cx,
                                    gridComponent.cy,
                                    gridComponent.xr,
                                    gridComponent.yr,
                                    motionComponent.velocityX,
                                    motionComponent.velocityY,
                                    collisionData.width,
                                    collisionData.height,
                                    AppConfig.gridCellSize
                                )
                                if (result != 0) {
                                    levelMapComponent.collisionResolver.resolveXCollision(gridComponent, motionComponent, levelMapComponent.collisionChecker, result)
                                    entity.configure {
                                        it += world.gridCollisionXComponent {
                                            dir = result
                                        }
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

                        if (collisionData != null) {
                            if (motionComponent.velocityY != 0f) {
                                levelMapComponent.collisionChecker.preYCheck(
                                    gridComponent.cx,
                                    gridComponent.cy,
                                    gridComponent.xr,
                                    gridComponent.yr,
                                    motionComponent.velocityX,
                                    motionComponent.velocityY,
                                    collisionData.width,
                                    collisionData.height,
                                    AppConfig.gridCellSize
                                )
                                val result = levelMapComponent.collisionChecker.checkYCollision(
                                    gridComponent.cx,
                                    gridComponent.cy,
                                    gridComponent.xr,
                                    gridComponent.yr,
                                    motionComponent.velocityX,
                                    motionComponent.velocityY,
                                    collisionData.width,
                                    collisionData.height,
                                    AppConfig.gridCellSize
                                )
                                if (result != 0) {
                                    levelMapComponent.collisionResolver.resolveYCollision(gridComponent, motionComponent, levelMapComponent.collisionChecker, result)
                                    entity.configure {
                                        it += world.GridCollisionYComponent {
                                            dir = result
                                        }
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
                        it += world.GridCollisionZComponent {
                            dir = 0
                        }
                    }
                }

                motionComponent.velocityZ *= motionComponent.frictionZ
                if (motionComponent.velocityZ.isAlmostEquals(0.0005f)) {
                    motionComponent.velocityZ = 0f
                }

//        println("GridMoveSystem: cx, cy: ${gridComponent.cx}, ${gridComponent.cy} xr, yr: ${gridComponent.xr}, ${gridComponent.yr}")
            }
        }
    }

    override fun onAlpha(alpha: Float) {
        entityFamily.forEach { entity ->
            entity[GridComponent].interpolationAlpha = alpha
        }
    }
}
