package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.datastructure.FastIntMap
import korlibs.korge.fleks.components.Event.Companion.EventComponent
import korlibs.korge.fleks.components.SubscribedMessages.Companion.SubscribedMessagesComponent
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


typealias MessageStore = FastIntMap<EntityConfig>


/**
 * This class is used to ...
 */
@Serializable @SerialName("EntityMsg")
class EntityMsg private constructor(
    val entities: MutableList<Entity> = mutableListOf(),
    var entityConfig: String = ""
) : Poolable<EntityMsg> {
    // Init an existing data instance with data from another one
    override fun init(from: EntityMsg) {
        entities.addAll(from.entities)
        entityConfig = from.entityConfig
    }

    // Cleanup data instance manually
    // This is used for data instances when they are a value property of a component
    override fun cleanup() {
        entities.clear()
        entityConfig = ""
    }

    // Clone a new data instance from the pool
    override fun clone(): EntityMsg = entityMsg { init(from = this@EntityMsg) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticEntityMsg(config: EntityMsg.() -> Unit): EntityMsg =
            EntityMsg().apply(config)

        // Use this function to get a new instance of the data object from the pool
        fun entityMsg(config: EntityMsg.() -> Unit): EntityMsg =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "EntityMsg") { EntityMsg() }
    }
}




/**
 * This system implements sending messages between entities. Entities can subscribe to
 * specific type of messages. Entities can publish messages. When a message is published,
 * all entities subscribed to that message will have the specific eventConfig executed.
 *
 * Use MessagePassingSystem for scenarios where entities need to communicate with each other
 * without direct references, promoting loose coupling and flexibility in entity interactions.
 *
 * TODO describe used components
 */
class MessagePassingSystem : IteratingSystem(
    family = family { all(PublishMessagesComponent) },
    interval = Fixed(1 / 60f)
) {
    override fun onTickEntity(entity: Entity) {



        val subscribesMessagesComponent = entity[SubscribedMessagesComponent]

        subscribesMessagesComponent.messages.forEach { (messageType, _) ->
            // Clear previous messages
            messageStore.remove(messageType)
        }

/*
        // Set event if the entity is publishing it
        if (subscribesMessagesComponent.publish) {
            messageStore[0] = ""
        }

        // Run the specific event config function on the entity which is subscribed to this event
        if (subscribesMessagesComponent.subscribe && eventMap[subscribesMessagesComponent.event]) {
            world.configureEntity(subscribesMessagesComponent.eventConfig, entity)
        }
*/
    }
}

class  {



    companion object {
        const val CAMERA_FOLLOW = 0
    }

    // Event bitmap
    private val messageStore: MessageStore = FastIntMap()

    /**
     * Adds the message passing systems to the world configuration.
     */
    fun WorldConfiguration.addMessagePassingSystem() {
        systems {
            // First system will publish messages from entities
            add(MessagePassingPublishSystem(messageStore))
            // Second system will inform subscribed entities about published messages
            add(MessagePassingSubscribeSystem(messageStore))
        }
    }


    class MessagePassingSubscribeSystem(
        private val messageStore: MessageStore
    ) : IteratingSystem(
        family = family { all(EventComponent) },
        interval = Fixed(1 / 60f)
    ) {

        override fun onTickEntity(entity: Entity) {
            val eventComponent = entity[EventComponent]

            // Set event if the entity is publishing it
            if (eventComponent.publish) {
                eventMap.set(eventComponent.event)
            }

            // Run the specific event config function on the entity which is subscribed to this event
            if (eventComponent.subscribe && eventMap[eventComponent.event]) {
                world.configureEntity(eventComponent.eventConfig, entity)
            }
        }
    }
}