package korlibs.korge.fleks.entity.config


import com.github.quillraven.fleks.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.tags.CameraFollowTag
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*

@Serializable @SerialName("AttachCameraToEntityConfig")
data class AttachCameraToEntityConfig(
    override val name: String
) : EntityConfig {

    // Function for adding components to this entity
    override fun World.entityConfigure(entity: Entity) : Entity {

        entity.configure {
            it += CameraFollowTag
        }
        return entity
    }

    init {
        // Register entity config into entity factory for lookup by its name
        EntityFactory.register(this)
    }
}
