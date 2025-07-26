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
    /**
     * Pre-checks for X direction collision.#
     *
     * This method can be overridden to perform any necessary pre-checks before the actual X collision check.
     *
     * @param cx The current X coordinate in the grid.
     * @param cy The current Y coordinate in the grid.
     * @param xr The relative X position of the entity inside a grid cell.
     * @param yr The relative Y position of the entity insdie a grid cell.
     * @param velocityX The velocity in the X direction.
     * @param velocityY The velocity in the Y direction.
     * @param width The width of the entity.
     * @param height The height of the entity.
     * @param cellSize The size of each cell in the grid.
     */
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

    /**
     * Pre-checks for Y direction collision.
     *
     * This method can be overridden to perform any necessary pre-checks before the actual Y collision check.
     *
     * @param cx The current X coordinate in the grid.
     * @param cy The current Y coordinate in the grid.
     * @param xr The relative X position of the entity inside a grid cell.
     * @param yr The relative Y position of the entity inside a grid cell.
     * @param velocityX The velocity in the X direction.
     * @param velocityY The velocity in the Y direction.
     * @param width The width of the entity.
     * @param height The height of the entity.
     * @param cellSize The size of each cell in the grid.
     */
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

    /**
     * Checks for X direction collision.
     *
     * This method can be overridden to implement specific collision checking logic.
     *
     * @param cx The current X coordinate in the grid.
     * @param cy The current Y coordinate in the grid.
     * @param xr The relative X position of the entity inside a grid cell.
     * @param yr The relative Y position of the entity inside a grid cell.
     * @param velocityX The velocity in the X direction.
     * @param velocityY The velocity in the Y direction.
     * @param width The width of the entity.
     * @param height The height of the entity.
     * @param cellSize The size of each cell in the grid.
     * @return An integer indicating the collision result (e.g., 0 for no collision, 1 for right collision, -1 for left collision).
     */
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

    /**
     * Checks for Y direction collision.
     *
     * This method can be overridden to implement specific collision checking logic.
     *
     * @param cx The current X coordinate in the grid.
     * @param cy The current Y coordinate in the grid.
     * @param xr The relative X position of the entity inside a grid cell.
     * @param yr The relative Y position of the entity inside a grid cell.
     * @param velocityX The velocity in the X direction.
     * @param velocityY The velocity in the Y direction.
     * @param width The width of the entity.
     * @param height The height of the entity.
     * @param cellSize The size of each cell in the grid.
     * @return An integer indicating the collision result (e.g., 0 for no collision, 1 for bottom collision, -1 for top collision).
     */
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

    /**
     * Checks if there is a collision at the specified grid coordinates.
     */
    open fun hasCollision(cx: Int, cy: Int): Boolean = false
}
