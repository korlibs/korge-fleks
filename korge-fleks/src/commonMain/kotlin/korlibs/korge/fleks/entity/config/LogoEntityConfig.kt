package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.*
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
data class LogoEntityConfig(
    override val name: String,

    private val assetName: String,
    private val centerX: Boolean = false,
    private val centerY: Boolean = false,
    private val offsetX: Float = 0f,
    private val offsetY: Float = 0f,
    @Serializable(with = RGBAAsInt::class) private val tint: RGBA = Colors.WHITE,
    private val alpha: Float = 1f,
    private val layerIndex: Int,
    private val layerTag: RenderLayerTag

) : EntityConfig {

    override fun World.entityConfigure(entity: Entity) : Entity {
        val assetStore: AssetStore = inject(name = "AssetStore")

        entity.configure {
            it += positionComponent {
                x = this@LogoEntityConfig.offsetX + (if (centerX) (AppConfig.VIEW_PORT_WIDTH - assetStore.getImageData(assetName).width).toFloat() * 0.5f else 0f)
                y = this@LogoEntityConfig.offsetY + (if (centerY) (AppConfig.VIEW_PORT_HEIGHT - assetStore.getImageData(assetName).height).toFloat() * 0.5f else 0f)
            }
            it += spriteComponent {
                name = assetName
            }
            it += spriteLayersComponent {
                // TODO load all layer names from asset store (from the aseprite file)
                // Initialize sprite layers in parent EntityConfig
            }
            it += entityRefsByNameComponent {
                // Initialize entities for each layer in parent EntityConfig
            }
            it += rgbaComponent {
                rgba = tint
                alpha = this@LogoEntityConfig.alpha
            }
            it += layerComponent { index = this@LogoEntityConfig.layerIndex }
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
