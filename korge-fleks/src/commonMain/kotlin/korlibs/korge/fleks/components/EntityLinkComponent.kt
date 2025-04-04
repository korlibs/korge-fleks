package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.fleks.utils.componentPool.*
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

@Serializable @SerialName("SubEntities")
data class SubEntitiesComponent(
    var subEntities: List<Entity> = listOf(),
    var subEntitiesByName: Map<String, Entity> = mapOf(),
    // Configure what to do with the linked entities
    var moveWith: Boolean = false,  // Not used currently!
) : CloneableComponent<SubEntitiesComponent>() {
    override fun type() = SubEntitiesComponent
    companion object : ComponentType<SubEntitiesComponent>()

    fun getSubEntity(name: String) : Entity =
        if (subEntitiesByName.contains(name)) subEntitiesByName[name]!!
        else Entity.NONE

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): SubEntitiesComponent =
        this.copy(
            // Perform deep copy of Entity List
            subEntities = subEntities.clone()
        )
}
