package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import kotlinx.serialization.*

@Serializable
@SerialName("Offset")
data class OffsetComponent(
    var x: Float = 0f,
    var y: Float = 0f
) : Component<OffsetComponent> {
    override fun type(): ComponentType<OffsetComponent> = OffsetComponent
    companion object : ComponentType<OffsetComponent>()
}
