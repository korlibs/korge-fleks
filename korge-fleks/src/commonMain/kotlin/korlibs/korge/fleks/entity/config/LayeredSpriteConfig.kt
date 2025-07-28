package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.*
import korlibs.datastructure.iterators.fastForEach
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.EntityRefsByName.Companion.entityRefsByNameComponent
import korlibs.korge.fleks.components.Layer.Companion.layerComponent
import korlibs.korge.fleks.components.LifeCycle.Companion.lifeCycleComponent
import korlibs.korge.fleks.components.Position.Companion.positionComponent
import korlibs.korge.fleks.components.Rgba.Companion.rgbaComponent
import korlibs.korge.fleks.components.Sprite.Companion.spriteComponent
import korlibs.korge.fleks.components.SpriteLayers.Companion.spriteLayersComponent
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


/**
 * Config for creation of a logo entity which can consist of multiple layers.
 * Each layer can be controlled independently e.g. to fade-in or move-in parts
 * of the logo more dynamically.
 *
 * Logo can be centered on the screen and additionally an offset can be specified.
 */
// TODO rename to SpriteWithLayersConfig or something similar?
@Serializable @SerialName("LogoEntityConfig")
data class LayeredSpriteConfig(
    override val name: String,

    private val assetName: String,
    private val centerX: Boolean = false,
    private val centerY: Boolean = false,
    private val offsetX: Float = 0f,
    private val offsetY: Float = 0f,
    @Serializable(with = RGBAAsInt::class) private val tint: RGBA = Colors.WHITE,
    private val alpha: Float = 1f,
    private val layerIndex: Int,
    private val layerTag: RenderLayerTag,
    private val createEntityPerLayer: Boolean = true // If true, creates an entity for each layer in the sprite for tween animation

) : EntityConfig {

    override fun World.entityConfigure(entity: Entity) : Entity {
        val assetStore: AssetStore = inject(name = "AssetStore")
        val imageFrame = assetStore.getImageFrame(assetName)

        entity.configure {
            it += positionComponent {
                x = this@LayeredSpriteConfig.offsetX + (if (centerX) (AppConfig.VIEW_PORT_WIDTH - assetStore.getImageData(assetName).width).toFloat() * 0.5f else 0f)
                y = this@LayeredSpriteConfig.offsetY + (if (centerY) (AppConfig.VIEW_PORT_HEIGHT - assetStore.getImageData(assetName).height).toFloat() * 0.5f else 0f)
            }
            it += spriteComponent {
                name = assetName
            }
            it += spriteLayersComponent {
                // Iterate over all layers of the sprite
                imageFrame.layerData.fastForEach { layerData ->
                    val layerName = layerData.layer.name ?: error("LayeredSpriteConfig: Layer name is null for layer index ${layerData.layer.index} in asset '$assetName'!")
                    // Add layer to the sprite layers component
                    createSpriteLayer(layerName)
                }
            }
            if (createEntityPerLayer) {
                it += entityRefsByNameComponent {
                    // Iterate over all layers of the sprite
                    imageFrame.layerData.fastForEach { layerData ->
                        val layerName = layerData.layer.name
                            ?: error("LayeredSpriteConfig: Layer name is null for layer index ${layerData.layer.index} in asset '$assetName'!")
                        // Add entity for each layer
                        add(layerName,
                            createEntity("layer_$layerName") { layerEntity ->
                                layerEntity += positionComponent {}
                                layerEntity += rgbaComponent {}
                            }
                        )
                    }
                }
            }
            it += rgbaComponent {
                rgba = tint
                alpha = this@LayeredSpriteConfig.alpha
            }
            it += layerComponent { index = this@LayeredSpriteConfig.layerIndex }
            it += layerTag
            // Add life cycle component because we have list of layer entities which needs to be cleaned up by LifeCycleSystem on deletion
            it += lifeCycleComponent {}
        }
        return entity
    }

    init {
        EntityFactory.register(this)
    }
}
