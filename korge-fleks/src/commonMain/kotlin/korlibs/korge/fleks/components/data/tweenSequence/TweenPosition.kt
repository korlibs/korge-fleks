package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This Tween is used to animate ...
 */
@Serializable @SerialName("TweenPosition")
class TweenPosition private constructor(
    var x: Float? = null,
    var y: Float? = null,
    var offsetX: Float? = null,
    var offsetY: Float? = null,

    override var target: Entity = Entity.NONE,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null
) : TweenBase, Poolable<TweenPosition> {
    // Init an existing data instance with data from another one
    override fun init(from: TweenPosition) {
        x = from.x
        y = from.y
        offsetX = from.offsetX
        offsetY = from.offsetY

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
        x = null
        y = null
        offsetX = null
        offsetY = null

        target = Entity.NONE
        delay = null
        duration = null
        easing = null
    }

    // Clone a new data instance from the pool
    override fun clone(): TweenPosition = pool.alloc().apply { init(from = this@TweenPosition) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticTweenPosition(config: TweenPosition.() -> Unit): TweenPosition =
            TweenPosition().apply(config)

        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.tweenPosition(config: TweenPosition.() -> Unit) { tweens.add(pool.alloc().apply(config)) }

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "TweenPosition") { TweenPosition() }
    }
}
