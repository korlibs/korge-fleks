package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.EntityRef.Companion.EntityRefComponent
import korlibs.korge.fleks.components.EntityRefs.Companion.EntityRefsComponent
import korlibs.korge.fleks.components.EntityRefsByName.Companion.EntityRefsByNameComponent
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.components.Position.Companion.PositionComponent


/**
 * This system updates the position of any linked entities if moveWith is true.
 * It checks for EntityRefComponent, EntityRefsComponent, and EntityRefsByNameComponent.
 * If the linked entity has a PositionComponent, it updates the position of the current entity accordingly.
 */
class EntityLinkSystem  : IteratingSystem(
    family = family { all(PositionComponent).any(EntityRefComponent, EntityRefsComponent, EntityRefsByNameComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val positionComponent = entity[PositionComponent]

        if (entity has EntityRefComponent) {
            val entityRefComponent = entity[EntityRefComponent]
            // Move linked entity with parent
            if (entityRefComponent.moveLinked) {
                setPositionOfLinkedEntity(entityRefComponent.entity, positionComponent)
            }
            // Move parent with linked entity
            if (entityRefComponent.moveWithParent) {
                setPositionOfParentEntity(entityRefComponent.entity, positionComponent)
            }
        }

        if (entity has EntityRefsComponent) {
            val entityRefsComponent = entity[EntityRefsComponent]
            if (entityRefsComponent.moveLinked) {
                for (entityRef in entityRefsComponent.entities) {
                    setPositionOfLinkedEntity(entityRef, positionComponent)
                }
            }
        }

        if (entity has EntityRefsByNameComponent) {
            val entityRefsByNameComponent = entity[EntityRefsByNameComponent]
            if (entityRefsByNameComponent.moveLinked) {
                for ((_, entityRef) in entityRefsByNameComponent.entities) {
                    setPositionOfLinkedEntity(entityRef, positionComponent)
                }
            }
        }
    }

    private fun setPositionOfLinkedEntity(entity: Entity, positionComponent: Position) {
        if (world.contains(entity)) {
            if (entity has PositionComponent) {
                val linkedPositionComponent = entity[PositionComponent]
                linkedPositionComponent.x = positionComponent.x
                linkedPositionComponent.y = positionComponent.y
            } else println("ERROR: EntityLinkSystem - Entity '${entity}' has no PositionComponent!")
        } else println("ERROR: EntityLinkSystem - Entity '${entity}' does not exist or version is different!")
    }

    private fun setPositionOfParentEntity(linkedEntity: Entity, positionComponent: Position) {
        if (world.contains(linkedEntity)) {
            if (linkedEntity has PositionComponent) {
                val linkedPositionComponent = linkedEntity[PositionComponent]
                positionComponent.x = linkedPositionComponent.x
                positionComponent.y = linkedPositionComponent.y
            } else println("ERROR: EntityLinkSystem - Entity '${linkedEntity}' (parent) has no PositionComponent!")
        } else println("ERROR: EntityLinkSystem - Entity '${linkedEntity}' (parent) does not exist or version is different!")
    }
}
