package korlibs.korge.fleks.components.messagePassing.tweens

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.data.tweenSequence.TweenBase
import korlibs.korge.fleks.components.data.tweenSequence.TweenListBase
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This Tween is used to subscribe to a message type with an optional entityConfig and lifeTime.
 *
 * @param type The message type to subscribe to.
 * @param entityConfig Optional entity configuration associated with the subscription.
 * @param remainingMsgs Optional number of messages to receive before unsubscribing. If null, the subscription is indefinite.
 *                      If set to a specific number, the subscription will automatically unsubscribe after receiving that many messages.
 */
@Serializable @SerialName("TweenSubscribeMessage")
class TweenSubscribeMessage private constructor(
    var type: Int = 0,
    var entityConfig: String? = null,
    var remainingMsgs: Int? = null,

    override var target: Entity = Entity.NONE,  // not used
    override var delay: Float? = null,
    override var duration: Float? = null,       // not used
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null
) : TweenBase, Poolable<TweenSubscribeMessage> {
    // Init an existing data instance with data from another one
    override fun init(from: TweenSubscribeMessage) {
        type = from.type
        entityConfig = from.entityConfig
        remainingMsgs = from.remainingMsgs

        target = from.target
        delay = from.delay
        duration = from.duration
        easing = from.easing
        // Hint: it is not needed to copy "easing" property by creating new one like below:
        // easing = Easing.ALL[easing::class.toString().substringAfter('$')]
    }

    // Cleanup data instance manually
    // This is used for data instances when they are a value property of a component
    override fun cleanup() {
        type = 0
        entityConfig = null
        remainingMsgs = 0

        target = Entity.NONE
        delay = null
        duration = null
        easing = null
    }

    // Clone a new data instance from the pool
    override fun clone(): TweenSubscribeMessage = pool.alloc().apply { init(from = this@TweenSubscribeMessage ) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticTweenSubscribeMessage(config: TweenSubscribeMessage.() -> Unit ): TweenSubscribeMessage =
            TweenSubscribeMessage().apply(config)

        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.tweenSubscribeMessage(config: TweenSubscribeMessage.() -> Unit ) { tweens.add(pool.alloc().apply(config)) }

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "TweenSubscribeMessage") { TweenSubscribeMessage() }
    }
}