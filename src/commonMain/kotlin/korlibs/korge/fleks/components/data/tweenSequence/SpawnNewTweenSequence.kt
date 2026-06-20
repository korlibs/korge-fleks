package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This Tween is used to spawn a new sequence of tweens. This tween sequence will be detached from the parent tween
 * sequence and executed in parallel to the parent sequence.
 */
@Serializable @SerialName("SpawnNewTweenSequence")
class SpawnNewTweenSequence private constructor(
    override var tweens: MutableList<TweenBase> = mutableListOf(),       // tween objects which contain entity and its properties to be animated in sequence

    override var target: Entity = Entity.NONE,    // not used
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase, TweenListBase, Poolable<SpawnNewTweenSequence> {
    // Init an existing data instance with data from another one
    override fun init(from: SpawnNewTweenSequence) {
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
    override fun clone(): SpawnNewTweenSequence = pool.alloc().apply { init(from = this@SpawnNewTweenSequence) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticSpawnNewTweenSequence(config: SpawnNewTweenSequence.() -> Unit): SpawnNewTweenSequence =
            SpawnNewTweenSequence().apply(config)

        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.spawnNewTweenSequence(config: SpawnNewTweenSequence.() -> Unit) { tweens.add(pool.alloc().apply(config)) }

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "SpawnNewTweenSequence") { SpawnNewTweenSequence() }
    }
}
