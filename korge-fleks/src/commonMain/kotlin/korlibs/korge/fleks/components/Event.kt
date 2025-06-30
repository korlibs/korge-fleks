package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Component which is used to implement a publish-subscribe event/message passing system.
 * It is used by [EventSystem].
 *
 * Hint: Not yet used in KorGE-Fleks.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("Event")
class Event private constructor(
    var publish: Boolean = false,
    var subscribe: Boolean = false,
    var event: Int = 0,
    var eventConfig: String = ""
) : PoolableComponent<Event>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: Event) {
        publish = from.publish
        subscribe = from.subscribe
        event = from.event
        eventConfig = from.eventConfig
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        publish = false
        subscribe = false
        event = 0
        eventConfig = ""
    }

    override fun type() = EventComponent

    companion object {
        val EventComponent = componentTypeOf<Event>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticEventComponent(config: Event.() -> Unit ): Event =
            Event().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun eventComponent(config: Event.() -> Unit ): Event =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "Event") { Event() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): Event = eventComponent { init(from = this@Event ) }

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
