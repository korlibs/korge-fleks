package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


  /**
 * This Tween is used to hold a list of tweens that are executed in parallel.
 */
@Serializable @SerialName("ParallelTweens")
class ParallelTweens private constructor(
    override var tweens: MutableList<TweenBase> = mutableListOf(),       // tween objects which contain entity and its properties to be animated in parallel
    // We have List with static tweens and pass ownership to TweenSequenceComponent for deleting the tweens of a list when the component is removed from an entity

    override var target: Entity = Entity.NONE,    // not used
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase, TweenListBase, Poolable<ParallelTweens> {
    // Init an existing data instance with data from another one
    override fun init(from: ParallelTweens) {
        tweens.init(from.tweens)

        target = from.target
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
    override fun clone(): ParallelTweens = pool.alloc().apply { init(from = this@ParallelTweens ) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticParallelTweens(config: ParallelTweens.() -> Unit ): ParallelTweens =
            ParallelTweens().apply(config)

        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.parallelTweens(config: ParallelTweens.() -> Unit ) { tweens.add(pool.alloc().apply(config)) }

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "ParallelTweens") { ParallelTweens() }
    }
}
