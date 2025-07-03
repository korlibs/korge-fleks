package korlibs.korge.fleks.logic.collision.checker

import korlibs.korge.fleks.prefab.Prefab
import korlibs.korge.fleks.prefab.data.LevelData
import kotlin.math.floor

class PlatformerCollisionChecker(
//    private var level: LevelData
) : CollisionChecker() {
    var rightCollisionRatio: Float = 0.7f
    var leftCollisionRatio: Float = 0.3f
    var bottomCollisionRatio: Float = 0f
    var topCollisionRatio: Float = 1f
    var useTopCollisionRatio: Boolean = false

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
        Prefab.levelData?.let { level ->
            if (level.hasCollision(cx + 1, cy) && xr >= rightCollisionRatio) {
                return 1
            }
            if (level.hasCollision(cx - 1, cy) && xr <= leftCollisionRatio) {
                return -1
            }
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
        Prefab.levelData?.let { level ->
            val heightCoordDiff = if (useTopCollisionRatio) topCollisionRatio else floor(height / cellSize)
            if (level.hasCollision(cx, cy + 1) && yr >= heightCoordDiff) {
                return 1
            }
            if (level.hasCollision(cx, cy - 1) && yr <= bottomCollisionRatio) {
                return -1
            }
        }
        return 0
    }

    override fun hasCollision(cx: Int, cy: Int): Boolean = Prefab.levelData?.hasCollision(cx, cy) ?: false
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
        if (cy + 1 > 112) {  // && yr >= 0.7f) {
            return 1
        }
        return 0

        // 750 / 16 =

        // 110 * 16
    }
}
