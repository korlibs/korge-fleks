package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.Collision.Companion.CollisionComponent
import korlibs.korge.fleks.components.Grid.Companion.GridComponent
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.logic.collision.checker.GroundChecker
import korlibs.korge.fleks.logic.collision.checker.PlatformerGroundChecker

class PlatformerGroundSystem(
    private val groundChecker: GroundChecker = PlatformerGroundChecker()
) : IteratingSystem(
        World.family { all( CollisionComponent, GridComponent, MotionComponent ) },
    interval = Fixed(1 / 30f)
) {
    private val assetStore = world.inject<AssetStore>("AssetStore")

    override fun onTickEntity(entity: Entity) {
//        val collisionComponent = entity[CollisionComponent]
//        val gridComponent = entity[GridComponent]
//        val motionComponent = entity[MotionComponent]
//        val collisionBox = assetStore.getCollisionData(collisionComponent.name)
//
//        collisionComponent.wasGroundedLastFrame = collisionComponent.isGrounded
//        collisionComponent.isGrounded = groundChecker.onGround(
//            gridComponent.cx,
//            gridComponent.cy,
//            gridComponent.xr,
//            gridComponent.yr,
//            motionComponent.velocityY,
//            collisionBox
//        )
//
//        println("isGrounded: ${collisionComponent.isGrounded} for entity: ${entity.id} grid: ${gridComponent.cx}, ${gridComponent.cy} " +
//                "xr: ${gridComponent.xr}, yr: ${gridComponent.yr} velocityY: ${motionComponent.velocityY}")
    }
}
