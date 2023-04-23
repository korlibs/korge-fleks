package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("TouchInput")
data class TouchInput(
    var drawOnLayer: String = ""
) : Component<TouchInput> {
    override fun type(): ComponentType<TouchInput> = TouchInput
    companion object : ComponentType<TouchInput>()
}
