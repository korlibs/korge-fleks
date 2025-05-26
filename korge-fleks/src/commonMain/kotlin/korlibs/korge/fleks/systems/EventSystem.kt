package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.collection.BitArray
import korlibs.korge.fleks.components.Event.Companion.EventComponent
import korlibs.korge.fleks.utils.*

/**
 * Event system which implements a publish-subscribe mechanism to trigger execution of specific createEntity functions.
 *
 * Hint: Not yet used in KorGE-Fleks.
 */
class EventSystem : IteratingSystem(
    family = family { all(EventComponent) },
    interval = EachFrame
) {
    // Event bitmap
    private val eventMap: BitArray = BitArray()

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
