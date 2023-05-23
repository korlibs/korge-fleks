package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.assets.EntityConfig
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.utils.Invokable
import korlibs.korge.fleks.utils.EntityConfigId
import korlibs.korge.fleks.utils.InvokableSerializer
import korlibs.korge.fleks.utils.noConfig


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

        val invokable: Invokable? = null,
        val configId: EntityConfigId = noConfig
    ) : EntityConfig

    data class LogoLayerConfig(
        var layerName: String = "",
        var offsetX: Float = 0.0f,
        var offsetY: Float = 0.0f,
        val alpha: Float = 0.0f,
        var parentEntity: Entity = nullEntity
    ) : EntityConfig

    val configureLogo = Invokable { world, entity, config ->
        with(world) {
            val logoConfig = inject<AssetStore>("AssetStore").getEntityConfig<LogoConfig>(config.name())
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
                logoConfig.invokable?.let { invokable ->
                    entity.getOrAdd(InputTouchButton) { InputTouchButton() }.also {
                        it.action = invokable
                        it.buttonId = logoConfig.configId
                    }
                }
            }
        }
        entity
    }

    val configureLogoLayer = Invokable { world, entity, config ->
        with(world) {
            val layerConfig = inject<AssetStore>("AssetStore").getEntityConfig<LogoLayerConfig>(config.name())
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
        }
        entity
    }

    init {
        InvokableSerializer.register(
            configureLogo,
            configureLogoLayer
        )
    }
}
