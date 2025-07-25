package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.EntityRef.Companion.EntityRefComponent
import korlibs.korge.fleks.components.EntityRefs.Companion.EntityRefsComponent
import korlibs.korge.fleks.components.EntityRefsByName.Companion.EntityRefsByNameComponent
import korlibs.korge.fleks.components.Info.Companion.InfoComponent
import korlibs.korge.fleks.components.LifeCycle.Companion.LifeCycleComponent

class LifeCycleSystem : IteratingSystem(
    family { all(LifeCycleComponent) },
    interval =  Fixed(1f / 60f)
) {
    override fun onTickEntity(entity: Entity) {
        val lifeCycle = entity[LifeCycleComponent]

        if (lifeCycle.healthCounter <= 0) {
            // Delete first all sub-entities
            deleteEntityRefs(entity)

            world -= entity
            debugPrint(entity, "base")
        }
    }

    private fun debugPrint(entity: Entity, type: String) {
        val name: String = entity.getOrNull(InfoComponent)?.name ?: "no name"
//        println("INFO: LifeCycleSystem: Remove $type-entity '${entity.id}' ($name)")
    }

    fun deleteEntityRefs(entity: Entity) {
        entity.getOrNull(EntityRefComponent)?.let {
            world -= it.entity
            debugPrint(it.entity, "sub")
        }
        entity.getOrNull(EntityRefsComponent)?.entities?.forEach { entity ->
            world -= entity
            debugPrint(entity, "sub")
        }
        entity.getOrNull(EntityRefsByNameComponent)?.entities?.forEach { (_, entity) ->
            world -= entity
            debugPrint(entity, "sub")
        }

    }
}
