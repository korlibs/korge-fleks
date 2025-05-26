package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.EntityRef.Companion.EntityRefComponent
import korlibs.korge.fleks.components.Position.Companion.PositionComponent


class EntityLinkSystem  : IteratingSystem(
    family = family { all(EntityRefComponent).any(EntityRefComponent, PositionComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val entityLink = entity[EntityRefComponent]
        val entityRefComponent = entity[EntityRefComponent]

        if (entityRefComponent.moveWith) {
            if (entity has PositionComponent && entityRefComponent.entity has PositionComponent) {
                val positionComponent = entity[PositionComponent]
                // TODO: Check how we solve this by just using Entity class
                val linkedPositionComponent = entityLink.entity[PositionComponent]
                positionComponent.x = linkedPositionComponent.x
                positionComponent.y = linkedPositionComponent.y
            }
        }
    }
}
