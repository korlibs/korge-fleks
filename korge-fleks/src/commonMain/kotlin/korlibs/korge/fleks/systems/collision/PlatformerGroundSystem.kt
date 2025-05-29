package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.Grid.Companion.GridComponent
import korlibs.korge.fleks.components.MotionComponent
import korlibs.korge.fleks.components.Platformer.Companion.PlatformerComponent
import korlibs.korge.fleks.logic.collision.checker.CollisionChecker
import korlibs.korge.fleks.logic.collision.checker.GroundChecker
import korlibs.korge.fleks.logic.collision.checker.SimpleGroundChecker

class PlatformerGroundSystem(
    private val groundChecker: GroundChecker = SimpleGroundChecker(),
    private val noCollisionChecker: CollisionChecker = CollisionChecker()
) : IteratingSystem(
        World.family { all( PlatformerComponent, GridComponent ) }
) {

    override fun onTickEntity(entity: Entity) {
        val platformerComponent = entity[PlatformerComponent]
        val gridComponent = entity[GridComponent]
        val motionComponent = entity[MotionComponent.Companion]

        platformerComponent.onGround = groundChecker.onGround(
            motionComponent.velocityY,
            gridComponent.cx,
            gridComponent.cy,
            gridComponent.xr,
            gridComponent.yr,
            noCollisionChecker
        )
    }
}
