package korlibs.korge.fleks.components.messagePassing.data

import korlibs.korge.fleks.systems.MessagePassingSystem
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This class is used my the [MessagePassingSystem] component to save a list of entities which want to be informed when
 * a specific [TxMsg] type was sent.
 *
 * Hint: This class uses custom init and cleanup functions for its list of [RxMsg] elements to ensure
 *       proper deep copy and freeing of the poolable [RxMsg] instances.
 */
@Serializable @SerialName("ListOfRxMsg")
class ListOfRxMsg private constructor(
    val messages: MutableList<RxMsg> = mutableListOf()
) : Poolable<ListOfRxMsg> {
    // Init an existing data instance with data from another one
    override fun init(from: ListOfRxMsg) {
        messages.init(from.messages)
    }

    // Cleanup data instance manually
    // This is used for data instances when they are a value property of a component
    override fun cleanup() {
        messages.cleanup()
    }

    // Clone a new data instance from the pool
    override fun clone(): ListOfRxMsg = listOfRxMsg { init(from = this@ListOfRxMsg) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticListOfRxMsg(config: ListOfRxMsg.() -> Unit): ListOfRxMsg =
            ListOfRxMsg().apply(config)

        // Use this function to get a new instance of the data object from the pool
        fun listOfRxMsg(config: ListOfRxMsg.() -> Unit): ListOfRxMsg =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "ListOfRxMsg") { ListOfRxMsg() }

        /**
         * Init function (deep copy) for [MutableList] of [RxMsg] elements.
         * This will clone each rxMsg and add it to the list.
         */
        fun MutableList<RxMsg>.init(from: List<RxMsg>) {
            from.forEach { rxMsg ->
                this.add(rxMsg.clone())
            }
        }

        /**
         * Cleanup function for [MutableList] of [RxMsg] elements.
         * This will free each rxMsg and clear the list.
         */
        fun MutableList<RxMsg>.cleanup() {
            this.forEach { rxMsg ->
                rxMsg.free()
            }
            this.clear()
        }

        /**
         * Cleanup and remove the [RxMsg] at index [i] from the list.
         */
        fun MutableList<RxMsg>.cleanupAt(i: Int) {
            this[i].free()
            this.removeAt(i)
        }
    }
}