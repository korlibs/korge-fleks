package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.image.format.*
import korlibs.image.format.ImageAnimation.Direction.*
import korlibs.korge.assetmanager.AssetStore
import korlibs.time.*
import kotlinx.serialization.*


/**
 * The [SpriteComponent] component adds visible details to a sprite entity.
 * By adding [SpriteComponent] to an entity the entity will be able to handle animations.
 *
 * @param [name]
 * @param [anchorX] X offset of the sprite graphic to the zero-point of the sprite (pivot-point).
 * @param [anchorY] Y offset of the sprite graphic to the zero-point of the sprite (pivot-point).
 * @param [layerIndex] defines the order in which the sprite texture will be drawn. Higher numbers mean the sprite will be rendered on top of other sprites with smaller number.
 *
 * @param [animation]
 *
 *
 */
@Serializable
@SerialName("Sprite")
data class SpriteComponent(
    var name: String = "",
    var anchorX: Float = 0f,                          // x,y position of the pivot point within the sprite
    var anchorY: Float = 0f,
    val layerIndex: Int = 0,

    var animation: String? = null,
    var frameIndex: Int = 0,                          // frame number of animation which is currently drawn
    var running: Boolean = false,                     // Switch animation on and off
    var direction: ImageAnimation.Direction? = null,
    var destroyOnAnimationFinished: Boolean = false,  // Delete entity when direction is [ONCE_FORWARD] or [ONCE_REVERSE]

    // internal, do not set directly
    var increment: Int = 1,                           // out of [-1, 0, 1]; will be added to frameIndex each new frame
    var nextFrameIn: Float = 0f                       // time in seconds until next frame of animation shall be shown
) : Component<SpriteComponent> {
    override fun type(): ComponentType<SpriteComponent> = SpriteComponent

    /**
     * Initialize animation properties with data from AssetStore.
     */
    override fun World.onAdd(entity: Entity) {
        val numFrames = AssetStore.getAnimationNumberOfFrames(name, animation)

        // Set frameIndex for starting animation
        if (direction == REVERSE || direction == ONCE_REVERSE) {
            frameIndex = numFrames - 1
        } else {
            frameIndex = 0
        }

        // Set direction from Aseprite if not specified in the component
        if (direction == null) {
            direction = AssetStore.getAnimationDirection(name, animation)
        }

        // Init increment for setting frameIndex
        increment = when (direction) {
            FORWARD -> +1
            REVERSE -> -1
            PING_PONG -> +1     // ping-pong is starting forward
            ONCE_FORWARD -> +1  // starting forward
            ONCE_REVERSE -> -1  // starting reverse
            null -> error("SpriteAnimationFamily: direction shall not be null!")
        }

        // Set frame time for first frame
        nextFrameIn = AssetStore.getAnimationFrameDuration(name, animation, 0)

//        println("\nSpriteAnimationComponent:\n    entity: ${entity.id}\n    numFrames: $numFrames\n    increment: ${spriteAnimationComponent.increment}\n    direction: ${spriteAnimationComponent.direction}\n")

    }

    companion object : ComponentType<SpriteComponent>()
}

fun AssetStore.getImageAnimation(name: String, animation: String? = null) : ImageAnimation {
    return if (animation != null) {
        val spriteAnimations = getImageData(name).animationsByName
        if (spriteAnimations.contains(animation)) {
            spriteAnimations[animation]!!
        } else error("AssetStore: Image animation '$animation' not found!")
    } else {
        getImageData(name).defaultAnimation
    }
}

/**
 * Get animation frame for [frameIndex] out of [AssetStore].
 */
fun AssetStore.getImageFrame(name: String, animation: String? = null, frameIndex: Int = 0) : ImageFrame {
    val animationFrames = if (animation != null) {
        val spriteAnimations = getImageData(name).animationsByName
        if (spriteAnimations.contains(animation)) {
            spriteAnimations[animation]!!.frames
        } else {
            println("WARNING - AssetStore: Image animation '$animation' not found!")
            return ImageFrame(0)
        }
    } else {
        getImageData(name).defaultAnimation.frames
    }
    return if (animationFrames.size > frameIndex) {
        animationFrames[frameIndex]
    } else {
        println("WARNING -- AssetStore: Image animation frame '$frameIndex' out of bounds!")
        ImageFrame(0)
    }
}

fun AssetStore.getAnimationNumberOfFrames(name: String, animation: String? = null) : Int =
    getImageAnimation(name, animation).frames.size

fun AssetStore.getAnimationDirection(name: String, animation: String? = null) : ImageAnimation.Direction =
    getImageAnimation(name, animation).direction

/**
 * Returns the duration of an animation frame in seconds.
 */
fun AssetStore.getAnimationFrameDuration(name: String, animation: String? = null, frameIndex: Int) : Float =
    getImageFrame(name, animation, frameIndex).duration.seconds.toFloat()
