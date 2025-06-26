package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This Tween is used to animate ...
 */
@Serializable @SerialName("SendEvent")
class SendEvent private constructor(
    var event: Int = 0,                           // Set a specific event so that a Wait for event can be unlocked

    override var target: Entity = Entity.NONE,    // not used
    override var delay: Float? = null,            // Not used
    override var duration: Float? = null,         // not used
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase, Poolable<SendEvent> {
    // Init an existing data instance with data from another one
    override fun init(from: SendEvent) {
        event = from.event

        // target not used
        // delay not used
        // duration not used
        // easing not used
    }

    // Cleanup data instance manually
    // This is used for data instances when they are a value property of a component
    override fun cleanup() {
        event = 0

        // target not used
        // delay not used
        // duration not used
        // easing not used
    }

    // Clone a new data instance from the pool
    override fun clone(): SendEvent = pool.alloc().apply { init(from = this@SendEvent ) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticSendEvent(config: SendEvent.() -> Unit ): SendEvent =
            SendEvent().apply(config)

        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.sendEvent(config: SendEvent.() -> Unit ) { tweens.add(pool.alloc().apply(config)) }

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "SendEvent") { SendEvent() }
    }
}
