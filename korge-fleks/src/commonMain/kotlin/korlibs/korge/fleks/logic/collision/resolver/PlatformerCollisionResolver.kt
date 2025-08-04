package korlibs.korge.fleks.logic.collision.resolver

import korlibs.korge.fleks.prefab.data.LevelData
import korlibs.korge.fleks.components.Grid
import korlibs.korge.fleks.components.Motion
import korlibs.korge.fleks.logic.collision.GridPosition
import korlibs.korge.fleks.logic.collision.checker.CollisionChecker
import korlibs.korge.fleks.utils.AppConfig

class PlatformerCollisionResolver(
    var level: LevelData
): CollisionResolver() {

}

class SimpleCollisionResolver(val gridWidth: Int, val gridHeight: Int) : CollisionResolver() {

    override fun resolveXCollision(
        grid: GridPosition, motionComponent: Motion,
        //collision: CollisionChecker,
        width: Float,
        height: Float,
        dir: Int
    ) {
        if (dir == 1) {
//            grid.cx += 1
            // We need to move the position to the egde of the cell where the collision happened
            grid.xr = 1f - width / AppConfig.GRID_CELL_SIZE
            motionComponent.velocityX = 0f
        }
        if (dir == -1) {
//            grid.cx -= 1
//            grid.xr = 0f
            motionComponent.velocityX = 0f
        }
    }

    // TODO: Implement CollisionChecker with "old" raycast system
/*
    override fun resolveYCollision(
        gridComponent: Grid, motionComponent: Motion, collision: CollisionChecker, dir: Int
    ) {
        if (dir == -1) {
//            gridComponent.cy = 0
//            gridComponent.yr = 0.3f
        }
        if (dir == 1) {
//            motionComponent.velocityY = 0f
//            gridComponent.yr = 1f
//            gridComponent.cy = gridHeight
//            gridComponent.yr = 0.7f
        }
    }
*/
}
