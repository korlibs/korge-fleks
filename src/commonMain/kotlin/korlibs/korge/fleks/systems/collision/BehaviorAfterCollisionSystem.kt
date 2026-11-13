package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.collection.compareEntity
import korlibs.korge.fleks.components.Behavior.Companion.BehaviorComponent
import korlibs.korge.fleks.tags.BehaviorAfterCollisionTag
import korlibs.korge.fleks.entity.behavior.BehaviorStorage

/**
 * This system is responsible to update the behavior of entities which have a
 * [BehaviorComponent] and [BehaviorAfterCollisionTag] attached.
 *
 */
class BehaviorAfterCollisionSystem : IteratingSystem(
    family = World.family { all(BehaviorComponent, BehaviorAfterCollisionTag) },
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
