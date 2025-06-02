package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.Grid.Companion.GridComponent
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.Platformer.Companion.PlatformerComponent
import korlibs.korge.fleks.logic.collision.checker.CollisionChecker
import korlibs.korge.fleks.logic.collision.checker.GroundChecker

class PlatformerGroundSystem() :
    IteratingSystem(
        family { all(PlatformerComponent, GridComponent, MotionComponent/*, GridCollision*/) }) {

    override fun onTickEntity(entity: Entity) {
        val platformerComponent = entity[PlatformerComponent]
        val gridComponent = entity[GridComponent]
        val motionComponent = entity[MotionComponent]

        platformerComponent.onGround = groundChecker.onGround(
            motionComponent.velocityY,
            gridComponent.cx,
            gridComponent.cy,
            gridComponent.xr,
            gridComponent.yr,
            noCollisionChecker
        )
    }

    private val groundChecker: GroundChecker = SimpleGroundChecker()
    private val noCollisionChecker: CollisionChecker = CollisionChecker()

    class SimpleGroundChecker : GroundChecker() {
        override fun onGround(
            velocityY: Float, cx: Int, cy: Int, xr: Float, yr: Float, collisionChecker: CollisionChecker
        ): Boolean {
            return cy == 750 // && yr <= 0.3f

            // TODO: here we can check the middle point under the player if it is inside a collision box
        }
    }



    // Debug position for ground checking below player start position
    // 1758 - 1024 = 734 + 16 = 750
}
