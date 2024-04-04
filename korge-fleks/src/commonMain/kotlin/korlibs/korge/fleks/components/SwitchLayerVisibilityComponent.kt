package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


/**
 * This component is used to switch [visible][AppearanceComponent.visible] property of [AppearanceComponent] component.
 *
 * TODO check description above
 */
@Serializable
@SerialName("SwitchLayerVisibility")
data class SwitchLayerVisibilityComponent(
    var offVariance: Double = 0.0,  // variance in switching value off: (1.0) - every frame switching possible, (0.0) - no switching at all
    var onVariance: Double = 0.0,   // variance in switching value on again: (1.0) - changed value switches back immediately, (0.0) - changed value stays forever
    var spriteLayers: List<LayerVisibility> = listOf()
) : Component<SwitchLayerVisibilityComponent> {

    @Serializable
    @SerialName("LayerVisibility")
    data class LayerVisibility(
        var name: String = "",
        var visible: Boolean = true
    ) : SerializeBase

    override fun type() = SwitchLayerVisibilityComponent
    companion object : ComponentType<SwitchLayerVisibilityComponent>()
}
