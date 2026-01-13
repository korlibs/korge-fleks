package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.componentTypeOf
import korlibs.korge.fleks.systems.EntityMsg
import korlibs.korge.fleks.utils.AppConfig
import korlibs.korge.fleks.utils.Pool
import korlibs.korge.fleks.utils.PoolableComponent
import korlibs.korge.fleks.systems.MessagePassingSystem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to store messages that an entity is subscribed to.
 * It is used by [MessagePassingSystem] to store entity which have subscribed to a
 * specific message type.
 * Message types are simple integer numbers. They map to [EntityMsg] instances.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("SubscribedMessages")
class SubscribedMessages private constructor(
    val messages: MutableMap<Int, EntityMsg> = mutableMapOf()
) : PoolableComponent<SubscribedMessages>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are a value property of another component
    fun init(from: SubscribedMessages) {
        messages.putAll(from.messages)
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are a value property of another component
    fun cleanup() {
        messages.clear()
    }

    override fun type() = SubscribedMessagesComponent

    companion object {
        val SubscribedMessagesComponent = componentTypeOf<SubscribedMessages>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticSubscribedMessagesComponent(config: SubscribedMessages.() -> Unit ): SubscribedMessages =
            SubscribedMessages().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun subscribedMessagesComponent(config: SubscribedMessages.() -> Unit ): SubscribedMessages =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "SubscribedMessages") { SubscribedMessages() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): SubscribedMessages = subscribedMessagesComponent { init(from = this@SubscribedMessages ) }

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
}
