package korlibs.korge.fleks.logic.collision.checker

import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.data.Point.Companion.point
import korlibs.korge.fleks.logic.collision.GridPosition
import korlibs.korge.fleks.prefab.Prefab
import korlibs.korge.fleks.utils.AppConfig
import korlibs.korge.fleks.utils.DebugPointPool
import kotlin.math.abs
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
        var dir = 0
        val yrTop = yr + collisionBox.y / AppConfig.GRID_CELL_SIZE
        val yrBottom = yr + (collisionBox.y + collisionBox.height) / AppConfig.GRID_CELL_SIZE

        // Check direction of movement
        if (velocityX > 0f) {  // Moving right
            val xrRight = xr + (collisionBox.x + collisionBox.width) / AppConfig.GRID_CELL_SIZE

            val checkDistance = ceil(abs(collisionBox.x + collisionBox.width) / AppConfig.GRID_CELL_SIZE).toInt()

            debugSaveRatioPoint(cx, cy, xrRight, yr)

            // Cell coordinates of top corner of the collision box
            grid.setAndNormalizeY(cy, yrTop)  // Top corner of the collision box

            // Check collision in bottom cell and if yr is greater than 1 (yr is over cell bounds)
            repeat( ceil(collisionBox.height / AppConfig.GRID_CELL_SIZE).toInt()) { i ->
                debugSaveGridCell(cx + checkDistance, grid.cy + i)

                if (level.hasCollision(cx + checkDistance, grid.cy + i) && xrRight >= checkDistance) {  // Check the next X cell
                    dir = 1
                }
            }
            // Cell coordinates of bottom corner of the collision box
            grid.setAndNormalizeY(cy, yrBottom)  // Bottom corner of the collision box
            debugSaveGridCell(cx + checkDistance, grid.cy)
            if (level.hasCollision(cx + checkDistance, grid.cy) && xrRight >= checkDistance) {
                dir = 1
            }
        } else if (velocityX < 0f) {  // Moving left
            val xrLeft = xr + collisionBox.x / AppConfig.GRID_CELL_SIZE

            val checkDistance = ceil(abs(collisionBox.x) / AppConfig.GRID_CELL_SIZE).toInt()

            debugSaveRatioPoint(cx, cy, xrLeft, yr)

            // Cell coordinates of top corner of the collision box
            grid.setAndNormalizeY(cy, yrTop)  // Top corner of the collision box

            // Check collision in bottom cell and if yr is greater than 1 (yr is over cell bounds)
            repeat( ceil(collisionBox.height / AppConfig.GRID_CELL_SIZE).toInt()) { i ->
                debugSaveGridCell(cx - checkDistance, grid.cy + i)

                if (level.hasCollision(cx - checkDistance, grid.cy + i) && xrLeft <= 1 - checkDistance) {  // Check the next X cell
                    dir = -1
                }
            }
            // Cell coordinates of bottom corner of the collision box
            grid.setAndNormalizeY(cy, yrBottom)  // Bottom corner of the collision box
            debugSaveGridCell(cx - checkDistance, grid.cy)
            if (level.hasCollision(cx - checkDistance, grid.cy) && xrLeft <= 1 - checkDistance) {
                dir = -1
            }
        }
        return dir
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
        var dir = 0
        val xrLeft = xr + collisionBox.x / AppConfig.GRID_CELL_SIZE
        val xrRight = xr + (collisionBox.x + collisionBox.width) / AppConfig.GRID_CELL_SIZE

        // Check direction of movement
        if (velocityY > 0f) {  // Moving down
            val yrBottom = yr + (collisionBox.y + collisionBox.height) / AppConfig.GRID_CELL_SIZE

            val checkDistance = ceil(abs(collisionBox.y + collisionBox.height) / AppConfig.GRID_CELL_SIZE).toInt()

            debugSaveRatioPoint(cx, cy, xr, yrBottom)

            // Cell coordinates of left corner of the collision box
            grid.setAndNormalizeX(cx, xrLeft)  // Left corner of the collision box

            // Check collision in bottom cell and if yr is greater than 1 (yr is over cell bounds)
            repeat( ceil(collisionBox.width / AppConfig.GRID_CELL_SIZE).toInt()) { i ->
                debugSaveGridCell(grid.cx + i, cy + checkDistance)

                if (level.hasCollision(grid.cx + i, cy + checkDistance) && yrBottom >= checkDistance) {  // Check the next Y cell
                    dir = 1
                }
            }
            // Cell coordinates of right corner of the collision box
            grid.setAndNormalizeX(cx, xrRight)  // Right corner of the collision box
            debugSaveGridCell(grid.cx, cy + checkDistance)
            if (level.hasCollision(grid.cx, cy + checkDistance) && yrBottom >= checkDistance) {
                dir = 1
            }
        } else if (velocityY < 0f) {  // Moving up
            val yrTop = yr + collisionBox.y / AppConfig.GRID_CELL_SIZE

            val checkDistance = ceil(abs(collisionBox.y) / AppConfig.GRID_CELL_SIZE).toInt()

            debugSaveRatioPoint(cx, cy, xr, yrTop)

            // Cell coordinates of left corner of the collision box
            grid.setAndNormalizeX(cx, xrLeft)  // Left corner of the collision box

            // Check collision in bottom cell and if yr is greater than 1 (yr is over cell bounds)
            repeat( ceil(collisionBox.width / AppConfig.GRID_CELL_SIZE).toInt()) { i ->
                debugSaveGridCell(grid.cx + i, cy - checkDistance)

                if (level.hasCollision(grid.cx + i, cy - checkDistance) && yrTop <= 1 - checkDistance) {  // Check the next Y cell
                    dir = -1
                }
            }
            // Cell coordinates of right corner of the collision box
            grid.setAndNormalizeX(cx, xrRight)  // Right corner of the collision box
            debugSaveGridCell(grid.cx, cy - checkDistance)
            if (level.hasCollision(grid.cx, cy - checkDistance) && yrTop <= 1 - checkDistance) {
                dir = -1
            }
        }
        return dir
    }

    private fun debugSaveGridCell(cx: Int, cy: Int) {
        debugPointPool.collisionGridCells.add(
            point {
                x = cx.toFloat() * AppConfig.GRID_CELL_SIZE
                y = cy.toFloat() * AppConfig.GRID_CELL_SIZE
            }
        )
    }

    private fun debugSaveRatioPoint(cx: Int, cy: Int, xr: Float, yr: Float) {
        debugPointPool.collisionRatioPositions.add(
            point {
                x = (cx.toFloat() + xr) * AppConfig.GRID_CELL_SIZE
                y = (cy.toFloat() + yr) * AppConfig.GRID_CELL_SIZE
            }
        )
    }
}
