package korlibs.korge.fleks.logic.collision.checker

import korlibs.korge.fleks.prefab.Prefab
import korlibs.korge.fleks.prefab.data.LevelData
import korlibs.korge.fleks.systems.collision.CollisionGrid
import kotlin.math.floor
import kotlin.math.roundToInt

class PlatformerCollisionChecker(
//    private var level: LevelData
) : CollisionChecker() {
    var rightCollisionRatio: Float = 1f
    var leftCollisionRatio: Float = 0f
    var bottomCollisionRatio: Float = 0f
    var topCollisionRatio: Float = 1f
    var useTopCollisionRatio: Boolean = true

    override fun checkXCollision(
        collisionGrid: CollisionGrid,
        velocityX: Float,
        velocityY: Float,
//        width: Int,
//        height: Int,
        cellSize: Float
    ): Int {
        Prefab.levelData?.let { level ->
            repeat(((collisionGrid.cxBottomRight - collisionGrid.cxTopRight) / cellSize).roundToInt()) { i ->
                if (level.hasCollision(collisionGrid.cxTopRight + 1, collisionGrid.cyTopRight + i) && collisionGrid.xrTopRight >= rightCollisionRatio) {
                    return 1
                }
//                if (level.hasCollision(cx - 1, cy + i) && xr <= leftCollisionRatio) {
//                    return -1
//                }
            }
            if (level.hasCollision(collisionGrid.cxBottomRight + 1, collisionGrid.cyBottomRight) && collisionGrid.xrBottomRight >= rightCollisionRatio) {
                return 1
            }

        }
        return 0
    }

    override fun checkYCollision(
        collisionGrid: CollisionGrid,
        velocityX: Float,
        velocityY: Float,
//        width: Int,
//        height: Int,
//        cellSize: Float
    ): Int {
        Prefab.levelData?.let { level ->
//            val heightCoordDiff = if (useTopCollisionRatio) topCollisionRatio else floor(height / cellSize)
//            if (level.hasCollision(cx, cy + 1) && yr >= heightCoordDiff) {
//                return 1
//            }
//            if (level.hasCollision(cx, cy - 1) && yr <= bottomCollisionRatio) {
//                return -1
//            }
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
        collisionGrid: CollisionGrid,
        velocityX: Float,
        velocityY: Float,
//        width: Int,
//        height: Int,
        cellSize: Float
    ): Int {
//        if (cx - 1 < 0 && xr <= 0.3f) {
//            return -1
//        }
//        if (cx + 1 > gridWidth && xr >= 0.7f) {
//            return 1
//        }
        return 0
    }

    override fun checkYCollision(
        collisionGrid: CollisionGrid,
        velocityX: Float,
        velocityY: Float,
//        width: Int,
//        height: Int,
//        cellSize: Float
    ): Int {
//        if (cy - 1 < 0 && yr <= 0.3f) {
//            return -1
//        }
//        if (cy + 1 > 112) {  // && yr >= 0.7f) {
//            return 1
//        }
        return 0

        // 750 / 16 =

        // 110 * 16
    }
}
