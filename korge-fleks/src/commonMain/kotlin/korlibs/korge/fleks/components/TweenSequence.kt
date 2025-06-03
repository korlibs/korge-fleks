package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.data.tweenSequence.TweenBase
import korlibs.korge.fleks.components.data.tweenSequence.TweenListBase
import korlibs.korge.fleks.components.data.tweenSequence.free
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component holds all needed details to animate properties of components of entities.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("TweenSequence")
class TweenSequence private constructor(
    override var tweens: MutableList<TweenBase> = mutableListOf(),

    // Internal runtime data
    var index: Int = 0,              // This points to the animation step which is currently in progress
    var timeProgress: Float = 0f,    // Elapsed time for the object to be animated
    var waitTime: Float = 0f,
    var executed: Boolean = false
) : PoolableComponents<TweenSequence>(), TweenListBase {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: TweenSequence) {
        tweens = from.tweens  // List is static and elements do not change
        index = from.index
        timeProgress = from.timeProgress
        waitTime = from.waitTime
        executed = from.executed
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    // Usually not needed to be called directly - cleanup will be called by component life cycle of Korge-fleks
    fun cleanup() {
        // We own the static list of tweens - Thus, we need to put them back to the pool
        tweens.free()

        tweens.clear()
        index = 0
        timeProgress = 0f
        waitTime = 0f
        executed = false
    }

    // Usually not needed to be called directly - cleanup will be called by component life cycle of Korge-fleks
    override fun freeRecursive() { cleanup() }

    override fun type() = TweenSequenceComponent

    companion object {
        val TweenSequenceComponent = componentTypeOf<TweenSequence>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticTweenSequenceComponent(config: TweenSequence.() -> Unit ): TweenSequence =
            TweenSequence().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun World.TweenSequenceComponent(config: TweenSequence.() -> Unit ): TweenSequence =
            getPoolable(TweenSequenceComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addTweenSequenceComponentPool(preAllocate: Int = 0) {
            addPool(TweenSequenceComponent, preAllocate) { TweenSequence() }
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): TweenSequence =
        getPoolable(TweenSequenceComponent).apply { init(from = this@TweenSequence ) }

    // Initialize the component automatically when it is added to an entity
    override fun World.initComponent(entity: Entity) {
        // Initialize internal waitTime property with delay value of first tween if available.
        if (tweens.isNotEmpty()) waitTime = tweens[index].delay ?: 0f
    }

    // Cleanup/Reset the component automatically when it is removed from an entity (component will be returned to the pool eventually)
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }
}
