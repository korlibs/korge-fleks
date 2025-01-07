package korlibs.korge.fleks.entity.config


import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*

@Serializable @SerialName("CameraConfig")
data class CameraConfig(
    override val name: String
) : EntityConfig {

    override fun World.entityConfigure(entity: Entity) : Entity {

        entity.configure {
            // Camera has position within the game world
            // Offset can be used to "shake" the camera on explosions etc.
            it += PositionComponent()

            // TODO: Add bounds of level world
        }
        return entity
    }

    init {
        EntityFactory.register(this)
    }
}
