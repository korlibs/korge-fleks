package korlibs.korge.fleks.logic.collision.checker

import korlibs.korge.fleks.assets.data.gameObject.CollisionRect


abstract class GroundChecker {
    abstract fun onGround(
        cx: Int,
        cy: Int,
        xr: Double,
        yr: Double,
        velocityY: Double,
        collisionBox: CollisionRect
    ): Boolean
}
