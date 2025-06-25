package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This Tween is used to animate ...
 */
@Serializable @SerialName("Wait")
class Wait private constructor(
    var event: Int? = null,                           // Wait for a specific event if event is not "null" - need to unblock from infinite wait (Float.MAX_VALUE)

    override var target: Entity = Entity.NONE,        // not used
    override var delay: Float? = null,                // Not used
    override var duration: Float? = Float.MAX_VALUE,  // Use duration by setting explicitly a value
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase, Poolable<Wait> {
    // Init an existing data instance with data from another one
    override fun init(from: Wait) {
        event = from.event

        // target not used
        // delay not used
        duration = from.duration
        // easing not used
    }

    // Cleanup data instance manually
    // This is used for data instances when they are a value property of a component
    override fun cleanup() {
        event = null

        // target not used
        // delay not used
        duration = Float.MAX_VALUE
        // easing not used
    }

    // Clone a new data instance from the pool
    override fun clone(): Wait = pool.alloc().apply { init(from = this@Wait ) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticWait(config: Wait.() -> Unit ): Wait =
            Wait().apply(config)

        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.wait(config: Wait.() -> Unit ) { tweens.add(pool.alloc().apply(config)) }

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "Wait") { Wait() }
    }
}
