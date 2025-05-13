package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.TweenSequence.TweenBase
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Animation Component data classes based on TweenBase
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("SpawnNewTweenSequence")
class SpawnNewTweenSequence private constructor(
    var tweens: List<TweenBase> = listOf(),       // tween objects which contain entity and its properties to be animated in sequence

    override var entity: Entity = Entity.NONE,    // not used
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: SpawnNewTweenSequence) {
        tweens = from.tweens  // List is static and elements do not change
        entity = from.entity
        delay = from.delay
        duration = from.duration
        easing = from.easing
        // Hint: it is not needed to copy "easing" property by creating new one like below:
        // easing = Easing.ALL[easing::class.toString().substringAfter('$')]
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    override fun free() {
        // Do not put items back to the pool - we do not own them - TweenSequence is returning them to pool when
        // component is removed from the entity
        tweens = listOf()
        entity = Entity.NONE
        delay = null
        duration = null
        easing = null

        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of component data as val inside another component
        fun staticSpawnNewTweenSequence(config: SpawnNewTweenSequence.() -> Unit ): SpawnNewTweenSequence =
            SpawnNewTweenSequence().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun SpawnNewTweenSequence(config: SpawnNewTweenSequence.() -> Unit ): SpawnNewTweenSequence =
            pool.alloc().apply(config)

        private val pool = Pool(preallocate = 0) { SpawnNewTweenSequence() }
    }
}

/*
import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.TweenSequence.TweenBase
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable @SerialName("$DATA$")
class $DATA$ private constructor(
    var value: ...

    override var entity: Entity = Entity.NONE,    // not used
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: $DATA$) {
        value = ...
        entity = from.entity
        delay = from.delay
        duration = from.duration
        easing = from.easing
        // Hint: it is not needed to copy "easing" property by creating new one like below:
        // easing = Easing.ALL[easing::class.toString().substringAfter('$')]
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    override fun free() {
        value = ...
        entity = Entity.NONE
        delay = null
        duration = null
        easing = null

        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of component data as val inside another component
        fun static$DATA$(config: $DATA$.() -> Unit ): $DATA$ =
            $DATA$().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun $DATA$(config: $DATA$.() -> Unit ): $DATA$ =
            pool.alloc().apply(config)

        private val pool = Pool(preallocate = 0) { $DATA$() }
    }
}
 */
