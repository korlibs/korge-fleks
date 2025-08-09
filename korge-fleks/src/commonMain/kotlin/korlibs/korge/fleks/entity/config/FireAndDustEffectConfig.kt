package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.*
import korlibs.image.format.*
import korlibs.korge.fleks.components.Layer.Companion.layerComponent
import korlibs.korge.fleks.components.Motion.Companion.motionComponent
import korlibs.korge.fleks.components.Rgba.Companion.rgbaComponent
import korlibs.korge.fleks.components.Sprite.Companion.spriteComponent
import korlibs.korge.fleks.components.TweenSequence.Companion.tweenSequenceComponent
import korlibs.korge.fleks.components.data.tweenSequence.DeleteEntity.Companion.deleteEntity
import korlibs.korge.fleks.components.data.tweenSequence.TweenRgba.Companion.tweenRgba
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


/**
 * Function to generate effect objects like explosions, shoots, dust, etc.
 *
 */
@Serializable @SerialName("FireAndDustEffectConfig")
data class FireAndDustEffectConfig(
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
    private val fadeOutDuration: Float = 0f,
    private val screenCoordinates: Boolean = false
) : EntityConfig {

    // Configure function which applies the config to the entity's components
    override fun World.entityConfigure(entity: Entity) : Entity {
        entity.configure {
            if (screenCoordinates) it += ScreenCoordinatesTag
            it += motionComponent {
                velocityX =
                    if (velocityVariationX != 0f) this@FireAndDustEffectConfig.velocityX + (-velocityVariationX..velocityVariationX).random()
                    else this@FireAndDustEffectConfig.velocityX
                velocityY =
                    if (velocityVariationY != 0f) this@FireAndDustEffectConfig.velocityY + (-velocityVariationY..velocityVariationY).random()
                    else this@FireAndDustEffectConfig.velocityY
            }
            it += spriteComponent {
                name = assetName
                anchorX = offsetX
                anchorY = offsetY
                animation = animationName
                running = true
                direction = ImageAnimation.Direction.ONCE_FORWARD
                destroyOnAnimationFinished = true
            }
            it += rgbaComponent { alpha = 1f }
            if (fadeOutDuration > 0f) {
                it += tweenSequenceComponent {
                    // Fade out effect objects
                    tweenRgba { target = it; alpha = 0f; duration = fadeOutDuration }
                    deleteEntity { target = it }
                }
            }
            it += renderLayerTag
            if (layerIndex != null) it += layerComponent { index = this@FireAndDustEffectConfig.layerIndex }
//            entity += RenderLayerTag.DEBUG
        }
        return entity
    }

    init {
        EntityFactory.register(this)
    }
}
