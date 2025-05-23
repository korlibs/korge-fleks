package korlibs.korge.fleks.entity.config


import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*

@Serializable @SerialName("MainCameraConfig")
data class MainCameraConfig(
    override val name: String
) : EntityConfig {

    override fun World.entityConfigure(entity: Entity) : Entity {

        entity.configure {
            // Camera has position within the game world
            // Offset can be used to "shake" the camera on explosions etc.
            it += PositionComponent {}

            // Camera has a tag to make it easily accessible for other systems and entity configurations
            it += MainCameraTag
        }
        return entity
    }

    init {
        EntityFactory.register(this)
    }
}
