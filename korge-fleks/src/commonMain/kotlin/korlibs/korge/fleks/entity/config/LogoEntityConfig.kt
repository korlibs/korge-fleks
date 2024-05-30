package korlibs.korge.fleks.entity.config

import AppConfig
import com.github.quillraven.fleks.*
import korlibs.korge.assetmanager.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.tags.*

data class LogoEntityConfig(
    override val name: String,

    private val x: Float = 0f,
    private val y: Float = 0f,
    private val assetName: String,
    private val centerX: Boolean = false,
    private val centerY: Boolean = false,
    private val tint: RgbaComponent.Rgb = RgbaComponent.Rgb.WHITE,
    private val alpha: Float = 1f,
    private val layerIndex: Int,
    private val layerTag: RenderLayerTag
) : EntityFactory.EntityConfig {
    override val configureEntity = fun(world: World, entity: Entity) : Entity = with(world) {

        val globalX: Float? = if (centerX) (AppConfig.TARGET_VIRTUAL_WIDTH - AssetStore.getImageData(assetName).width).toFloat() * 0.5f else null
        val globalY: Float? = if (centerY) (AppConfig.TARGET_VIRTUAL_HEIGHT - AssetStore.getImageData(assetName).height).toFloat() * 0.5f else null

        entity.configure {
            it += PositionComponent(
                x = globalX ?: this@LogoEntityConfig.x,
                y = globalY ?: this@LogoEntityConfig.y
            )
            it += SpriteComponent(
                name = this@LogoEntityConfig.assetName
            )
            it += RgbaComponent().apply {
                tint = this@LogoEntityConfig.tint
                alpha = this@LogoEntityConfig.alpha
            }
            it += LayerComponent(layerIndex = this@LogoEntityConfig.layerIndex)
            it += this@LogoEntityConfig.layerTag

            // TODO handle logo layers
        }
        return entity
    }

    init {
        EntityFactory.register(this)
    }
}
