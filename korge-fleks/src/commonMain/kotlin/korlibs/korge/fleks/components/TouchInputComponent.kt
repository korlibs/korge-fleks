package korlibs.korge.fleks.components

import com.github.quillraven.fleks.ComponentType
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable @SerialName("TouchInput")
data class TouchInputComponent(
    var pressed: Boolean = false,
    var triggerImmediately: Boolean = false,
    var entityConfig: String = ""
) : CloneableComponent<TouchInputComponent>() {
    override fun type(): ComponentType<TouchInputComponent> = TouchInputComponent
    companion object : ComponentType<TouchInputComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): TouchInputComponent = this.copy()
}
