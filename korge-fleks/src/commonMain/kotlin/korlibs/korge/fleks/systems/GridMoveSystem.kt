package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.Gravity.Companion.GravityComponent
import korlibs.korge.fleks.components.Grid.Companion.GridComponent
import korlibs.korge.fleks.components.MotionComponent
import korlibs.korge.fleks.logic.collision.checker.CollisionChecker
import korlibs.korge.fleks.logic.collision.resolver.CollisionResolver
import kotlin.math.abs
import kotlin.math.ceil


class GridMoveSystem(
    private val collisionChecker: CollisionChecker,
    private val collisionResolver: CollisionResolver
) : IteratingSystem(
    family = family { all(MotionComponent, GridComponent) },
    interval = Fixed(1 / 60f)
) {

    override fun onTickEntity(entity: Entity) {
        val moveComponent = entity[MotionComponent]
        val gridComponent = entity[GridComponent]

        val gravity = entity.getOrNull(GravityComponent)
//        val collision = entity.getOrNull(GridCollision)
//        val resolver = if (collision != null) entity.getOrNull(GridCollisionResolver) else null

        gridComponent.lastPx = gridComponent.attachX
        gridComponent.lastPy = gridComponent.attachY

        if (gravity != null) {
//            moveComponent.velocityX += gravity.calculateDeltaXGravity()
            moveComponent.velocityY += gravity.calculateDeltaYGravity()
        }
/*
        /**
         * Any movement greater than [Grid.maxGridMovementPercent] will increase the number of steps here.
         * The steps will break down the movement into smaller iterators to avoid jumping over grid collisions
         */
        val steps = ceil(abs(moveComponent.velocityX) + abs(moveComponent.velocityY) / gridComponent.maxGridMovementPercent)
        if (steps > 0) {
            var i = 0
            while (i < steps) {
                gridComponent.xr += moveComponent.velocityX / steps

                if (collision != null) {
                    if (moveComponent.velocityX != 0f) {
                        collision.checker.preXCheck(
                            gridComponent.cx,
                            gridComponent.cy,
                            gridComponent.xr,
                            gridComponent.yr,
                            moveComponent.velocityX,
                            moveComponent.velocityY,
                            gridComponent.width,
                            gridComponent.height,
                            gridComponent.gridCellSize
                        )
                        val result = collision.checker.checkXCollision(
                            gridComponent.cx,
                            gridComponent.cy,
                            gridComponent.xr,
                            gridComponent.yr,
                            moveComponent.velocityX,
                            moveComponent.velocityY,
                            gridComponent.width,
                            gridComponent.height,
                            gridComponent.gridCellSize
                        )
                        if (result != 0) {
                            resolver?.resolver?.resolveXCollision(gridComponent, moveComponent, collision, result)
                            entity.configure {
                                it += GridCollisionResult.GridCollisionXPool.alloc(world).apply {
                                    axes = GridCollisionResult.Axes.X
                                    dir = result
                                }
                            }

                        }
                    }
                }

                while (gridComponent.xr > 1) {
                    gridComponent.xr--
                    gridComponent.cx++
                }
                while (gridComponent.xr < 0) {
                    gridComponent.xr++
                    gridComponent.cx--
                }

                gridComponent.yr += moveComponent.velocityY / steps

                if (collision != null) {
                    if (moveComponent.velocityY != 0f) {
                        collision.checker.preYCheck(
                            gridComponent.cx,
                            gridComponent.cy,
                            gridComponent.xr,
                            gridComponent.yr,
                            moveComponent.velocityX,
                            moveComponent.velocityY,
                            gridComponent.width,
                            gridComponent.height,
                            gridComponent.gridCellSize
                        )
                        val result = collision.checker.checkYCollision(
                            gridComponent.cx,
                            gridComponent.cy,
                            gridComponent.xr,
                            gridComponent.yr,
                            moveComponent.velocityX,
                            moveComponent.velocityY,
                            gridComponent.width,
                            gridComponent.height,
                            gridComponent.gridCellSize
                        )
                        if (result != 0) {
                            resolver?.resolver?.resolveYCollision(gridComponent, moveComponent, collision, result)
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
        moveComponent.velocityX *= moveComponent.frictionX
        if (moveComponent.velocityX.isFuzzyZero(0.0005f)) {
            moveComponent.velocityX = 0f
        }

        moveComponent.velocityY *= moveComponent.frictionY
        if (moveComponent.velocityY.isFuzzyZero(0.0005f)) {
            moveComponent.velocityY = 0f
        }

        gridComponent.zr += moveComponent.velocityZ

        if (gridComponent.zr > 0 && gravity != null) {
            moveComponent.velocityZ -= gravity.calculateDeltaZGravity()
        }

        if (gridComponent.zr < 0) {
            gridComponent.zr = 0f
            moveComponent.velocityZ = 0f
            entity.configure {
                it += GridCollisionResult.GridCollisionZPool.alloc(world).apply {
                    axes = GridCollisionResult.Axes.Z
                    dir = 0
                }
            }

        }

        moveComponent.velocityZ *= moveComponent.frictionZ
        if (moveComponent.velocityZ.isFuzzyZero(0.0005f)) {
            moveComponent.velocityZ = 0f
        }
*/
    }

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
//        entity[Grid].interpolationAlpha = alpha
    }
}
