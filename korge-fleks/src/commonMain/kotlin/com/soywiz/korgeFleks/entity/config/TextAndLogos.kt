package com.soywiz.korgeFleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.soywiz.korgeFleks.components.*


object TextAndLogos {

    data class LogoConfig(
        val centerX: Boolean = false,
        val centerY: Boolean = false,
        val offsetX: Double = 0.0,
        val offsetY: Double = 0.0,

        val logoName: String? = null,
        val text: String? = null,
        val fontName: String = "",

        val alpha: Double = 0.0,
        val drawOnLayer: String? = null
    )

    data class LogoLayerConfig(
        var layerName: String = "",
        var offsetX: Double = 0.0,
        var offsetY: Double = 0.0,
        val alpha: Double = 0.0,
        var parentEntity: Entity = nullEntity
    )

    fun configureLogo(world: World, entity: Entity, config: LogoConfig) : Entity = with(world) {
        entity.configure { entity ->

            config.logoName?.let {
                entity.getOrAdd(Sprite) { Sprite() }.also {
                    it.assetName = config.logoName
                }
            }
            config.text?.let {
                entity.getOrAdd(Text) { Text() }.also {
                    it.text = config.text
                    it.fontName = config.fontName
                }
            }

            entity += PositionShape()
            entity.getOrAdd(Layout) { Layout() }.also {
                it.centerX = config.centerX
                it.centerY = config.centerY
                it.offsetX = config.offsetX
                it.offsetY = config.offsetY
            }
            config.drawOnLayer?.let {
                entity.getOrAdd(Drawable) { Drawable() }.also {
                    it.drawOnLayer = config.drawOnLayer
                }
            }
            entity.getOrAdd(Appearance) { Appearance() }.also {
                it.alpha = config.alpha
            }
            entity += LifeCycle()
        }
        return entity
    }

    fun createLogo(world: World, config: LogoConfig) : Entity {
        val entity = world.entity {}
        return configureLogo(world, entity, config)
    }

    fun createLogoLayer(world: World, config: LogoLayerConfig) : Entity  {
        return world.entity {
            it += SpecificLayer(spriteLayer = config.layerName, parentEntity = config.parentEntity)
            it += PositionShape()
            it += Offset(x = config.offsetX, y = config.offsetY)
            it += Appearance(alpha = config.alpha)
        }
    }
}
