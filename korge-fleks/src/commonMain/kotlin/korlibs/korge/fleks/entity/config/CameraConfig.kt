package korlibs.korge.fleks.entity.config


import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*

@Serializable @SerialName("CameraConfig")
data class CameraConfig(
    override val name: String,

    private val viewPortWidth: Float = 0f,
    private val viewPortHeight: Float = 0f
) : EntityConfig {

    override fun World.entityConfigure(entity: Entity) : Entity {

        entity.configure {
            it += PositionComponent(
                offsetX = viewPortWidth * 0.5f,
                offsetY = viewPortHeight * 0.5f
            )
        }
        return entity
    }

    init {
        EntityFactory.register(this)
    }
}