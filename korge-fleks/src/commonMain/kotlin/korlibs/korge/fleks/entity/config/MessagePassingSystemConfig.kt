package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.messagePassing.MessagePassingConfig.Companion.messagePassingConfigComponent
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*

/**
 * Entity configuration for the [MessagePassingSystem][korlibs.korge.fleks.systems.MessagePassingSystem].
 */
@Serializable @SerialName("MessagePassingSystemConfig")
data class MessagePassingSystemConfig(
    override val name: String
) : EntityConfig {

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
