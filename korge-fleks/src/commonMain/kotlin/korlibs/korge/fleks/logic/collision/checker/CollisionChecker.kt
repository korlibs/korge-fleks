package korlibs.korge.fleks.logic.collision.checker

/**
 * A base class for collision checking in a grid-based system.
 * This class provides methods to check for collisions in both X and Y directions,
 * as well as methods to perform pre-checks before the actual collision checks.
 *
 * @see [PlatformerCollisionChecker]
 * @see [SimpleCollisionChecker]
 */
open class CollisionChecker {
    open fun preXCheck(
        cx: Int,
        cy: Int,
        xr: Float,
        yr: Float,
        velocityX: Float,
        velocityY: Float,
        width: Float,
        height: Float,
        cellSize: Float
    ) = Unit

    open fun preYCheck(
        cx: Int,
        cy: Int,
        xr: Float,
        yr: Float,
        velocityX: Float,
        velocityY: Float,
        width: Float,
        height: Float,
        cellSize: Float
    ) = Unit

    open fun checkXCollision(
        cx: Int,
        cy: Int,
        xr: Float,
        yr: Float,
        velocityX: Float,
        velocityY: Float,
        width: Float,
        height: Float,
        cellSize: Float
    ): Int = 0

    open fun checkYCollision(
        cx: Int,
        cy: Int,
        xr: Float,
        yr: Float,
        velocityX: Float,
        velocityY: Float,
        width: Float,
        height: Float,
        cellSize: Float
    ): Int = 0

    open fun hasCollision(cx: Int, cy: Int): Boolean = false
}
