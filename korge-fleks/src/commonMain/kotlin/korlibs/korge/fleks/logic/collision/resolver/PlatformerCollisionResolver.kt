package korlibs.korge.fleks.logic.collision.resolver

import korlibs.korge.fleks.assets.WorldData
import korlibs.korge.fleks.components.Grid
import korlibs.korge.fleks.components.MotionComponent
import korlibs.korge.fleks.logic.collision.checker.CollisionChecker

class PlatformerCollisionResolver(
    var level: WorldData
): CollisionResolver() {

}

class SimpleCollisionResolver(val gridWidth: Int, val gridHeight: Int) : CollisionResolver() {

    override fun resolveXCollision(gridComponent: Grid, motionComponent: MotionComponent, collision: CollisionChecker, dir: Int) {
//        if (dir == -1) {
//            gridComponent.cx = 0
//            gridComponent.xr = 0.3f
//        }
//        if (dir == 1) {
//            gridComponent.cx = gridWidth
//            gridComponent.xr = 0.7f
//        }
    }

    // TODO: Implement CollisionChecker with "old" raycast system

    override fun resolveYCollision(gridComponent: Grid, motionComponent: MotionComponent, collision: CollisionChecker, dir: Int) {
        if (dir == -1) {
//            gridComponent.cy = 0
//            gridComponent.yr = 0.3f
        }
        if (dir == 1) {
            motionComponent.velocityY = 0f
            gridComponent.yr = 1f
//            gridComponent.cy = gridHeight
//            gridComponent.yr = 0.7f
        }
    }
}
