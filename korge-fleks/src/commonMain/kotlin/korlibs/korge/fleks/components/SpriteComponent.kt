package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.image.format.*
import korlibs.image.format.ImageAnimation.Direction.*
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.utils.*
import korlibs.time.*
import kotlinx.serialization.*


/**
 * The [SpriteComponent] component adds visible details to a sprite entity.
 * By adding [SpriteComponent] to an entity the entity will be able to handle textures and animations.
 *
 * @param [name] is the identifier for getting the sprite graphic from the [AssetStore].
 * @param [anchorX] X offset of the sprite graphic to the zero-point of the sprite (pivot-point).
 * @param [anchorY] Y offset of the sprite graphic to the zero-point of the sprite (pivot-point).
 *
 * @param [animation] is the identifier for getting the animation from the [AssetStore] for the
 *        sprite graphic specified by name property. If the sprite does not have animation than set it
 *        to "null".
 * @param [frameIndex] is the frame number which will be displayed by the [ObjectRenderSystem] for the sprite.
 *        The number can be also set directly to start the animation at a specific frame. Make sure the index
 *        is within the number of frames for the animation.
 * @param [running] starts or ends playing of the animation.
 * @param [direction] can be set to control the playing direction of the animation. It can be one of
 *        [FORWARD], [REVERSE], [PING_PONG], [ONCE_FORWARD] or [ONCE_REVERSE]. If not set than it is
 *        taken from the Aseprite file.
 * @param [destroyOnAnimationFinished] It this is set to true than the entity will be deleted when the
 *        sprite animation has finished playing. This works only if direction is set to [ONCE_FORWARD] or
 *        [ONCE_REVERSE].
 *
 * Other parameters should not be set directly. They are used internally by [SpriteSystem].
 */
@Serializable @SerialName("Sprite")
data class SpriteComponent(
    var name: String = "",
    var anchorX: Float = 0f,                          // x,y position of the pivot point within the sprite
    var anchorY: Float = 0f,

    var animation: String? = null,                    // Leave null if sprite texture does not have an animation
    var frameIndex: Int = 0,                          // frame number of animation which is currently drawn
    var running: Boolean = false,                     // Switch animation on and off
    var direction: ImageAnimation.Direction? = null,  // Default: Get direction from Aseprite file
    var destroyOnAnimationFinished: Boolean = false,  // Delete entity when direction is [ONCE_FORWARD] or [ONCE_REVERSE]

    // internal, do not set directly
    var increment: Int = -2,                          // out of [-1, 0, 1]; will be added to frameIndex each new frame
    var nextFrameIn: Float = 0f,                      // time in seconds until next frame of animation shall be shown
    var initialized: Boolean = false
) : Poolable<SpriteComponent>() {
    override fun type(): ComponentType<SpriteComponent> = SpriteComponent

    // Set frameIndex for starting animation
    fun setFrameIndex(assetStore: AssetStore) {
        frameIndex = if (direction == REVERSE || direction == ONCE_REVERSE)
            assetStore.getAnimationNumberOfFrames(name, animation) - 1 else 0
    }

    // Set frame time for first frame
    fun setNextFrameIn(assetStore: AssetStore) {
        nextFrameIn = assetStore.getAnimationFrameDuration(name, animation, frameIndex)
    }

    // Init increment for setting frameIndex
    fun setIncrement() {
        increment = when (direction) {
            FORWARD -> +1
            REVERSE -> -1
            PING_PONG -> +1     // ping-pong is starting forward
            ONCE_FORWARD -> +1  // starting forward
            ONCE_REVERSE -> -1  // starting reverse
            null -> error("SpriteAnimationFamily: direction shall not be null!")
        }
    }

    /**
     * Initialize animation properties with data from [AssetStore].
     */
    override fun World.onAdd(entity: Entity) {
        // Make sure that initialization is skipped on world snapshot loading (deserialization of save game)
        if (initialized) return
        else initialized = true

        val assetStore: AssetStore = this.inject(name = "AssetStore")

        // Set direction from Aseprite if not specified in the component
        if (direction == null) {
            direction = assetStore.getAnimationDirection(name, animation)
        }
        setFrameIndex(assetStore)
        setNextFrameIn(assetStore)
        setIncrement()

//        println("\nSpriteAnimationComponent:\n    entity: ${entity.id}\n    numFrames: $numFrames\n    increment: ${spriteAnimationComponent.increment}\n    direction: ${spriteAnimationComponent.direction}\n")
    }

    companion object : ComponentType<SpriteComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): SpriteComponent =
        this.copy(
            direction = direction  // normal ordinary enum - no deep copy needed
        )
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
            println("WARNING -- AssetStore: Image animation '$animation' not found!")
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
