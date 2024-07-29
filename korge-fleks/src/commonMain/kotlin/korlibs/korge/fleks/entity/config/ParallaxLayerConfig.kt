package korlibs.korge.fleks.entity.config

import korlibs.korge.fleks.components.ParallaxComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.entity.EntityFactory.EntityConfig
import kotlinx.serialization.*


@Serializable @SerialName("ParallaxLayerConfig")
data class ParallaxLayerConfig(
    override val name: String,
    
    private val assetName: String,
    private val layerTag: RenderLayerTag,
    private val x: Float = 0f,
    private val y: Float = 0f
) : EntityConfig {

    override fun World.entityConfigure(entity: Entity) : Entity {
        entity.configure {
            it += PositionComponent(
                x = this@ParallaxLayerConfig.x,
                y = this@ParallaxLayerConfig.y
            )  // global position for the whole parallax background
            it += MotionComponent(
                velocityX = -12f  // world units per second
            )
            it += ParallaxComponent(
                name = assetName
            )
            it += layerTag
        }
        return entity
    }

    init {
        EntityFactory.register(this)
    }
}
