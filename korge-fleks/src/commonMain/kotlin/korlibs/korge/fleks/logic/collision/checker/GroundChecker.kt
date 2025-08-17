package korlibs.korge.fleks.logic.collision.checker

import korlibs.korge.fleks.assets.AssetStore


abstract class GroundChecker {
    abstract fun onGround(
        cx: Int,
        cy: Int,
        xr: Float,
        yr: Float,
        velocityY: Float,
        collisionBox: AssetStore.CollisionData
    ): Boolean
}