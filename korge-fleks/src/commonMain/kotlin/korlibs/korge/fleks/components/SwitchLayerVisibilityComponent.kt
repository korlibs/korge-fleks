package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import kotlinx.serialization.*


/**
 * This component is manipulating the rgba value of [SpriteLayersComponent].
 */
@Serializable
@SerialName("SwitchLayerVisibility")
data class SwitchLayerVisibilityComponent(
    var offVariance: Float = 0f,  // variance in switching value off: 1f - every frame switching possible, 0f - no switching at all
    var onVariance: Float = 1f,   // variance in switching value on again: 1f - changed value switches back immediately, 0f - changed value stays forever
) : Component<SwitchLayerVisibilityComponent> {
    override fun type() = SwitchLayerVisibilityComponent
    companion object : ComponentType<SwitchLayerVisibilityComponent>()
}
