package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.TweenSequence.Companion.cleanup
import korlibs.korge.fleks.components.TweenSequence.TweenBase
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * TODO: currently not used
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("LoopTweens")
class LoopTweens private constructor(
    var tweens: List<TweenBase> = listOf(),       // tween objects which contain entity and its properties to be animated in a loop

    override var entity: Entity = Entity.NONE,    // not used
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase {
    // Init an existing tween data instance with data from another tween
    fun init(from: LoopTweens) {
        tweens = from.tweens
        entity = from.entity
        delay = from.delay
        duration = from.duration
        easing = from.easing
        // Hint: it is not needed to copy "easing" property by creating new one like below:
        // easing = Easing.ALL[easing::class.toString().substringAfter('$')]
    }

    // Cleanup the tween data instance manually
    override fun free() {
        tweens.cleanup()
        tweens = listOf()
        entity = Entity.NONE
        delay = null
        duration = null
        easing = null

        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of tween data as val inside a component (TODO: check if needed)
        fun staticLoopTweens(config: LoopTweens.() -> Unit ): LoopTweens =
            LoopTweens().apply(config)

        // Use this function to get a new instance of a tween from the pool and add it to a TweenSequence component
        fun LoopTweens(config: LoopTweens.() -> Unit ): LoopTweens =
            pool.alloc().apply(config)

        private val pool = Pool(preallocate = 0) { LoopTweens() }
    }
}
