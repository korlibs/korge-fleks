package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


/**
 * This component links one entity to another entity
 */
@Serializable @SerialName("EntityLink")
data class EntityLinkComponent(
    var linkedEntity: Entity = Entity.NONE,
    // Configure what to do with the linked entity
    var moveWith: Boolean = true

) : CloneableComponent<EntityLinkComponent>() {
    override fun type() = EntityLinkComponent
    companion object : ComponentType<EntityLinkComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): EntityLinkComponent =
        this.copy(
            // Perform deep copy of Entity
            linkedEntity = linkedEntity.clone()  // Entity(linkedEntity.id, linkedEntity.version)
        )
}

data class SubEntitiesComponent(
    var subEntities: List<Entity> = listOf(),
    // Configure what to do with the linked entities
    var moveWith: Boolean = false,
    var delete: Boolean = false
) : CloneableComponent<SubEntitiesComponent>() {
    override fun type() = SubEntitiesComponent
    companion object : ComponentType<SubEntitiesComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): SubEntitiesComponent =
        this.copy(
            // Perform deep copy of Entity List
            subEntities = subEntities.clone()
        )
}
