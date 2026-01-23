package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.TweenSequence.Companion.TweenSequenceComponent
import korlibs.korge.fleks.components.messagePassing.PublishMessages.Companion.PublishMessagesComponent
import korlibs.korge.fleks.components.messagePassing.data.ListOfRxMsg.Companion.cleanupAt
import korlibs.korge.fleks.prefab.SystemRuntimeConfigs
import korlibs.korge.fleks.utils.*


/**
 * This system implements sending messages between entities. Entities can subscribe to
 * specific message events. Entities can publish message events. When a message is published,
 * all entities subscribed to that message will have the specific eventConfig executed and/or
 * a wait in a [TweenSequence] will be released.
 *
 * Use MessagePassingSystem for scenarios where entities need to communicate with each other
 * without direct references, promoting loose coupling and flexibility in entity interactions.
 *
 * Involved Components:
 * - [PublishMessagesComponent]: Attached to entities that want to publish messages.
 * - [SystemRuntimeConfigs]: Holds the runtime configuration for message subscriptions for the message passing system.
 * - [TweenSequenceComponent]: Used to manage tween sequences, including wait states that can be released upon message receipt.
 */
class MessagePassingSystem : IteratingSystem(
    family = family { all(PublishMessagesComponent) },
    interval = Fixed(1 / 60f)
) {
    private val systemRuntimeConfigs = world.inject<SystemRuntimeConfigs>("SystemRuntimeConfigs")

    override fun onTickEntity(entity: Entity) {
        val senderEntity = entity  // name alias for better readability
        // Get subscribed messages info from runtime config
        val messagePassingConfigComponent = systemRuntimeConfigs.getMessagePassingConfig(world) ?: return

        val publishMessagesComponent = senderEntity[PublishMessagesComponent]

        // Do the message send "procedure"
        publishMessagesComponent.listOfTxMsgs.forEach { txMsg ->
            val msgEvent = txMsg.event
            val senderEntityConfig = txMsg.entityConfig

            // Check for each published message type if a receiver has subscribed
            messagePassingConfigComponent.rxMessagesByEvent[msgEvent]?.messages?.let { rxMessagesList ->
                rxMessagesList.forEach { message ->
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
                    // Release wait state if required - this is used in tween sequences to wait for a message
                    if (message.releaseWait && receiverEntity has TweenSequenceComponent) {
                        val tweenSequenceComponent = receiverEntity[TweenSequenceComponent]
                        tweenSequenceComponent.waitTime = 0f
                    }
                    // If not unlimited messages, decrease counter - cleanup must be done OUTSIDE of this loop
                    if (message.remainingMsgs > 0) {
                        message.remainingMsgs -= 1
                    }
                }

                // Remove all messages which have remainingMsgs == 0 (means auto-unsubscribe)
                for (i in rxMessagesList.size -1 downTo 0) {
                    if (rxMessagesList[i].remainingMsgs == 0) {
                        rxMessagesList.cleanupAt(i)
                    }
                }
            }
        }

        // Cleanup - delete publish messages entity after all message events have been sent
        world -= entity
    }
}
