package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import kotlinx.serialization.*


/**
 * This component links one entity to another entity
 */
@Serializable
@SerialName("EntityLink")
data class EntityLinkComponent(
    var linkedEntity: Entity = Entity.NONE,
    // Configure what to do with the linked entity
    var moveWith: Boolean = true

) : Component<EntityLinkComponent> {
    override fun type() = EntityLinkComponent
    companion object : ComponentType<EntityLinkComponent>()

    //
    fun clone() : EntityLinkComponent =
        EntityLinkComponent(
            linkedEntity = Entity(linkedEntity.id, linkedEntity.version),
            moveWith = moveWith
        )
}
