package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.collection.compareEntity
import korlibs.korge.fleks.components.Behavior.Companion.BehaviorComponent
import korlibs.korge.fleks.entity.behavior.BehaviorStorage


/**
 * This system is responsible to update the behavior of entities which have a [BehaviorComponent] attached.
 * The behavior is updated with a fixed interval of 1/60f to ensure a consistent update rate.
 *
 * Entities are sorted by their behavior component index to ensure a consistent update order.
 * The higher the index, the later the entity will be updated in the current tick.
 */
class BehaviorSystem : IteratingSystem(
    family = World.family { all(BehaviorComponent) },
    interval = Fixed(1 / 60f),
    // Sort entities by their behavior component index to ensure a consistent update order
    comparator = compareEntity { entA, entB -> entA[BehaviorComponent].index.compareTo(entB[BehaviorComponent].index) }
) {
    override fun onTickEntity(entity: Entity) {
        val behaviorComponent = entity[BehaviorComponent]
        // Tick the behavior tree
        val behavior = BehaviorStorage[behaviorComponent.name]
        behavior.run { world.update(entity, deltaTime) }  // deltaTime in seconds
    }
}
