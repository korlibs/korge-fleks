package korlibs.korge.fleks.logic.collision.resolver

import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.Grid
import korlibs.korge.fleks.components.Motion
import korlibs.korge.fleks.logic.collision.GridPosition
import korlibs.korge.fleks.utils.AppConfig


class PlatformerCollisionResolver : CollisionResolver() {
    private val grid = GridPosition()

    override fun resolveXCollision(
        gridComponent: Grid,
        motionComponent: Motion,
        collisionBox: AssetStore.CollisionData,
        dir: Int
    ) {
        if (dir == 1) {  // Right direction
            val xrRight = gridComponent.xr + (collisionBox.x + collisionBox.width) / AppConfig.GRID_CELL_SIZE

            grid.setAndNormalizeX(gridComponent.cx, xrRight)  // Get cell of right corner of the collision box
            val checkDistance = grid.cx - gridComponent.cx  // Check distance between pivot point cell and right corner cell

            val collisionOverlap = checkDistance - xrRight  // Ratio from right edge of the cell
            // Move the grid position to the edge of the cell
            gridComponent.xr += collisionOverlap
        }
        if (dir == -1) {  // Left direction
            val xrLeft = gridComponent.xr + collisionBox.x / AppConfig.GRID_CELL_SIZE

            grid.setAndNormalizeX(gridComponent.cx, xrLeft)  // Get cell of left corner of the collision box
            val checkDistance = gridComponent.cx - grid.cx  // Check distance between pivot point cell and left corner cell

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

            grid.setAndNormalizeY(gridComponent.cy, yrBottom)  // Get cell of bottom corner of the collision box
            val checkDistance = grid.cy - gridComponent.cy  // Check distance between pivot point cell and bottom corner cell

            val collisionOverlap = checkDistance - yrBottom  // Ratio from bottom edge of the cell
            // Move the grid position to the edge of the cell
            gridComponent.yr += collisionOverlap
        }
        if (dir == -1) {  // Up direction
            val yrTop = gridComponent.yr + collisionBox.y / AppConfig.GRID_CELL_SIZE

            grid.setAndNormalizeY(gridComponent.cy, yrTop)  // Get cell of top corner of the collision box
            val checkDistance = gridComponent.cy - grid.cy  // Check distance between pivot point cell and top corner cell

            val collisionOverlap = (1 - checkDistance) - yrTop  // Ratio from top edge of the cell
            // Move the grid position to the edge of the cell
            gridComponent.yr += collisionOverlap  // TODO this seems not to fix the issue + 0.0001f  // To avoid floating point precision issues
        }
        // We are in front of a collider cell (floor) - thus we stop the motion
        motionComponent.velocityY = 0f
    }
}
