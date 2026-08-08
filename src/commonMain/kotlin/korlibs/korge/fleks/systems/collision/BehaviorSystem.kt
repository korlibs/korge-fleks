package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.Behavior.Companion.BehaviorComponent
import korlibs.korge.fleks.entity.behavior.BehaviorStorage


/**
 * This system is responsible to update the behavior of entities which have a [BehaviorComponent] attached.
 * The behavior is updated with a fixed interval of 1/60f to ensure a consistent update rate.
 */
class BehaviorSystem : IteratingSystem(
    family = World.family { all(BehaviorComponent) },
    interval = Fixed(1 / 60f)
) {
    override fun onTickEntity(entity: Entity) {
        val behaviorComponent = entity[BehaviorComponent]
        // Tick the behavior tree
        val behavior = BehaviorStorage[behaviorComponent.name]
        behavior.run { world.update(entity, deltaTime) }  // deltaTime in seconds
    }
}
