package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import korlibs.korge.fleks.entity.config.nothing
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("InputTouchButton")
data class InputTouchButtonComponent(
    var pressed: Boolean = false,
    var triggerImmediately: Boolean = false,
    var config: Identifier = nothing,
    var function: Identifier = nothing
) : Component<InputTouchButtonComponent> {
    override fun type(): ComponentType<InputTouchButtonComponent> = InputTouchButtonComponent
    companion object : ComponentType<InputTouchButtonComponent>()
}
