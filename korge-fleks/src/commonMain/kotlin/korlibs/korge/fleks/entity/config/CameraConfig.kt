package korlibs.korge.fleks.entity.config


import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*

@Serializable @SerialName("MainCameraConfig")
data class MainCameraConfig(
    override val name: String,

    private val viewPortWith: Int,
    private val viewPortHeight: Int
) : EntityConfig {

    override fun World.entityConfigure(entity: Entity) : Entity {

        entity.configure {
            // Camera has position within the game world
            // Offset can be used to "shake" the camera on explosions etc.
            it += PositionComponent()
            // Camera has a size which is the view port of the game
            it += SizeIntComponent(
                width = viewPortWith,  // SizeIntComponent is used to store the view port size as integer values
                height = viewPortHeight
            )
            // Save half size for middle point of view port in separate component
            it += SizeComponent(
                width = viewPortWith * 0.5f,  // SizeComponent is used to store offset to middle point of view port
                height = viewPortHeight * 0.5f
            )

            // Camera has a tag to make it easily accessible for other systems and entity configurations
            it += MainCameraTag

            // TODO: Add bounds of level world (really here?)

        }
        return entity
    }

    init {
        EntityFactory.register(this)
    }
}
