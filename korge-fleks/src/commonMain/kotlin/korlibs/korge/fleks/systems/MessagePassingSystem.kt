package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.messagePassing.MessagePassingConfig.Companion.MessagePassingConfigComponent
import korlibs.korge.fleks.components.messagePassing.PublishMessages.Companion.PublishMessagesComponent
import korlibs.korge.fleks.utils.*


// Message types
const val RELEASE_CAMERA = 1

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
        val runtimeConfigEntity: Entity = world.getMessagePassingEntity()
        val publishMessagesComponent = entity[PublishMessagesComponent]  // senderEntity
        val subscribesMessagesComponent = runtimeConfigEntity[MessagePassingConfigComponent]

        // Do the message send "procedure"
        publishMessagesComponent.listOfTxMsgs.forEach { txMsg ->
            val msgType = txMsg.type
            val senderEntityConfig = txMsg.entityConfig

            // Check for each published message type if a receiver has subscribed
            subscribesMessagesComponent.rxMessagesByMsgType[msgType]?.let { message ->
                // Execute given entityConfig on all entities which have subscribed to this message
                message.entities.forEach { receiverEntity ->
                    // Part (1) message source knows what the message destination needs
                    senderEntityConfig?.let { senderEntityConfig ->
                        world.configureEntity(senderEntityConfig, receiverEntity)
                    }
                    // Part (2) message destination knows what to do when this message arrives
                    message.entityConfig?.let { receiverEntityConfig ->
                        world.configureEntity(receiverEntityConfig, receiverEntity)
                    }
                }
            }
        }

        // Cleanup - delete component after all messages have be sent
        entity.configure { it -= PublishMessagesComponent }
    }
}
