package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("InputTouchButton")
data class InputTouchButton(
    var pressed: Boolean = false,
    var triggerImmediately: Boolean = false,
    var action: Invokable = noInvokable,
    var buttonId: EntityConfig = noConfig
) : Component<InputTouchButton>, SerializeBase {
    override fun type(): ComponentType<InputTouchButton> = InputTouchButton
    companion object : ComponentType<InputTouchButton>()
}
