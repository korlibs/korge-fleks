package korlibs.korge.fleks.entity.config

import AppConfig
import com.github.quillraven.fleks.*
import korlibs.korge.assetmanager.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.tags.*


/**
 * Config for creation of a logo entity which can consist of multiple layers.
 * Each layer can be controlled independently e.g. to fade-in or move-in parts
 * of the logo more dynamically.
 *
 * Logo can be centered on the screen and additionally an offset can be specified.
 */
data class LogoEntityConfig(
    override val name: String,

    private val assetName: String,
    private val centerX: Boolean = false,
    private val centerY: Boolean = false,
    private val offsetX: Float = 0f,
    private val offsetY: Float = 0f,
    private val tint: RgbaComponent.Rgb = RgbaComponent.Rgb.WHITE,
    private val alpha: Float = 1f,
    private val layerIndex: Int,
    private val layerTag: RenderLayerTag

) : EntityFactory.EntityConfig {

    override val configureEntity = fun(world: World, entity: Entity) : Entity = with(world) {
        val assetStore: AssetStore = inject(name = "AssetStore")

        entity.configure {
            it += PositionComponent(
                x = offsetX + (if (centerX) (AppConfig.TARGET_VIRTUAL_WIDTH - assetStore.getImageData(assetName).width).toFloat() * 0.5f else 0f),
                y = offsetY + (if (centerY) (AppConfig.TARGET_VIRTUAL_HEIGHT - assetStore.getImageData(assetName).height).toFloat() * 0.5f else 0f)
            )
            it += SpriteComponent(
                name = assetName
            )
            it += RgbaComponent().apply {
                tint = this@LogoEntityConfig.tint
                alpha = this@LogoEntityConfig.alpha
            }
            it += LayerComponent(layerIndex = this@LogoEntityConfig.layerIndex)
            it += layerTag

            // TODO handle logo layers
        }
        return entity
    }

    init {
        EntityFactory.register(this)
    }
}
