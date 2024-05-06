package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*


/**
 * This component enables rendering objects in layers.
 *
 * @param [layerIndex] defines the order in which objects like textures or shapes will be drawn.
 * Higher numbers mean that the object will be rendered on top of other objects with smaller number.
 */
data class LayerComponent(
    val layerIndex: Int = 0
) : Component<LayerComponent> {
    override fun type(): ComponentType<LayerComponent> = LayerComponent
    companion object : ComponentType<LayerComponent>()
}
