package korlibs.korge.fleks.logic.collision.checker

import com.github.quillraven.fleks.World.Companion.inject
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.assets.data.gameObject.CollisionRect
import korlibs.korge.fleks.logic.collision.GridPosition
import korlibs.korge.fleks.utils.AppConfig
import kotlin.math.abs
import kotlin.math.ceil


class PlatformerGroundChecker : GroundChecker() {
    private val grid = GridPosition()
    private val level = inject<AssetStore>("AssetStore").worldMapData

    override fun onGround(
        cx: Int,
        cy: Int,
        xr: Double,
        yr: Double,
        velocityY: Double,
        collisionBox: CollisionRect
    ): Boolean {
        val xrLeft = xr + collisionBox.x / AppConfig.GRID_CELL_SIZE
        val xrRight = xr + (collisionBox.x + collisionBox.width) / AppConfig.GRID_CELL_SIZE

        // Check direction of movement
        if (velocityY >= 0.0) {  // Moving down
            val yrBottom = yr + (collisionBox.y + collisionBox.height) / AppConfig.GRID_CELL_SIZE

            val checkDistance: Int = ceil(abs(collisionBox.y + collisionBox.height) / AppConfig.GRID_CELL_SIZE).toInt()
            val checkBottom: Double = checkDistance.toDouble() - 0.0001  // To avoid floating point precision issues

            // Cell coordinates of left corner of the collision box
            grid.setAndNormalizeX(cx, xrLeft)  // Left corner of the collision box

            // Check collision in bottom cell and if yr is greater than 1 (yr is over cell bounds)
            repeat(ceil(collisionBox.width / AppConfig.GRID_CELL_SIZE).toInt()) { i ->
                if (level.hasCollision(grid.cx + i, cy + checkDistance) && yrBottom >= checkBottom) return true  // Check the next Y cell
            }
            // Cell coordinates of right corner of the collision box
            grid.setAndNormalizeX(cx, xrRight)  // Right corner of the collision box
            if (level.hasCollision(grid.cx, cy + checkDistance) && yrBottom >= checkBottom) return true
        }
        return false
    }
}
