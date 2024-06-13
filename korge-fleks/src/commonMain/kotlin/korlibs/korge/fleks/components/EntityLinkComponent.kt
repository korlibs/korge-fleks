package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
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

    // Hint to myself: Check if deep copy is needed on any change in the component!
    fun clone() : EntityLinkComponent =
        this.copy(
            // Perform deep copy of Entity
            linkedEntity = linkedEntity.clone()  // Entity(linkedEntity.id, linkedEntity.version)
        )
}
