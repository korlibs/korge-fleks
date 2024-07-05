package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.*

class LifeCycleSystem : IteratingSystem(
    family { all(LifeCycleComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val lifeCycle = entity[LifeCycleComponent]

        if (lifeCycle.healthCounter <= 0) {
            // Delete first all sub-entities
            entity.getOrNull(SubEntitiesComponent)?.subEntities?.forEach {
                world -= it
                println("LifeCycleSystem: Remove sub-entity '${entity.id}'")
            }
            // Delete first all layer entities
            entity.getOrNull(LayeredSpriteComponent)?.layerList?.forEach {
                world -= it.entity
                println("LifeCycleSystem: Remove layer-entity '${entity.id}'")
            }
            world -= entity
            println("LifeCycleSystem: Remove base-entity '${entity.id}'")
        }
    }
}
