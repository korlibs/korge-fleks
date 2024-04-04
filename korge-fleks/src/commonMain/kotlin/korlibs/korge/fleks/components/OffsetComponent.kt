package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import kotlinx.serialization.*

@Serializable
@SerialName("Offset")
data class OffsetComponent(
    var x: Double = 0.0,
    var y: Double = 0.0
) : Component<OffsetComponent> {
    override fun type(): ComponentType<OffsetComponent> = OffsetComponent
    companion object : ComponentType<OffsetComponent>()
}
