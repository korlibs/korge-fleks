package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.assets.EntityConfig
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.utils.Invokable
import korlibs.korge.fleks.utils.EntityConfigId
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
    )

    fun configureLogo(world: World, entity: Entity, config: EntityConfigId) : Entity = with(world) {
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
        return entity
    }

    // TODO change to configure logo layer
    fun createLogoLayer(world: World, config: LogoLayerConfig) : Entity  {
        return world.entity {
            it += SpecificLayer(spriteLayer = config.layerName, parentEntity = config.parentEntity)
            it += PositionShape()
            it += Offset(x = config.offsetX, y = config.offsetY)
            it += Appearance(alpha = config.alpha)
        }
    }
}

// TODO clean up
/*
fun createLogo(world: World, entity: Entity, config: EntityConfig) : Entity = with (world) {
    // Assuming entity has Info component
    val configName = if (entity has Info) entity[Info].configName else error("TextAndLogos - createLogo: Entity '${entity.id}' has no Info component!")
    val config = inject<AssetStore>("AssetStore").getConfig<TextAndLogos.LogoConfig>(configName)
    return TextAndLogos.configureLogo(this, entity, config)
}
*/