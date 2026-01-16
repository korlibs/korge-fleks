package korlibs.korge.fleks.components.messagePassing.tweens

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.data.tweenSequence.TweenBase
import korlibs.korge.fleks.components.data.tweenSequence.TweenListBase
import korlibs.korge.fleks.components.messagePassing.PublishMessages.Companion.PublishMessagesComponent
import korlibs.korge.fleks.components.messagePassing.PublishMessages.Companion.publishMessagesComponent
import korlibs.korge.fleks.components.messagePassing.data.TxMsg.Companion.txMsg
import korlibs.korge.fleks.utils.AppConfig
import korlibs.korge.fleks.utils.EasingAsString
import korlibs.korge.fleks.utils.Pool
import korlibs.korge.fleks.utils.Poolable
import korlibs.korge.fleks.utils.createEntity
import korlibs.math.interpolation.Easing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This Tween is used to set the message type and an entityConfig for a message which shall be published.
 */
@Serializable @SerialName("TweenPublishMessage")
class TweenPublishMessage private constructor(
    var type: Int = 0,
    var entityConfig: String? = null,

    override var target: Entity = Entity.NONE,  // not used
    override var delay: Float? = null,
    override var duration: Float? = null,       // not used
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null
) : TweenBase, Poolable<TweenPublishMessage> {
    // Init an existing data instance with data from another one
    override fun init(from: TweenPublishMessage) {
        type = from.type
        entityConfig = from.entityConfig

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

        target = Entity.NONE
        delay = null
        duration = null
        easing = null
    }

    // Clone a new data instance from the pool
    override fun clone(): TweenPublishMessage = pool.alloc().apply { init(from = this@TweenPublishMessage ) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticTweenPublishMessage(config: TweenPublishMessage.() -> Unit ): TweenPublishMessage =
            TweenPublishMessage().apply(config)

        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.tweenPublishMessage(config: TweenPublishMessage.() -> Unit ) { tweens.add(pool.alloc().apply(config)) }

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "TweenPublishMessage") { TweenPublishMessage() }

        fun World.createMsgPublishEntity(msgType: Int, msgEntityConfig: String?) {
            createEntity("TweenPublishMessage").configure { txEntity ->
                txEntity.getOrAdd(PublishMessagesComponent) { publishMessagesComponent {} }.add(txMsg {
                    type = msgType                  // set message type
                    entityConfig = msgEntityConfig  // set (possibly) entityConfig string which shall be executed on publish message
                })
            }
        }
    }
}