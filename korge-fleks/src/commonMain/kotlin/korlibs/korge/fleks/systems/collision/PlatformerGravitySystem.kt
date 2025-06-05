package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.Gravity.Companion.GravityComponent
import korlibs.korge.fleks.components.Platformer.Companion.PlatformerComponent

class PlatformerGravitySystem : IteratingSystem(
    World.family { all(PlatformerComponent, GravityComponent) }
) {

    override fun onTickEntity(entity: Entity) {
        val platformerComponent = entity[PlatformerComponent]
        val gravityComponent = entity[GravityComponent]

        gravityComponent.enableGravityY = !platformerComponent.onGround
    }
}