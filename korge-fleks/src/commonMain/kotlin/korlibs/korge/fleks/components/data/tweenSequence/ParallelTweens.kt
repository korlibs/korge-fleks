package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable @SerialName("ParallelTweens")
class ParallelTweens private constructor(
    override var tweens: MutableList<TweenBase> = mutableListOf(),       // tween objects which contain entity and its properties to be animated in parallel
    // We have List with static tweens and pass ownership to TweenSequenceComponent for deleting the tweens of a list when the component is removed from an entity

    override var target: Entity = Entity.NONE,    // not used
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase, TweenListBase {
    // Init an existing tween data instance with data from another tween
    fun init(from: ParallelTweens) {
        tweens = from.tweens  // List is static and elements do not change
        delay = from.delay
        duration = from.duration
        // Hint: it is not needed to copy "easing" property by creating new one like below:
        // easing = Easing.ALL[easing::class.toString().substringAfter('$')]
    }

    // Cleanup the tween data instance manually
    override fun free() {
        // Do not put items back to the pool - we do not own them - TweenSequence is returning them to pool when
        // component is removed from the entity
        tweens.clear()
        delay = null
        duration = null

        pool.free(this)
    }

    // This is called by TweenSequence component which owns all (static) tweens
    override fun freeRecursive() {
        tweens.free()
        free()
    }

    companion object {
        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.parallelTweens(config: ParallelTweens.() -> Unit) {
            tweens.add(pool.alloc().apply(config))
        }

        // Use this function to create a new instance of this data class as static value property
        fun staticParallelTweens(): ParallelTweens = ParallelTweens()

        private val pool = Pool(preallocate = 0) { ParallelTweens() }
    }
}
