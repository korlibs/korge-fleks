package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This Tween is used to animate ...
 */
@Serializable @SerialName("TweenRgba")
class TweenRgba private constructor(
    var alpha: Float? = null,
    var tint: Int? = null,
    var visible: Boolean? = null,

    override var target: Entity = Entity.NONE,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null
) : TweenBase, Poolable<TweenRgba> {
    // Init an existing data instance with data from another one
    override fun init(from: TweenRgba) {
        alpha = from.alpha
        tint = from.tint
        visible = from.visible

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
        alpha = null
        tint = null
        visible = null

        target = Entity.NONE
        delay = null
        duration = null
        easing = null
    }

    // Clone a new data instance from the pool
    override fun clone(): TweenRgba = pool.alloc().apply { init(from = this@TweenRgba ) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticTweenRgba(config: TweenRgba.() -> Unit ): TweenRgba =
            TweenRgba().apply(config)

        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.tweenRgba(config: TweenRgba.() -> Unit ) { tweens.add(pool.alloc().apply(config)) }

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "TweenRgba") { TweenRgba() }
    }
}
