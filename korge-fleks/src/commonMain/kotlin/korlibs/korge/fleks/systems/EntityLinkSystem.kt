package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.EntityRef.Companion.EntityRefComponent
import korlibs.korge.fleks.components.EntityRefs.Companion.EntityRefsComponent
import korlibs.korge.fleks.components.EntityRefsByName.Companion.EntityRefsByNameComponent
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
            if (entityRefComponent.moveWith) {
                if (entityRefComponent.entity has PositionComponent) {
                    val linkedPositionComponent = entityRefComponent.entity[PositionComponent]
                    linkedPositionComponent.x = positionComponent.x
                    linkedPositionComponent.y = positionComponent.y
                }
            }
        }

        if (entity has EntityRefsComponent) {
            val entityRefsComponent = entity[EntityRefsComponent]
            if (entityRefsComponent.moveWith) {
                for (entityRef in entityRefsComponent.entities) {
                    if (entityRef has PositionComponent) {
                        val linkedPositionComponent = entityRef[PositionComponent]
                        linkedPositionComponent.x = positionComponent.x
                        linkedPositionComponent.y = positionComponent.y
                    }
                }
            }
        }

        if (entity has EntityRefsByNameComponent) {
            val entityRefsByNameComponent = entity[EntityRefsByNameComponent]
            if (entityRefsByNameComponent.moveWith) {
                for ((_, entityRef) in entityRefsByNameComponent.entities) {
                    if (entityRef has PositionComponent) {
                        val linkedPositionComponent = entityRef[PositionComponent]
                        linkedPositionComponent.x = positionComponent.x
                        linkedPositionComponent.y = positionComponent.y
                    }
                }
            }
        }
    }
}
