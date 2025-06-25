package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This Tween is used to animate ...
 */
@Serializable @SerialName("TweenTouchInput")
class TweenTouchInput private constructor(
    var enabled: Boolean? = null,

    override var target: Entity = Entity.NONE,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null
) : TweenBase, Poolable<TweenTouchInput> {
    // Init an existing data instance with data from another one
    override fun init(from: TweenTouchInput) {
        enabled = from.enabled

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
        enabled = null

        target = Entity.NONE
        delay = null
        duration = null
        easing = null
    }

    // Clone a new data instance from the pool
    override fun clone(): TweenTouchInput = pool.alloc().apply { init(from = this@TweenTouchInput ) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticTweenTouchInput(config: TweenTouchInput.() -> Unit ): TweenTouchInput =
            TweenTouchInput().apply(config)

        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.tweenTouchInput(config: TweenTouchInput.() -> Unit ) { tweens.add(pool.alloc().apply(config)) }

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "TweenTouchInput") { TweenTouchInput() }
    }
}
