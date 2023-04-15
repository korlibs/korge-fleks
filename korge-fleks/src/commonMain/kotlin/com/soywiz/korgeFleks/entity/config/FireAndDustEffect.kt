package com.soywiz.korgeFleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.soywiz.korgeFleks.components.*
import com.soywiz.korgeFleks.utils.random

object FireAndDustEffect {

    data class EffectObjectConfig(
        val assetName: String,
        val animationName: String,
        val offsetX: Double = 0.0,
        val offsetY: Double = 0.0,
        val velocityX: Double = 0.0,
        val velocityY: Double = 0.0,
        val velocityVariationX: Double = 0.0,
        val velocityVariationY: Double = 0.0,
        val drawOnLayer: String,
        val fadeOutDuration: Float = 0f
    )

    fun configureEffectObject(world: World, entity: Entity, config: EffectObjectConfig) : Entity = with(world) {
        entity.configure { entity ->
            entity.getOrAdd(Offset) { Offset() }.also {
                it.x = config.offsetX
                it.y = config.offsetY
            }
            entity.getOrAdd(Motion) { Motion() }.also {
                var velocityXX = config.velocityX
                var velocityYY = config.velocityY
                if (config.velocityVariationX != 0.0) {
                    velocityXX += (-config.velocityVariationX..config.velocityVariationX).random()
                }
                if (config.velocityVariationY != 0.0) {
                    velocityYY += (-config.velocityVariationY..config.velocityVariationY).random()
                }
                it.velocityX = velocityXX
                it.velocityY = velocityYY
            }
            entity.getOrAdd(Sprite) { Sprite() }.also {
                it.assetName = config.assetName
                it.animationName = config.animationName
                it.isPlaying = true
            }
            entity.getOrAdd(Drawable) { Drawable() }.also {
                it.drawOnLayer = config.drawOnLayer
            }
            entity.getOrAdd(Appearance) { Appearance() }
            entity.getOrAdd(LifeCycle) { LifeCycle() }
            if (config.fadeOutDuration > 0f) {
                entity.getOrAdd(AnimationScript) { AnimationScript() }.also {
                    it.tweens = listOf(
                        // Fade out effect objects
                        TweenAppearance(entity = entity, alpha = 0.0, duration = config.fadeOutDuration)
                    )
                }
            }
            /* for visual debugging
                            entity.getOrAdd(DebugInfo) { DebugInfo() }.also {
                                it.name = "Dust"
                            }
            // */
        }
        return entity
    }
}
