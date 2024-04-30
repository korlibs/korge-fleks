package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.assetmanager.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.components.TweenSequenceComponent.*
import korlibs.korge.fleks.utils.Identifier
import korlibs.korge.fleks.utils.random


/**
 * Function to generate effect objects like explosions, shoots, dust, etc.
 *
 */
object FireAndDustEffect {

    data class Config(
        val assetName: String,
        val animationName: String,
        val offsetX: Float = 0f,
        val offsetY: Float = 0f,
        val velocityX: Float = 0f,
        val velocityY: Float = 0f,
        val velocityVariationX: Float = 0f,
        val velocityVariationY: Float = 0f,
        val drawOnLayer: String,
        val fadeOutDuration: Float = 0f
    ) : ConfigBase

    // Used in component properties to specify invokable function
    val configureEffectObject = Identifier(name = "configureEffectObject")

    // Configure function which applies the config to the entity's components
    private val configureEffectObjectFct = fun(world: World, entity: Entity, config: Identifier) = with(world) {
        val effectConfig = AssetStore.getEntityConfig<Config>(config.name)
        entity.configure { entity ->
            entity.getOrAdd(OffsetComponent) { OffsetComponent() }.also {
                it.x = effectConfig.offsetX
                it.y = effectConfig.offsetY
            }
            entity.getOrAdd(MotionComponent) { MotionComponent() }.also {
                var velocityXX = effectConfig.velocityX
                var velocityYY = effectConfig.velocityY
                if (effectConfig.velocityVariationX != 0f) {
                    velocityXX += (-effectConfig.velocityVariationX..effectConfig.velocityVariationX).random()
                }
                if (effectConfig.velocityVariationY != 0f) {
                    velocityYY += (-effectConfig.velocityVariationY..effectConfig.velocityVariationY).random()
                }
                it.velocityX = velocityXX
                it.velocityY = velocityYY
            }
            entity.getOrAdd(SpriteComponent) { SpriteComponent() }.also {
                it.name = effectConfig.assetName
                it.animation = effectConfig.animationName
//                it.isPlaying = true
            }

//            entity.getOrAdd(DrawableComponent) { DrawableComponent() }.also {
//                it.drawOnLayer = effectConfig.drawOnLayer
//            }
            entity.getOrAdd(LifeCycleComponent) { LifeCycleComponent() }
            if (effectConfig.fadeOutDuration > 0f) {
                entity.getOrAdd(TweenSequenceComponent) { TweenSequenceComponent() }.also {
                    it.tweens = listOf(
                        // Fade out effect objects
                        TweenRgba(entity = entity, alpha = 0f, duration = effectConfig.fadeOutDuration),
                        DeleteEntity(entity = entity)
                    )
                }
            }
/* for visual debugging
            entity.getOrAdd(DebugInfo) { DebugInfo() }.also {
                it.name = "Dust"
            }
// */
        }
        entity
    }

    init {
        EntityFactory.register(configureEffectObject, configureEffectObjectFct)
    }
}
