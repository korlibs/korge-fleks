package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Animation Component data classes based on TweenBase
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 *
 * TODO: Make sure that list of tweens is deep copied into new TweenSequence component in TweenSequenceSystem
 */
@Serializable @SerialName("SpawnNewTweenSequence")
class SpawnNewTweenSequence private constructor(
    override var tweens: MutableList<TweenBase> = mutableListOf(),       // tween objects which contain entity and its properties to be animated in sequence

    override var target: Entity = Entity.NONE,    // not used
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase, TweenListBase {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: SpawnNewTweenSequence) {
        tweens = from.tweens  // List is static and elements do not change
        target = from.target
        delay = from.delay
        duration = from.duration
        easing = from.easing
        // Hint: it is not needed to copy "easing" property by creating new one like below:
        // easing = Easing.ALL[easing::class.toString().substringAfter('$')]
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    override fun free() {
        tweens.clear()
        target = Entity.NONE
        delay = null
        duration = null
        easing = null

        pool.free(this)
    }

    // This is called by TweenSequence component which owns all (static) tweens
    override fun freeRecursive() {
        // Do not put items back to the pool - we do not own them - The newly spawned TweenSequence is returning them to pool when
        // component is removed from the entity  -> no call to tweens.free()
        free()
    }

    companion object {
        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.spawnNewTweenSequence(config: SpawnNewTweenSequence.() -> Unit) {
            tweens.add(pool.alloc().apply(config))
        }

        private val pool = Pool(preallocate = 0) { SpawnNewTweenSequence() }
    }
}

