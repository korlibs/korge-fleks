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
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("Event")
class Event private constructor(
    var publish: Boolean = false,
    var subscribe: Boolean = false,
    var event: Int = 0,
    var eventConfig: String = ""
) : Poolable<Event>() {
    override fun type() = EventComponent

    companion object {
        val EventComponent = componentTypeOf<Event>()

        fun World.EventComponent(config: Event.() -> Unit ): Event =
            getPoolable(EventComponent).apply { config() }

        fun InjectableConfiguration.addEventComponentPool(preAllocate: Int = 0) {
            addPool(EventComponent, preAllocate) { Event() }
        }
    }

    override fun World.clone(): Event =
        getPoolable(EventComponent).apply {
            publish = this@Event.publish
            subscribe = this@Event.subscribe
            event = this@Event.event
            eventConfig = this@Event.eventConfig
        }

    override fun World.cleanupComponent(entity: Entity) {
        publish = false
        subscribe = false
        event = 0
        eventConfig = ""
    }
}