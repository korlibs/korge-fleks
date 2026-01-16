package korlibs.korge.fleks.components.messagePassing.data

import korlibs.korge.fleks.utils.AppConfig
import korlibs.korge.fleks.utils.Pool
import korlibs.korge.fleks.utils.Poolable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This class is used to store message type and (to be executed) entityConfig in a [PublishMessage] component.
 */
@Serializable
@SerialName("TxMsg")
class TxMsg private constructor(
    var type: Int = 0,
    var entityConfig: String? = null
) : Poolable<TxMsg> {
    // Init an existing data instance with data from another one
    override fun init(from: TxMsg) {
        type = from.type
        entityConfig = from.entityConfig
    }

    // Cleanup data instance manually
    // This is used for data instances when they are a value property of a component
    override fun cleanup() {
        type = 0
        entityConfig = null
    }

    // Clone a new data instance from the pool
    override fun clone(): TxMsg = txMsg { init(from = this@TxMsg ) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticTxMsg(config: TxMsg.() -> Unit ): TxMsg =
            TxMsg().apply(config)

        // Use this function to get a new instance of the data object from the pool
        fun txMsg(config: TxMsg.() -> Unit ): TxMsg =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "TxMsg") { TxMsg() }

        /**
         * Init function (deep copy) for [MutableList] of [TxMsg] elements.
         * This will clone each txMsg and add it to the list.
         */
        fun MutableList<TxMsg>.init(from: List<TxMsg>) {
            from.forEach { txMsg ->
                this.add(txMsg.clone())
            }
        }

        /**
         * Free all [TxMsg]'s in the list and clear the list.
         * This will free each txMsg and clear the list.
         */
        fun MutableList<TxMsg>.cleanup() {
            this.forEach { txMsg ->
                txMsg.free()
            }
            this.clear()
        }
    }
}