package korlibs.korge.fleks.logic.collision.checker

abstract class GroundChecker {

    abstract fun onGround(
        velocityY: Float,
        cx: Int,
        cy: Int,
        xr: Float,
        yr: Float,
        collisionChecker: CollisionChecker
    ): Boolean
}