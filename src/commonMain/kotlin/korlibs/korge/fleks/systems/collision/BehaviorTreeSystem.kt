package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.BehaviorTree.Companion.BehaviorTreeComponent
import korlibs.korge.fleks.entity.behavior.BehaviorTreeStorage


/**
 * This system is responsible to tick the behavior tree of entities which have a [BehaviorTreeComponent] attached.
 * The behavior tree is ticked with a fixed interval of 1/60f to ensure a consistent update rate for the behavior trees.
 */
class BehaviorTreeSystem : IteratingSystem(
    family = World.family { all(BehaviorTreeComponent) },
    interval = Fixed(1 / 60f)
) {
    override fun onTickEntity(entity: Entity) {
        val bTreeComponent = entity[BehaviorTreeComponent]
        // Tick the behavior tree
        val behaviorTree = BehaviorTreeStorage.get(bTreeComponent.characterConfig)
        behaviorTree.run { world.tick(entity, deltaTime) }  // deltaTime in seconds
    }
}
