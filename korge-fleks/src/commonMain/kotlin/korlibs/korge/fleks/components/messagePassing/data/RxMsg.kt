package korlibs.korge.fleks.components.messagePassing.data

import com.github.quillraven.fleks.Entity
import korlibs.korge.fleks.utils.AppConfig
import korlibs.korge.fleks.utils.Pool
import korlibs.korge.fleks.utils.Poolable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This class is used by [ListOfRxMsg] to save data for a rx message. Each rx message contains:
 * - an entity which wants to receive a specific message type.
 * - an optional entityConfig string to configure the entity when the message was received.
 * - an optional remainingMsgs counter to limit the number of messages to receive before unsubscribing automatically.
 */
@Serializable
@SerialName("RxMsg")
class RxMsg private constructor(
    var entity: Entity = Entity.NONE,
    var entityConfig: String? = null,
    var remainingMsgs: Int? = null
) : Poolable<RxMsg> {
    // Init an existing data instance with data from another one
    override fun init(from: RxMsg) {
        entity = from.entity
        entityConfig = from.entityConfig
        remainingMsgs = from.remainingMsgs
    }

    // Cleanup data instance manually
    // This is used for data instances when they are a value property of a component
    override fun cleanup() {
        entity = Entity.NONE
        entityConfig = null
        remainingMsgs = null
    }

    // Clone a new data instance from the pool
    override fun clone(): RxMsg = rxMsg { init(from = this@RxMsg) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticRxMsg(config: RxMsg.() -> Unit): RxMsg =
            RxMsg().apply(config)

        // Use this function to get a new instance of the data object from the pool
        fun rxMsg(config: RxMsg.() -> Unit): RxMsg =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "RxMsg") { RxMsg() }
    }
}