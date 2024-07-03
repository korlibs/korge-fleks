package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.EachFrame
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.LifeCycleComponent

class LifeCycleSystem : IteratingSystem(
    family { all(LifeCycleComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val lifeCycle = entity[LifeCycleComponent]

        if (lifeCycle.healthCounter <= 0) {
            world -= entity
            // TODO delete all subEntities
            println("LifeCycleSystem: Remove entity '${entity.id}'")
        }
    }
}
