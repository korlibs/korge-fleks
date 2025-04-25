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
                debugPrint(it, "sub")
            }
            // Delete first all layer entities
            entity.getOrNull(LayeredSpriteComponent)?.layerList?.forEach {
                world -= it.entity
                debugPrint(it.entity, "layer")
            }
            world -= entity
            debugPrint(entity, "base")
        }
    }

    private fun debugPrint(entity: Entity, type: String) {
        val name: String = entity.getOrNull(Info)?.name ?: "no name"
//        println("INFO: LifeCycleSystem: Remove $type-entity '${entity.id}' ($name)")
    }
}
