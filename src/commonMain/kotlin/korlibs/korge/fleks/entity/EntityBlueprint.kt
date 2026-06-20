package korlibs.korge.fleks.entity

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

/**
 * This interface maps the string [name] to a specific game object creation process.
 * The game object entity will be created by the [entityConfigure] function which can be configured through additional
 * config properties in the derived class.
 *
 * Hint:
 * Deriving the configuration for an entity from this interface keeps the configuration details (config properties)
 * together with the creation/configuration process of a complex game object which can consist of multiple entities.
 * Also, the creation process of the game object can involve multiple steps of [entityConfigure] calls. Thus,
 * it is possible to use a "layered" creation process for entities where each layer adds global, common or more
 * specific components or sub-entities to a game object.
 */
interface EntityBlueprint {
    val name: String
    fun World.entityConfigure(entity: Entity) : Entity
}