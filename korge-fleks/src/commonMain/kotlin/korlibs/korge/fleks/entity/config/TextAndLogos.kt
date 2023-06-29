package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.assets.ConfigBase
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.utils.*


object TextAndLogos {

    data class LogoConfig(
        val centerX: Boolean = false,
        val centerY: Boolean = false,
        val offsetX: Float = 0.0f,
        val offsetY: Float = 0.0f,

        val logoName: String? = null,
        val text: String? = null,
        val fontName: String = "",

        val alpha: Float = 0.0f,
        val drawOnLayer: String? = null,

        val function: Identifier? = null,
        val config: Identifier = nothing
    ) : ConfigBase

    data class LogoLayerConfig(
        var layerName: String = "",
        var offsetX: Float = 0.0f,
        var offsetY: Float = 0.0f,
        val alpha: Float = 0.0f,
        var parentEntity: Entity = invalidEntity
    ) : ConfigBase

    // Used in component properties to specify invokable function
    val configureLogo = Identifier(name = "configureLogo")
    val configureLogoLayer = Identifier(name = "configureLogoLayer")

    private val configureLogoFct = fun(world: World, entity: Entity, config: Identifier) = with(world) {
        val logoConfig = inject<AssetStore>("AssetStore").getEntityConfig<LogoConfig>(config)
        entity.configure { entity ->
            // Make sure we have position component
            entity.getOrAdd(PositionShape) { PositionShape() }

            logoConfig.logoName?.let {
                entity.getOrAdd(Sprite) { Sprite() }.also {
                    it.assetName = logoConfig.logoName
                }
            }
            logoConfig.text?.let {
                entity.getOrAdd(Text) { Text() }.also {
                    it.text = logoConfig.text
                    it.fontName = logoConfig.fontName
                }
            }

            entity += PositionShape()
            entity.getOrAdd(Layout) { Layout() }.also {
                it.centerX = logoConfig.centerX
                it.centerY = logoConfig.centerY
                it.offsetX = logoConfig.offsetX
                it.offsetY = logoConfig.offsetY
            }
            logoConfig.drawOnLayer?.let {
                entity.getOrAdd(Drawable) { Drawable() }.also {
                    it.drawOnLayer = logoConfig.drawOnLayer
                }
            }
            entity.getOrAdd(Appearance) { Appearance() }.also {
                it.alpha = logoConfig.alpha
            }
            entity += LifeCycle()
            logoConfig.function?.let { invokable ->
                entity.getOrAdd(InputTouchButton) { InputTouchButton() }.also {
                    it.function = invokable
                    it.config = logoConfig.config
                }
            }
        }
        entity
    }

    private val configureLogoLayerFct = fun(world: World, entity: Entity, config: Identifier) = with(world) {
        val layerConfig = inject<AssetStore>("AssetStore").getEntityConfig<LogoLayerConfig>(config)
        entity.configure { entity ->
            entity.getOrAdd(SpecificLayer) { SpecificLayer() }.also {
                it.spriteLayer = layerConfig.layerName
                it.parentEntity = layerConfig.parentEntity
            }
            entity.getOrAdd(PositionShape) { PositionShape() }  // x, y will be set to view in SpecificLayer hook function
            entity.getOrAdd(Offset) { Offset() }.also {
                it.x = layerConfig.offsetX
                it.y = layerConfig.offsetY
            }
            entity.getOrAdd(Appearance) { Appearance() }.also {
                it.alpha = layerConfig.alpha
            }
        }
        entity
    }

    init {
        Invokables.register(configureLogo, configureLogoFct)
        Invokables.register(configureLogoLayer, configureLogoLayerFct)
    }
}
