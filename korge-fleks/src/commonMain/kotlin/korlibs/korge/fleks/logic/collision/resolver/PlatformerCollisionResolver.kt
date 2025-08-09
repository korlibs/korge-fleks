package korlibs.korge.fleks.logic.collision.resolver

import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.Grid
import korlibs.korge.fleks.components.Motion
import korlibs.korge.fleks.prefab.Prefab
import korlibs.korge.fleks.utils.AppConfig
import kotlin.math.abs
import kotlin.math.ceil


class PlatformerCollisionResolver : CollisionResolver() {

    private val level = Prefab.levelData

    override fun resolveXCollision(
        gridComponent: Grid,
        motionComponent: Motion,
        collisionBox: AssetStore.CollisionData,
        dir: Int
    ) {
        // We need to calculate the position of the entity in front of the level collider
        if (dir == 1) {  // Right direction
            val xrRight = gridComponent.xr + (collisionBox.x + collisionBox.width) / AppConfig.GRID_CELL_SIZE

            val checkDistance = ceil(abs(collisionBox.x + collisionBox.width) / AppConfig.GRID_CELL_SIZE).toInt()

            val collisionOverlap = checkDistance - xrRight  // Ratio from right edge of the cell
            // Move the grid position to the edge of the cell
            gridComponent.xr += collisionOverlap
        }
        if (dir == -1) {  // Left direction
            val xrLeft = gridComponent.xr + collisionBox.x / AppConfig.GRID_CELL_SIZE

            val checkDistance = ceil(abs(collisionBox.x) / AppConfig.GRID_CELL_SIZE).toInt()

            val collisionOverlap = (1 - checkDistance) - xrLeft  // Ratio from left edge of the cell
            // Move the grid position to the edge of the cell
            gridComponent.xr += collisionOverlap
        }
        // We are in front of a collider cell (wall) - thus we stop the motion
        motionComponent.velocityX = 0f
    }

    override fun resolveYCollision(
        gridComponent: Grid,
        motionComponent: Motion,
        collisionBox: AssetStore.CollisionData,
        dir: Int
    ) {
        // First check if Y cell is already a collider
        if (dir == 1) {  // Down direction
            val yrBottom = gridComponent.yr + (collisionBox.y + collisionBox.height) / AppConfig.GRID_CELL_SIZE

            val checkDistance = ceil(abs(collisionBox.y + collisionBox.height) / AppConfig.GRID_CELL_SIZE).toInt()

            val collisionOverlap = checkDistance - yrBottom  // Ratio from bottom edge of the cell
            // Move the grid position to the edge of the cell
            gridComponent.yr += collisionOverlap
        }
        if (dir == -1) {  // Up direction
            val yrTop = gridComponent.yr + collisionBox.y / AppConfig.GRID_CELL_SIZE

            val checkDistance = ceil(abs(collisionBox.y) / AppConfig.GRID_CELL_SIZE).toInt()

            val collisionOverlap = (1 - checkDistance) - yrTop  // Ratio from top edge of the cell
            // Move the grid position to the edge of the cell
            gridComponent.yr += collisionOverlap
        }
        // We are in front of a collider cell (floor) - thus we stop the motion
        motionComponent.velocityY = 0f
    }
}
