package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * TODO: currently not used
 *
 * This Tween is used to hold a list of tweens that are executed in a loop. Forever.
 */
@Serializable @SerialName("LoopTweens")
class LoopTweens private constructor(
    override var tweens: MutableList<TweenBase> = mutableListOf(),       // tween objects which contain entity and its properties to be animated in a loop

    override var target: Entity = Entity.NONE,    // not used
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase, TweenListBase, Poolable<LoopTweens> {
    // Init an existing data instance with data from another one
    override fun init(from: LoopTweens) {
        tweens.init(from.tweens)

        // target not used
        delay = from.delay
        duration = from.duration
        // easing not used
    }

    // Cleanup data instance manually
    // This is used for data instances when they are a value property of a component
    override fun cleanup() {
        tweens.freeAndClear()

        // target not used
        delay = null
        duration = null
        // easing not used
    }

    // Clone a new data instance from the pool
    override fun clone(): LoopTweens = pool.alloc().apply { init(from = this@LoopTweens) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticLoopTweens(config: LoopTweens.() -> Unit): LoopTweens =
            LoopTweens().apply(config)

        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.loopTweens(config: LoopTweens.() -> Unit) { tweens.add(pool.alloc().apply(config)) }

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "LoopTweens") { LoopTweens() }
    }
}
