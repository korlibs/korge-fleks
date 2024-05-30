package korlibs.korge.fleks.systems

import korlibs.korge.fleks.components.EntityLinkComponent
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.*


class EntityLinkSystem  : IteratingSystem(
    family = family { all(EntityLinkComponent).any(EntityLinkComponent, PositionComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val (linkedEntity, moveWith) = entity[EntityLinkComponent]

        if (moveWith) {
            if (entity has PositionComponent && linkedEntity has PositionComponent) {
                val positionComponent = entity[PositionComponent]
                val (x, y) = linkedEntity[PositionComponent]
                positionComponent.x = x
                positionComponent.y = y
            }
        }
    }
}
