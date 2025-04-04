package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.componentPool.*
import kotlinx.serialization.*


/**
 * This component enables rendering objects in layers.
 *
 * @param [layerIndex] defines the order in which objects like textures or shapes will be drawn.
 * Higher numbers mean that the object will be rendered on top of other objects with smaller number.
 */
@Serializable @SerialName("Layer")
data class LayerComponent(
    val layerIndex: Int = 0
) : CloneableComponent<LayerComponent>() {
    override fun type(): ComponentType<LayerComponent> = LayerComponent
    companion object : ComponentType<LayerComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): LayerComponent = this.copy()
}
