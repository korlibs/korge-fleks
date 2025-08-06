package korlibs.korge.fleks.logic.collision.resolver

import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.Grid
import korlibs.korge.fleks.components.Motion
import korlibs.korge.fleks.utils.AppConfig


class PlatformerCollisionResolver : CollisionResolver() {

    override fun resolveXCollision(
        gridComponent: Grid,
        motionComponent: Motion,
        collisionBox: AssetStore.CollisionData,
        dir: Int
    ) {
        // We need to calculate the position of the entity in front of the level collider
        if (dir == 1) {  // Right direction
            val xxr = gridComponent.xr + (collisionBox.x + collisionBox.width) / AppConfig.GRID_CELL_SIZE
            val collisionOverlap = xxr - 1f
            // Move the grid position to the edge of the cell
            gridComponent.xr -= collisionOverlap

            // We are in front of a collider cell (wall) - thus we stop the motion
            motionComponent.velocityX = 0f
        }
        if (dir == -1) {  // Left direction
            // Set postion of the entity to the left edge of the cell - use negative offset of collision box
            gridComponent.xr = - collisionBox.x / AppConfig.GRID_CELL_SIZE
            motionComponent.velocityX = 0f
        }
    }

    override fun resolveYCollision(
        gridComponent: Grid,
        motionComponent: Motion,
        collisionBox: AssetStore.CollisionData,
        dir: Int
    ) {
        if (dir == -1) {  // Up direction
            val yyr = gridComponent.yr + collisionBox.y / AppConfig.GRID_CELL_SIZE

//            gridComponent.cy = 0
//            gridComponent.yr = 0.3f
        }
        if (dir == 1) {  // Down direction
            val yyr = gridComponent.yr + (collisionBox.y + collisionBox.height) / AppConfig.GRID_CELL_SIZE
            val collisionOverlap = yyr - 1f
            // Move the grid position to the edge of the cell
            gridComponent.yr -= collisionOverlap

            // We are in front of a collider cell (floor) - thus we stop the motion
            motionComponent.velocityY = 0f
        }
    }
}
