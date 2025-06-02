package korlibs.korge.fleks.logic.collision.checker

class PlatformerCollisionChecker {
}

/**
 *
 */
class SimpleCollisionChecker(val gridWidth: Int, val gridHeight: Int) : CollisionChecker() {
    override fun checkXCollision(
        cx: Int,
        cy: Int,
        xr: Float,
        yr: Float,
        velocityX: Float,
        velocityY: Float,
        width: Float,
        height: Float,
        cellSize: Float
    ): Int {
        if (cx - 1 < 0 && xr <= 0.3f) {
            return -1
        }
        if (cx + 1 > gridWidth && xr >= 0.7f) {
            return 1
        }
        return 0
    }

    override fun checkYCollision(
        cx: Int,
        cy: Int,
        xr: Float,
        yr: Float,
        velocityX: Float,
        velocityY: Float,
        width: Float,
        height: Float,
        cellSize: Float
    ): Int {
        if (cy - 1 < 0 && yr <= 0.3f) {
            return -1
        }
        if (cy + 1 > gridHeight && yr >= 0.7f) {
            return 1
        }
        return 0
    }
}
