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
            deleteEntity(entity)
            debugPrint(entity, "base")
        }
    }

    private fun debugPrint(entity: Entity, type: String) {
        val name: String = entity.getOrNull(InfoComponent)?.name ?: "no name"
        //println("INFO: LifeCycleSystem: Remove $type-entity '${entity.id}' ($name)")
    }

    private fun deleteEntity(entity: Entity) {
        // Check if entity has sub-entities and delete them first recursively
        if (entity has EntityRefComponent && entity[EntityRefComponent].deleteLinked) {
            deleteEntity(entity[EntityRefComponent].entity)
            debugPrint(entity[EntityRefComponent].entity, "sub")
        }
        if (entity has EntityRefsComponent && entity[EntityRefsComponent].deleteLinked) {
            entity[EntityRefsComponent].entities.forEach { subEntity ->
                deleteEntity(subEntity)
                debugPrint(subEntity, "sub")
            }
        }
        if (entity has EntityRefsByNameComponent && entity[EntityRefsByNameComponent].deleteLinked) {
            entity[EntityRefsByNameComponent].entities.forEach { (_, subEntity) ->
                deleteEntity(subEntity)
                debugPrint(subEntity, "sub")
            }
        }

        // Finally delete the entity itself
        world -= entity
    }
}
