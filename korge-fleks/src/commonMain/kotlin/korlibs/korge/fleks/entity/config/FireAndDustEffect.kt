package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.components.TweenSequenceComponent.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.entity.EntityFactory.EntityConfig
import korlibs.korge.fleks.utils.Identifier
import korlibs.korge.fleks.utils.random


/**
 * Function to generate effect objects like explosions, shoots, dust, etc.
 *
 */
data class FireAndDustEffect(
    override val name: String,
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
) : EntityConfig {

    // Used in component properties to specify invokable function
    val configureEffectObject = Identifier(name = "configureEffectObject")

    // Configure function which applies the config to the entity's components
    override val configureEntity = fun(world: World, entity: Entity) = with(world) {
        entity.configure { entity ->
            entity.getOrAdd(PositionComponent) { PositionComponent() }.also {
                it.offsetX = offsetX
                it.offsetY = offsetY
            }
            entity.getOrAdd(MotionComponent) { MotionComponent() }.also {
                var velocityXX = velocityX
                var velocityYY = velocityY
                if (velocityVariationX != 0f) {
                    velocityXX += (-velocityVariationX..velocityVariationX).random()
                }
                if (velocityVariationY != 0f) {
                    velocityYY += (-velocityVariationY..velocityVariationY).random()
                }
                it.velocityX = velocityXX
                it.velocityY = velocityYY
            }
            entity.getOrAdd(SpriteComponent) { SpriteComponent() }.also {
                it.name = assetName
                it.animation = animationName
//                it.isPlaying = true
            }

//            entity.getOrAdd(DrawableComponent) { DrawableComponent() }.also {
//                it.drawOnLayer = effectConfig.drawOnLayer
//            }
            entity.getOrAdd(LifeCycleComponent) { LifeCycleComponent() }
            if (fadeOutDuration > 0f) {
                entity.getOrAdd(TweenSequenceComponent) { TweenSequenceComponent() }.also {
                    it.tweens = listOf(
                        // Fade out effect objects
                        TweenRgba(entity = entity, alpha = 0f, duration = fadeOutDuration),
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
        EntityFactory.register(this)
    }
}
