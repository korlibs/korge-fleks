package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import korlibs.image.color.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.entity.config.nothing
import korlibs.korge.fleks.utils.*
import korlibs.korge.parallax.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This component is setting up the parallax background entity with all of its layers.
 * It is used in [ParallaxSystem]. There the position data for each layer is updated
 * according to world level movements.
 * In the [ParallaxRenderView] for drawing the background.
 */
@Serializable
@SerialName("Parallax")
data class ParallaxComponent(
    var config: Identifier = nothing,

    // List of layers
    val backgroundLayers: List<Layer> = listOf(),
    val attachedLayersRear: List<Layer> = listOf(),
    val parallaxPlane: List<Layer> = listOf(),
    val attachedLayersFront: List<Layer> = listOf(),
    val foregroundLayers: List<Layer> = listOf()
) : Component<ParallaxComponent> {

    @Serializable
    @SerialName("Parallax")
    data class Layer(
        /**
         * Position of layer relative to the top-left point of the parallax entity (from PositionComponent)
         */
        val position: PositionComponent = PositionComponent(),
        val rgba: RgbaComponent = RgbaComponent()
    ) : SerializeBase

    override fun type(): ComponentType<ParallaxComponent> = ParallaxComponent
    companion object : ComponentType<ParallaxComponent>()
}
