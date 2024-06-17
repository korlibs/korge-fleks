package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable @SerialName("InputTouchButton")
data class InputTouchButtonComponent(
    var pressed: Boolean = false,
    var triggerImmediately: Boolean = false,
    var entityConfig: String = "",
) : CloneableComponent<InputTouchButtonComponent>() {
    override fun type(): ComponentType<InputTouchButtonComponent> = InputTouchButtonComponent
    companion object : ComponentType<InputTouchButtonComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): InputTouchButtonComponent = this.copy()
}
