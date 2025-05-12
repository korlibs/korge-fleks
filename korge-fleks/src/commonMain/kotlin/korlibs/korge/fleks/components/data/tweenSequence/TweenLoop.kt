package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
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
class TweenLoop private constructor(
    val tweens: List<TweenBase> = listOf(),       // tween objects which contain entity and its properties to be animated in a loop

    override var entity: Entity = Entity.NONE,    // not used
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : Poolable<TweenLoop>(), TweenBase {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun World.init(from: TweenLoop) {
        answer = from.answer
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun World.cleanup() {
        answer = 42
    }

    override fun type() = TweenLoopComponent

    companion object {
        val TweenLoopComponent = componentTypeOf<TweenLoop>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticTweenLoopComponent(config: TweenLoop.() -> Unit ): TweenLoop =
            TweenLoop().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun World.TweenLoopComponent(config: TweenLoop.() -> Unit ): TweenLoop =
            getPoolable(TweenLoopComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addTweenLoopComponentPool(preAllocate: Int = 0) {
            addPool(TweenLoopComponent, preAllocate) { TweenLoop() }
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): TweenLoop =
        getPoolable(TweenLoopComponent).apply { init(from = this@TweenLoop ) }

    // Initialize the component automatically when it is added to an entity
    override fun World.initComponent(entity: Entity) {
    }

    // Cleanup/Reset the component automatically when it is removed from an entity (component will be returned to the pool eventually)
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }

    // Initialize an external prefab when the component is added to an entity
    override fun World.initPrefabs(entity: Entity) {
    }

    // Cleanup/Reset an external prefab when the component is removed from an entity
    override fun World.cleanupPrefabs(entity: Entity) {
    }
}

// TODO not used
@Serializable @SerialName("LoopTweens")
data class LoopTweens(
) : TweenBase {
    override fun clone(): LoopTweens {
        val copyOfTweens: MutableList<TweenBase> = mutableListOf()
        // Perform special deep copy of list elements
        tweens.forEach { element -> copyOfTweens.add(element.clone()) }

        return this.copy(
            tweens = copyOfTweens,
            entity = entity.clone(),
            easing = easing
        )
    }
}
