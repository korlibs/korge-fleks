package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.Collision.Companion.CollisionComponent
import korlibs.korge.fleks.components.Gravity.Companion.GravityComponent
import korlibs.korge.fleks.components.Grid
import korlibs.korge.fleks.components.Grid.Companion.GridComponent
import korlibs.korge.fleks.components.MotionComponent
import korlibs.korge.fleks.logic.collision.checker.CollisionChecker
import korlibs.korge.fleks.logic.collision.resolver.CollisionResolver
import korlibs.korge.fleks.utils.AppConfig
import kotlin.math.abs
import kotlin.math.ceil

class GridMoveSystem(
    private val collisionChecker: CollisionChecker,
    private val collisionResolver: CollisionResolver
) : IteratingSystem(
    family = World.family { all(MotionComponent, GridComponent, CollisionComponent) },
    interval = Fixed(1 / 60f)
) {

    override fun onTickEntity(entity: Entity) {
        val motionComponent = entity[MotionComponent]
        val gridComponent = entity[GridComponent]
        val collisionComponent = entity[CollisionComponent]

        val gravity = entity.getOrNull(GravityComponent)
//        val collision = entity.getOrNull(GridCollision)
//        val resolver = if (collision != null) entity.getOrNull(GridCollisionResolver) else null

        gridComponent.lastPx = gridComponent.attachX
        gridComponent.lastPy = gridComponent.attachY

        if (gravity != null) {
//            moveComponent.velocityX += gravity.calculateDeltaXGravity()
            motionComponent.velocityY += gravity.calculateDeltaYGravity()
        }
//*
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
                        collisionComponent.width,
                        collisionComponent.height,
                        AppConfig.gridCellSize
                    )
                    val result = collisionChecker.checkXCollision(
                        gridComponent.cx,
                        gridComponent.cy,
                        gridComponent.xr,
                        gridComponent.yr,
                        motionComponent.velocityX,
                        motionComponent.velocityY,
                        gridComponent.width,
                        gridComponent.height,
                        gridComponent.gridCellSize
                    )
                    if (result != 0) {
                        collisionResolver.resolveXCollision(gridComponent, motionComponent, collisionChecker, result)
                        entity.configure {
                            it += GridCollisionResult.GridCollisionXPool.alloc(world).apply {
                                axes = GridCollisionResult.Axes.X
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

                if (collision != null) {
                    if (motionComponent.velocityY != 0f) {
                        collision.checker.preYCheck(
                            gridComponent.cx,
                            gridComponent.cy,
                            gridComponent.xr,
                            gridComponent.yr,
                            motionComponent.velocityX,
                            motionComponent.velocityY,
                            gridComponent.width,
                            gridComponent.height,
                            gridComponent.gridCellSize
                        )
                        val result = collision.checker.checkYCollision(
                            gridComponent.cx,
                            gridComponent.cy,
                            gridComponent.xr,
                            gridComponent.yr,
                            motionComponent.velocityX,
                            motionComponent.velocityY,
                            gridComponent.width,
                            gridComponent.height,
                            gridComponent.gridCellSize
                        )
                        if (result != 0) {
                            resolver?.resolver?.resolveYCollision(gridComponent, motionComponent, collision, result)
                            entity.configure {
                                it += GridCollisionResult.GridCollisionYPool.alloc(world).apply {
                                    axes = GridCollisionResult.Axes.Y
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
        if (motionComponent.velocityX.isFuzzyZero(0.0005f)) {
            motionComponent.velocityX = 0f
        }

        motionComponent.velocityY *= motionComponent.frictionY
        if (motionComponent.velocityY.isFuzzyZero(0.0005f)) {
            motionComponent.velocityY = 0f
        }

        gridComponent.zr += motionComponent.velocityZ

        if (gridComponent.zr > 0 && gravity != null) {
            motionComponent.velocityZ -= gravity.calculateDeltaZGravity()
        }

        if (gridComponent.zr < 0) {
            gridComponent.zr = 0f
            motionComponent.velocityZ = 0f
            entity.configure {
                it += GridCollisionResult.GridCollisionZPool.alloc(world).apply {
                    axes = GridCollisionResult.Axes.Z
                    dir = 0
                }
            }

        }

        motionComponent.velocityZ *= motionComponent.frictionZ
        if (motionComponent.velocityZ.isFuzzyZero(0.0005f)) {
            motionComponent.velocityZ = 0f
        }
// */
    }

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
//        entity[Grid].interpolationAlpha = alpha
    }
}