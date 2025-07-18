package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This Tween is used to animate ...
 */
@Serializable @SerialName("Jump")
class Jump private constructor(
    var distance: Int = 0,                        // Jump relative distance from current index (minus means jump back)
    var event: Int? = null,                       // Check for a specific event - if event is 0 than jump otherwise do not jump and tween execution continues with next tween in the list

    override var target: Entity = Entity.NONE,    // not used
    override var delay: Float? = null,            // not used
    override var duration: Float? = null,         // not used
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase, Poolable<Jump> {
    // Init an existing data instance with data from another one
    override fun init(from: Jump) {
        distance = from.distance
        event = from.event

        // target not used
        // delay not used
        // duration not used
        // easing not used
    }

    // Cleanup data instance manually
    // This is used for data instances when they are a value property of a component
    override fun cleanup() {
        distance = 0
        event = null

        // target not used
        // delay not used
        // duration not used
        // easing not used
    }

    // Clone a new data instance from the pool
    override fun clone(): Jump = pool.alloc().apply { init(from = this@Jump) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticJump(config: Jump.() -> Unit): Jump =
            Jump().apply(config)

        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.jump(config: Jump.() -> Unit) { tweens.add(pool.alloc().apply(config)) }

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "Jump") { Jump() }
    }
}
