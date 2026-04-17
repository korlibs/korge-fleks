package korlibs.korge.fleks.entity.blueprints


import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.Position.Companion.positionComponent
import korlibs.korge.fleks.components.WorldChunk.Companion.worldChunkComponent
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.tags.*
import kotlinx.serialization.*

@Serializable @SerialName("MainCameraBlueprint")
data class MainCameraBlueprint(
    override val name: String
) : EntityBlueprint {

    override fun World.entityConfigure(entity: Entity) : Entity {

        entity.configure {
            // Camera has position within the game world relative to active chunk
            // Offset of position component can be used to "shake" the camera on explosions etc.
            it += positionComponent {}
            // Current chunk of the camera position (== middle view port position in the world)
            it += worldChunkComponent {}

            // Camera has a tag to make it easily accessible for other systems and entity configurations
            it += MainCameraTag
        }
        return entity
    }

    init {
        EntityFactory.register(this)
    }
}
