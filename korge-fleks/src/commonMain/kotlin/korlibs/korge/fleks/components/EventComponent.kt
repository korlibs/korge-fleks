package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


/**
 * Component which is used to implement a publish-subscribe event/message passing system.
 * It is used by [EventSystem].
 *
 * Hint: Not yet used in KorGE-Fleks.
 */
@Serializable @SerialName("Event")
data class EventComponent(
    val publish: Boolean = false,
    val subscribe: Boolean = false,
    val event: Int = 0,
    val eventConfig: String = ""
) : CloneableComponent<EventComponent>() {
    override fun type(): ComponentType<EventComponent> = EventComponent
    companion object : ComponentType<EventComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): EventComponent = this.copy()
}
