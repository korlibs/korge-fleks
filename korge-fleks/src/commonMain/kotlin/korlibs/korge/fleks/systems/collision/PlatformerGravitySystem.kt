package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.Gravity.Companion.GravityComponent
import korlibs.korge.fleks.components.PlatformerCollision.Companion.PlatformerCollisionComponent

class PlatformerGravitySystem : IteratingSystem(
    World.family { all(PlatformerCollisionComponent, GravityComponent) }
) {

    override fun onTickEntity(entity: Entity) {
        val platformerComponent = entity[PlatformerCollisionComponent]
        val gravityComponent = entity[GravityComponent]

        gravityComponent.enableGravityY = !platformerComponent.onGround
    }
}