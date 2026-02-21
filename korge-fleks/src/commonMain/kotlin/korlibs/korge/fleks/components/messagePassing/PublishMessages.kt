package korlibs.korge.fleks.components.messagePassing

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.messagePassing.data.TxMsg.Companion.init
import korlibs.korge.fleks.components.messagePassing.data.TxMsg.Companion.cleanup
import korlibs.korge.fleks.components.messagePassing.data.TxMsg
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to signal that an entity wants to send a message to other entities.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("PublishMessages")
class PublishMessages private constructor(
    val listOfTxMsgs: MutableList<TxMsg> = mutableListOf()
) : PoolableComponent<PublishMessages>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are a value property of another component
    fun init(from: PublishMessages) {
        listOfTxMsgs.init(from.listOfTxMsgs)
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are a value property of another component
    fun cleanup() {
        listOfTxMsgs.cleanup()
    }

    override fun type() = PublishMessagesComponent

    companion object {
        val PublishMessagesComponent = componentTypeOf<PublishMessages>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticPublishMessagesComponent(config: PublishMessages.() -> Unit ): PublishMessages =
            PublishMessages().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun publishMessagesComponent(config: PublishMessages.() -> Unit ): PublishMessages =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "PublishMessages") { PublishMessages() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): PublishMessages = publishMessagesComponent { init(from = this@PublishMessages ) }

    // Initialize the component automatically when it is added to an entity
    override fun World.initComponent(entity: Entity) {
    }

    // Cleanup/Reset the component automatically when it is removed from an entity (component will be returned to the pool eventually)
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }

    // Initialize an external prefab when the component is added to an entity
    override fun World.initPrefabs(entity: Entity) {
    }

    // Cleanup/Reset an external prefab when the component is removed from an entity
    override fun World.cleanupPrefabs(entity: Entity) {
    }

    // Free the component and return it to the pool - this is called directly by the SnapshotSerializerSystem
    override fun free() {
        cleanup()
        pool.free(this)
    }

    /**
     * Adds a new [TxMsg] to the [listOfTxMsgs].
     */
    fun add(txMsg: TxMsg) {
        listOfTxMsgs.add(txMsg)
    }
}
