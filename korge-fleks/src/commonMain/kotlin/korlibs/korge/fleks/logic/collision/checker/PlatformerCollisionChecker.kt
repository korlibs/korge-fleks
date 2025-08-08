package korlibs.korge.fleks.logic.collision.checker

import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.data.Point.Companion.point
import korlibs.korge.fleks.logic.collision.GridPosition
import korlibs.korge.fleks.prefab.Prefab
import korlibs.korge.fleks.utils.AppConfig
import korlibs.korge.fleks.utils.DebugPointPool
import kotlin.math.ceil

class PlatformerCollisionChecker(
    private val debugPointPool: DebugPointPool
//    private var level: LevelData
) : CollisionChecker() {
    var rightCollisionRatio: Float = 1f
    var leftCollisionRatio: Float = 0f
    var bottomCollisionRatio: Float = 1f
    var topCollisionRatio: Float = 0f
    var useTopCollisionRatio: Boolean = true

    private val grid = GridPosition()
    private val level = Prefab.levelData

    override fun checkXCollision(
        cx: Int,
        cy: Int,
        xr: Float,
        yr: Float,
        velocityX: Float,
        velocityY: Float,
        collisionBox: AssetStore.CollisionData
    ): Int {
        // Check direction of movement
        if (velocityX > 0f) {  // Moving right
            // First we need to calculate the top right point of the collision box
            // Add offset to pivot point and width of the collision box
            val xxr = xr + (collisionBox.x + collisionBox.width) / AppConfig.GRID_CELL_SIZE

            // Check collision in right cell and if xr is greater than 1 (xr is over cell bounds)
            if (level.hasCollision(cx + 1, cy) && xxr >= rightCollisionRatio) {
                return 1
            }

            // Start checking at right top edge of the collision rectangle
//                grid.applyOnX(width)

//                repeat((width / cellSize).roundToInt()) { i ->
//                    if (level.hasCollision(grid.cx + 1, grid.cy + i) && xr >= rightCollisionRatio) {
//                        return 1
//                    }
//                }
                // Check right bottom edge of the collision rectangle
//                grid.applyOnY(height)
//                if (level.hasCollision(grid.cx + 1, grid.cy) && xr >= rightCollisionRatio) {
//                    return 1
//                }


        } else if (velocityX < 0f) {  // Moving left
            // First we need to calculate the top left point of the collision box
            // Add offset to pivot point
            val xxr = xr + collisionBox.x / AppConfig.GRID_CELL_SIZE

            // Check collision in left cell and if xr is lower than 0 (xr is over cell bounds in negative direction)
            if (level.hasCollision(cx - 1, cy) && xxr <= leftCollisionRatio) {
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
        collisionBox: AssetStore.CollisionData
    ): Int {
        val xrRight = xr + (collisionBox.x + collisionBox.width) / AppConfig.GRID_CELL_SIZE
        val xrLeft = xr + collisionBox.x / AppConfig.GRID_CELL_SIZE
        //val yrBottom = yr + (collisionBox.y + collisionBox.height) / AppConfig.GRID_CELL_SIZE
        //val yrTop = yr + collisionBox.y / AppConfig.GRID_CELL_SIZE

        // Check direction of movement
        if (velocityY > 0f) {  // Moving down
            val yrBottom = yr + (collisionBox.y + collisionBox.height) / AppConfig.GRID_CELL_SIZE
            // Cell coordinates of left corner of the collision box
            grid.setAndNormalizeX(cx, xrLeft)  // Left corner of the collision box

            // Store collision checker positions for debug rendering
            // TODO
            val point = point {
                x = grid.cx.toFloat()
                y = (cy + 1).toFloat()
            }
            point.free()


            val i = 0
            // Check collision in bottom cell and if yr is greater than 1 (yr is over cell bounds)
//            repeat( ceil(collisionBox.width / AppConfig.GRID_CELL_SIZE).toInt()) { i ->
//                if (level.hasCollision(grid.cx + i, cy)) return 2  // Check the current Y cell
                if (level.hasCollision(grid.cx + i, cy + 1) && yrBottom >= bottomCollisionRatio) {  // Check tne next Y cell
                    return 1
                }
//            }
            // Cell coordinates of right corner of the collision box
            grid.setAndNormalizeX(cx, xrRight)  // Right corner of the collision box
            if (level.hasCollision(grid.cx, cy + 1) && yrBottom >= bottomCollisionRatio) {
                return 1
            }

        } else if (velocityY < 0f) {  // Moving up
            val yrTop = yr + collisionBox.y / AppConfig.GRID_CELL_SIZE
            // Cell coordinates of left corner of the collision box
            grid.setAndNormalizeX(cx, xrLeft)  // Left corner of the collision box

            // Check collision in bottom cell and if yr is greater than 1 (yr is over cell bounds)
            repeat( ceil(collisionBox.width / AppConfig.GRID_CELL_SIZE).toInt()) { i ->
                if (level.hasCollision(grid.cx + i, cy + 1) && yrTop >= topCollisionRatio) {
                    return -1
                }
            }
            // Cell coordinates of right corner of the collision box
            grid.setAndNormalizeX(cx, xrRight)  // Right corner of the collision box
            if (level.hasCollision(grid.cx, cy - 1) && yrTop >= topCollisionRatio) {
                return -1
            }
        }
        return 0
    }
}
