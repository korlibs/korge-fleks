package korlibs.korge.fleks.components.messagePassing

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.componentTypeOf
import korlibs.korge.fleks.components.data.RxMsg
import korlibs.korge.fleks.utils.AppConfig
import korlibs.korge.fleks.utils.Pool
import korlibs.korge.fleks.utils.PoolableComponent
import korlibs.korge.fleks.systems.MessagePassingSystem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to store the runtime information for the [MessagePassingSystem].
 * It is used to store a list of entities which have subscribed to a specific message type.
 * The message information is stored within an entityConfig object.
 * Message types are simple integer numbers. They map to [Message] instances.
 *
 * Key of map is the message type (Int).
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("MessagePassingConfig")
class MessagePassingConfig private constructor(
    val rxMessagesByMsgType: MutableMap<Int, RxMsg> = mutableMapOf()
) : PoolableComponent<MessagePassingConfig>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are a value property of another component
    fun init(from: MessagePassingConfig) {
        rxMessagesByMsgType.putAll(from.rxMessagesByMsgType)
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are a value property of another component
    fun cleanup() {
        rxMessagesByMsgType.clear()
    }

    override fun type() = MessagePassingConfigComponent

    companion object {
        val MessagePassingConfigComponent = componentTypeOf<MessagePassingConfig>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticMessagePassingConfigComponent(config: MessagePassingConfig.() -> Unit ): MessagePassingConfig =
            MessagePassingConfig().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun messagePassingConfigComponent(config: MessagePassingConfig.() -> Unit ): MessagePassingConfig =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "MessagePassingConfig") { MessagePassingConfig() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): MessagePassingConfig = messagePassingConfigComponent { init(from = this@MessagePassingConfig ) }

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