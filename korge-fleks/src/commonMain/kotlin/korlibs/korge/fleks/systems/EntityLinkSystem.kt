package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.components.EntityRef.Companion.EntityRefComponent


class EntityLinkSystem  : IteratingSystem(
    family = family { all(EntityRefComponent).any(EntityRefComponent, PositionComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val entityLink = entity[EntityRefComponent]
        val (linkedEntity, moveWith) = entity[EntityRefComponent]

        if (moveWith) {
            if (entity has PositionComponent && linkedEntity has PositionComponent) {
                val positionComponent = entity[PositionComponent]
                // TODO: Check how we solve this by just using Entity class
                val (x, y) = entityLink.linkedEntity[PositionComponent]
                positionComponent.x = x
                positionComponent.y = y
            }
        }
    }
}
