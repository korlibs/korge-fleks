package korlibs.korge.fleks.logic.collision.checker

import korlibs.korge.fleks.assets.data.gameObject.CollisionRect


abstract class GroundChecker {
    abstract fun onGround(
        cx: Int,
        cy: Int,
        xr: Float,
        yr: Float,
        velocityY: Float,
        collisionBox: CollisionRect
    ): Boolean
}