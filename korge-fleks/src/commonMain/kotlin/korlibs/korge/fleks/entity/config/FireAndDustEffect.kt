package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.image.format.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.components.TweenSequenceComponent.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.entity.EntityFactory.EntityConfig
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.random


/**
 * Function to generate effect objects like explosions, shoots, dust, etc.
 *
 */
data class FireAndDustEffect(
    override val name: String,

    private val assetName: String,
    private val animationName: String,

    private val offsetX: Float = 0f,
    private val offsetY: Float = 0f,

    private val velocityX: Float = 0f,
    private val velocityY: Float = 0f,
    private val velocityVariationX: Float = 0f,
    private val velocityVariationY: Float = 0f,

    private val renderLayerTag: RenderLayerTag = RenderLayerTag.MAIN_EFFECTS,
    private val layerIndex: Int? = null,
    private val fadeOutDuration: Float = 0f
) : EntityConfig {

    // Configure function which applies the config to the entity's components
    override val configureEntity = fun(world: World, entity: Entity) = with(world) {
        entity.configure { entity ->
            entity += InfoComponent(name = this@FireAndDustEffect.name)

            var velocityXX = velocityX
            var velocityYY = velocityY
            if (velocityVariationX != 0f) {
                velocityXX += (-velocityVariationX..velocityVariationX).random()
            }
            if (velocityVariationY != 0f) {
                velocityYY += (-velocityVariationY..velocityVariationY).random()
            }
            entity += MotionComponent(
                velocityX = velocityXX,
                velocityY = velocityYY
            )
            entity += SpriteComponent(
                name = assetName,
                anchorX = offsetX,
                anchorY = offsetY,
                animation = animationName,
                running = true,
                direction = ImageAnimation.Direction.ONCE_FORWARD,
                destroyOnAnimationFinished = true
            )
            entity += RgbaComponent().apply {
                alpha = 1f
            }
            if (fadeOutDuration > 0f) {
                entity.getOrAdd(TweenSequenceComponent) { TweenSequenceComponent() }.apply {
                    tweens = listOf(
                        // Fade out effect objects
                        TweenRgba(entity = entity, alpha = 0f, duration = fadeOutDuration),
                        DeleteEntity(entity = entity)
                    )
                }
            }
            entity += renderLayerTag
            if (layerIndex != null) entity += LayerComponent(layerIndex = this@FireAndDustEffect.layerIndex)
//            entity += RenderLayerTag.DEBUG
        }
        entity
    }

    init {
        EntityFactory.register(this)
    }
}
