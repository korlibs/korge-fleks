package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.image.format.ImageAnimation.Direction
import korlibs.image.format.ImageAnimation.Direction.*
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * The [SpriteComponent] component adds visible details to a sprite entity.
 * By adding [SpriteComponent] to an entity the entity will be able to handle textures and animations.
 *
 * @param [name] is the identifier for getting the sprite graphic from the [AssetStore].
 * @param [anchorX] X offset of the sprite graphic to the zero-point of the sprite (pivot-point).
 * @param [anchorY] Y offset of the sprite graphic to the zero-point of the sprite (pivot-point).
 *
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
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("Sprite")
class Sprite private constructor(
    var name: String = "",
    var visible: Boolean = true,
    var anchorX: Float = 0f,                          // x,y position of the pivot point within the sprite
    var anchorY: Float = 0f,

    var frameIndex: Int = 0,                          // frame number of animation which is currently drawn
    var running: Boolean = false,                     // Switch animation on and off
    var direction: Direction? = null,                 // Default: Get direction from Aseprite file
    var destroyOnAnimationFinished: Boolean = false,  // Delete entity when direction is [ONCE_FORWARD] or [ONCE_REVERSE]

    // internal, do not set directly
    var flipX: Boolean = false,
    var flipY: Boolean = false,
    var increment: Int = -2,                          // out of [-1, 0, 1]; will be added to frameIndex each new frame
    var nextFrameIn: Float = 0f                       // time in seconds until next frame of animation shall be shown
) : PoolableComponent<Sprite>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: Sprite) {
        name = from.name
        visible = from.visible
        anchorX = from.anchorX
        anchorY = from.anchorY
        frameIndex = from.frameIndex
        running = from.running
        direction = from.direction  // normal ordinary enum - no deep copy needed
        destroyOnAnimationFinished = from.destroyOnAnimationFinished
        flipX = from.flipX
        flipY = from.flipY
        increment = from.increment
        nextFrameIn = from.nextFrameIn
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        name = ""
        visible = true
        anchorX = 0f
        anchorY = 0f
        frameIndex = 0
        running = false
        direction = null
        destroyOnAnimationFinished = false
        flipX = false
        flipY = false
        increment = -2
        nextFrameIn = 0f
    }

    override fun type() = SpriteComponent

    companion object {
        val SpriteComponent = componentTypeOf<Sprite>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticSpriteComponent(config: Sprite.() -> Unit): Sprite =
            Sprite().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun spriteComponent(config: Sprite.() -> Unit): Sprite =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "Sprite") { Sprite() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): Sprite = spriteComponent { init(from = this@Sprite) }

    // Initialize the component automatically when it is added to an entity
    override fun World.initComponent(entity: Entity) {
        // Initialize animation properties with data from [AssetStore].
        val assetStore: AssetStore = this.inject(name = "AssetStore")
        resetAnimation(assetStore)
        //println("\nSpriteAnimationComponent:\n    entity: ${entity.id}\n    numFrames: $numFrames\n    increment: ${spriteAnimationComponent.increment}\n    direction: ${spriteAnimationComponent.direction}\n")
    }

    // Cleanup/Reset the component automatically when it is removed from an entity (component will be returned to the pool eventually)
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }

    // Initialize an external prefab when the component is added to an entity
    override fun World.initPrefabs(entity: Entity) {
    }

    // Cleanup/Reset an external prefab when the component is removed from an entity
    override fun World.cleanupPrefabs(entity: Entity) {
    }

    // Free the component and return it to the pool - this is called directly by the SnapshotSerializerSystem
    override fun free() {
        cleanup()
        pool.free(this)
    }

    // When changing animation state the component's properties need to be reset
    fun resetAnimation(assetStore: AssetStore) {
        setFrameIndex(assetStore)
        setNextFrameIn(assetStore)
        setIncrement()
    }

    // Set frameIndex for starting animation
    fun setFrameIndex(assetStore: AssetStore) {
        frameIndex = if (direction == REVERSE || direction == ONCE_REVERSE)
            assetStore.getTexture(name).numberOfFrames - 1 else 0
    }

    // Set frame time for first frame
    fun setNextFrameIn(assetStore: AssetStore) {
        nextFrameIn = assetStore.getTexture(name).getDuration(frameIndex)
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
}
