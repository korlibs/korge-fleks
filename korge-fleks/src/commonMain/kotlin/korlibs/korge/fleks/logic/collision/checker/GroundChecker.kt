package korlibs.korge.fleks.logic.collision.checker

import korlibs.korge.fleks.assets.data.gameObject.CollisionData


abstract class GroundChecker {
    abstract fun onGround(
        cx: Int,
        cy: Int,
        xr: Float,
        yr: Float,
        velocityY: Float,
        collisionBox: CollisionData
    ): Boolean
}