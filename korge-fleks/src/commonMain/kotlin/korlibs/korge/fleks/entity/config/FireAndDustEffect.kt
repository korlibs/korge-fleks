package korlibs.korge.fleks.entity.config

import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.assets.EntityConfig
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.utils.Invokable
import korlibs.korge.fleks.utils.InvokableSerializer
import korlibs.korge.fleks.utils.random


object FireAndDustEffect {

    data class Config(
        val assetName: String,
        val animationName: String,
        val offsetX: Float = 0.0f,
        val offsetY: Float = 0.0f,
        val velocityX: Float = 0.0f,
        val velocityY: Float = 0.0f,
        val velocityVariationX: Float = 0.0f,
        val velocityVariationY: Float = 0.0f,
        val drawOnLayer: String,
        val fadeOutDuration: Float = 0f
    ) : EntityConfig

    val configureEffectObject = Invokable(name = "configureEffectObject") { world, entity, config ->
        with(world) {
            val effectConfig = inject<AssetStore>("AssetStore").getEntityConfig<Config>(config)
            entity.configure { entity ->
                entity.getOrAdd(Offset) { Offset() }.also {
                    it.x = effectConfig.offsetX
                    it.y = effectConfig.offsetY
                }
                entity.getOrAdd(Motion) { Motion() }.also {
                    var velocityXX = effectConfig.velocityX
                    var velocityYY = effectConfig.velocityY
                    if (effectConfig.velocityVariationX != 0.0f) {
                        velocityXX += (-effectConfig.velocityVariationX..effectConfig.velocityVariationX).random()
                    }
                    if (effectConfig.velocityVariationY != 0.0f) {
                        velocityYY += (-effectConfig.velocityVariationY..effectConfig.velocityVariationY).random()
                    }
                    it.velocityX = velocityXX
                    it.velocityY = velocityYY
                }
                entity.getOrAdd(Sprite) { Sprite() }.also {
                    it.assetName = effectConfig.assetName
                    it.animationName = effectConfig.animationName
                    it.isPlaying = true
                }
                entity.getOrAdd(Drawable) { Drawable() }.also {
                    it.drawOnLayer = effectConfig.drawOnLayer
                }
                entity.getOrAdd(Appearance) { Appearance() }
                entity.getOrAdd(LifeCycle) { LifeCycle() }
                if (effectConfig.fadeOutDuration > 0f) {
                    entity.getOrAdd(AnimationScript) { AnimationScript() }.also {
                        it.tweens = listOf(
                            // Fade out effect objects
                            TweenAppearance(entity = entity, alpha = 0.0f, duration = effectConfig.fadeOutDuration)
                        )
                    }
                }
/* for visual debugging
                entity.getOrAdd(DebugInfo) { DebugInfo() }.also {
                    it.name = "Dust"
                }
// */
            }
        }
        entity
    }

    init {
        InvokableSerializer.register(configureEffectObject)
    }
}
