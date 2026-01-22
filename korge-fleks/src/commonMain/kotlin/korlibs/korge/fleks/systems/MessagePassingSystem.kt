package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.messagePassing.PublishMessages.Companion.PublishMessagesComponent
import korlibs.korge.fleks.prefab.SystemRuntimeConfigs
import korlibs.korge.fleks.utils.*


// Message types
const val RELEASE_CAMERA = 1
const val ATTACH_CAMERA = 2

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
    private val systemRuntimeConfigs = world.inject<SystemRuntimeConfigs>("SystemRuntimeConfigs")

    override fun onTickEntity(entity: Entity) {
        val senderEntity = entity  // name alias for better readability
        // Get subscribed messages info from runtime config
        val subscribesMessagesComponent = systemRuntimeConfigs.getMessagePassingConfig(world) ?: return

        val publishMessagesComponent = senderEntity[PublishMessagesComponent]

        // Do the message send "procedure"
        publishMessagesComponent.listOfTxMsgs.forEach { txMsg ->
            val msgEvent = txMsg.event
            val senderEntityConfig = txMsg.entityConfig

            // Check for each published message type if a receiver has subscribed
            subscribesMessagesComponent.rxMessagesByEvent[msgEvent]?.messages?.forEach { message ->
                // Execute given entityConfig on all entities which have subscribed to this message
                val receiverEntity = message.entity
                // Part (1) message source knows what the message destination needs
                senderEntityConfig?.let { senderEntityConfig ->
                    world.configureEntity(senderEntityConfig, receiverEntity)
                }
                // Part (2) message destination knows what to do when this message arrives
                message.entityConfig?.let { receiverEntityConfig ->
                    world.configureEntity(receiverEntityConfig, receiverEntity)
                }
                // Check if we need to unsubscribe the receiver entity automatically
                message.remainingMsgs?.let { remainingMsgs ->
                    if (remainingMsgs > 1) {
                        message.remainingMsgs = remainingMsgs - 1
                    } else {
                        // Unsubscribe - remove message from the list
                        subscribesMessagesComponent.rxMessagesByEvent[msgEvent]?.messages?.remove(message)
                    }
                }
            }
        }

        // Cleanup - delete publish messages entity after all messages have been sent
        world -= entity
    }
}
