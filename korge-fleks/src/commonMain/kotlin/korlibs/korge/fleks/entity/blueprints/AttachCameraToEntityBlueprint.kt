package korlibs.korge.fleks.entity.blueprints


import com.github.quillraven.fleks.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.tags.CameraFollowTag
import kotlinx.serialization.*

@Serializable @SerialName("AttachCameraToEntityBlueprint")
data class AttachCameraToEntityBlueprint(
    override val name: String
) : EntityBlueprint {

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
