package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.image.format.ImageAnimation
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This Tween is used to animate ...
 */
@Serializable @SerialName("TweenSprite")
class TweenSprite private constructor(
    var animation: String? = null,
    // do not tween the frameIndex, it is updated by the SpriteSystem
    var running: Boolean? = null,
    var direction: ImageAnimation.Direction? = null,
    var destroyOnPlayingFinished: Boolean? = null,

    override var target: Entity = Entity.NONE,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null
) : TweenBase, Poolable<TweenSprite> {
    // Init an existing data instance with data from another one
    override fun init(from: TweenSprite) {
        animation = from.animation
        running = from.running
        direction = from.direction
        destroyOnPlayingFinished = from.destroyOnPlayingFinished

        target = from.target
        delay = from.delay
        duration = from.duration
        easing = from.easing
        // Hint: it is not needed to copy "easing" property by creating new one like below:
        // easing = Easing.ALL[easing::class.toString().substringAfter('$')]
    }

    // Cleanup data instance manually
    // This is used for data instances when they are a value property of a component
    override fun cleanup() {
        animation = null
        running = null
        direction = null
        destroyOnPlayingFinished = null

        target = Entity.NONE
        delay = null
        duration = null
        easing = null
    }

    // Clone a new data instance from the pool
    override fun clone(): TweenSprite = pool.alloc().apply { init(from = this@TweenSprite) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticTweenSprite(config: TweenSprite.() -> Unit): TweenSprite =
            TweenSprite().apply(config)

        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.tweenSprite(config: TweenSprite.() -> Unit) { tweens.add(pool.alloc().apply(config)) }

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "TweenSprite") { TweenSprite() }
    }
}
