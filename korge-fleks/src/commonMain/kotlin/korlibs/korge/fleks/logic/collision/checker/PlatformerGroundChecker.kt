package korlibs.korge.fleks.logic.collision.checker

import korlibs.korge.fleks.assets.data.gameObject.CollisionData
import korlibs.korge.fleks.logic.collision.GridPosition
import korlibs.korge.fleks.prefab.Prefab
import korlibs.korge.fleks.utils.AppConfig
import kotlin.math.abs
import kotlin.math.ceil


class PlatformerGroundChecker : GroundChecker() {
    private val grid = GridPosition()
    private val level = Prefab.levelData

    override fun onGround(
        cx: Int,
        cy: Int,
        xr: Float,
        yr: Float,
        velocityY: Float,
        collisionBox: CollisionData
    ): Boolean {
        val xrLeft = xr + collisionBox.x / AppConfig.GRID_CELL_SIZE
        val xrRight = xr + (collisionBox.x + collisionBox.width) / AppConfig.GRID_CELL_SIZE

        // Check direction of movement
        if (velocityY >= 0f) {  // Moving down
            val yrBottom = yr + (collisionBox.y + collisionBox.height) / AppConfig.GRID_CELL_SIZE
            //println("\nyrBottom: $yrBottom")

            val checkDistance: Int = ceil(abs(collisionBox.y + collisionBox.height) / AppConfig.GRID_CELL_SIZE).toInt()
            val checkBottom: Float = checkDistance.toFloat() - 0.0001f  // To avoid floating point precision issues
            //println("checkBottom: $checkBottom")

            // Cell coordinates of left corner of the collision box
            grid.setAndNormalizeX(cx, xrLeft)  // Left corner of the collision box
            //println("grid.cx: ${grid.cx}, grid.xr: ${grid.xr}")

            // Check collision in bottom cell and if yr is greater than 1 (yr is over cell bounds)
            repeat(ceil(collisionBox.width / AppConfig.GRID_CELL_SIZE).toInt()) { i ->
                //println("Checking cell: cx + $i, cy + $checkDistance")
                if (level.hasCollision(grid.cx + i, cy + checkDistance) && yrBottom >= checkBottom) return true  // Check the next Y cell
            }
            // Cell coordinates of right corner of the collision box
            grid.setAndNormalizeX(cx, xrRight)  // Right corner of the collision box
            //println("grid.cx: ${grid.cx}, grid.xr: ${grid.xr}")
            //println("Checking cell: cx, cy + $checkDistance")
            if (level.hasCollision(grid.cx, cy + checkDistance) && yrBottom >= checkBottom) return true
        }
        return false
    }
}
