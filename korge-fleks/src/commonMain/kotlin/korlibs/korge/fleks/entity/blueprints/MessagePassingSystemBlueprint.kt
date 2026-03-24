package korlibs.korge.fleks.entity.blueprints

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.messagePassing.MessagePassingConfig.Companion.messagePassingConfigComponent
import korlibs.korge.fleks.entity.*
import kotlinx.serialization.*

/**
 * Entity configuration for the [MessagePassingSystem][korlibs.korge.fleks.systems.MessagePassingSystem].
 */
@Serializable @SerialName("MessagePassingSystemBlueprint")
data class MessagePassingSystemBlueprint(
    override val name: String
) : EntityBlueprint {

    // Function for adding components to this entity
    override fun World.entityConfigure(entity: Entity) : Entity {

        entity.configure {
            it += messagePassingConfigComponent {}
        }
        return entity
    }

    init {
        // Register entity config into entity factory for lookup by its name
        EntityFactory.register(this)
    }
}
