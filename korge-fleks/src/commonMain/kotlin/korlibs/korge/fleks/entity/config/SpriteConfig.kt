package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.Layer.Companion.layerComponent
import korlibs.korge.fleks.components.Position.Companion.positionComponent
import korlibs.korge.fleks.components.Rgba.Companion.rgbaComponent
import korlibs.korge.fleks.components.Sprite.Companion.spriteComponent
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.tags.RenderLayerTag
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


/**
 * Generic entity configuration object which sets up an arbitrary texture sprite object with
 * position, texture, color/visibility, layer index and to be used renderer layer.
 * All needed components are added so that the sprite will be rendered by the [object render system][korlibs.korge.fleks.renderSystems.ObjectRenderSystem].
 */
@Serializable @SerialName("SpriteConfig")
data class SpriteConfig(
    override val name: String,

    private val x: Float = 0f,
    private val y: Float = 0f,
    private val assetName: String,
    private val alpha: Float = 1f,
    private val layerIndex: Int = 0,
    private val renderLayerTag: RenderLayerTag,
) : EntityConfig {

    // Function for adding components to this entity
    override fun World.entityConfigure(entity: Entity) : Entity {

        entity.configure {
            it += positionComponent {
                x = this@SpriteConfig.x
                y = this@SpriteConfig.y
            }
            it += spriteComponent {
                name = assetName
            }
            it += rgbaComponent {
                alpha = this@SpriteConfig.alpha
            }
            it += layerComponent {
                index = layerIndex
            }
            it += renderLayerTag
        }
        return entity
    }

    init {
        // Register entity config into entity factory for lookup by its name
        EntityFactory.register(this)
    }
}