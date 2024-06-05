package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.image.color.*
import korlibs.korge.assetmanager.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.parallax.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This component is setting up the parallax background entity with all of its layers.
 * It is used in [ParallaxSystem]. There the position data for each layer is updated
 * according to world level movements.
 * In the [ParallaxRenderSystem] for drawing the background.
 */
@Serializable
@SerialName("Parallax")
data class ParallaxComponent(
    var name: String = "",

    // Do not set below properties directly - they will be set by the onAdd hook function
    // List of layers
    var backgroundLayers: List<Layer> = listOf(),
    var parallaxPlane: Plane = Plane(),
    var foregroundLayers: List<Layer> = listOf()
) : Component<ParallaxComponent> {

    @Serializable
    @SerialName("Parallax.Layer")
    data class Layer(
        var entity: Entity = Entity.NONE,  // Link to entity for tween animation
        /**
         * Local position of layer relative to the top-left point of the parallax entity (global PositionComponent).
         */
        val position: PositionComponent = PositionComponent(),
        val rgba: RgbaComponent = RgbaComponent()
    ) : SerializeBase


    @Serializable
    @SerialName("Parallax.Plane")
    data class Plane(
        var entity: Entity = Entity.NONE,  // Link to entity for tween animation
        /**
         * Local position of parallax plane relative to the top-left point of the parallax entity (global PositionComponent).
         * Here we use position.offsetX or position.offsetY perpendicular to below *Positions depending on ParallaxMode.
         */
        val position: PositionComponent = PositionComponent(),
        val rgba: RgbaComponent = RgbaComponent(),
        // Used for horizontal or vertical movements of line and attached layers depending on ParallaxMode
        var linePositions: MutableList<Float> = mutableListOf(),
        var attachedLayersRearPositions: MutableList<Float> = mutableListOf(),
        var attachedLayersFrontPositions: MutableList<Float> = mutableListOf()
    ) : SerializeBase

    override fun World.onAdd(entity: Entity) {
        val assetStore: AssetStore = this.inject(name = "AssetStore")

        // Get size for all layer lists to make sure that they fit to the parallax configuration
        // Same list sizes are also assumed in ParallaxRenderSystem
        val numberBackgroundLayers = assetStore.getBackground(name).config.backgroundLayers?.size ?: 0
        val numberAttachedRearLayers = assetStore.getBackground(name).config.parallaxPlane?.attachedLayersRear?.size ?: 0
        val numberParallaxPlaneLines = assetStore.getBackground(name).parallaxPlane?.imageDatas?.size ?: 0
        val numberAttachedFrontLayers = assetStore.getBackground(name).config.parallaxPlane?.attachedLayersFront?.size ?: 0
        val numberForegroundLayers = assetStore.getBackground(name).config.foregroundLayers?.size ?: 0

        // TODO Check if isEmpty check is needed - it looks that loadSnapshot does not call components onAdd hook functions
        // Initialize all layer lists on component creation (don't do this after deserialization - list are not empty)
        if (backgroundLayers.isEmpty()) backgroundLayers = List(numberBackgroundLayers) { Layer() }
        if (foregroundLayers.isEmpty()) foregroundLayers = List(numberForegroundLayers) { Layer() }
        if (parallaxPlane.attachedLayersRearPositions.isEmpty()) parallaxPlane.attachedLayersRearPositions = MutableList(numberAttachedRearLayers) { 0f }
        if (parallaxPlane.linePositions.isEmpty()) parallaxPlane.linePositions = MutableList(numberParallaxPlaneLines) { 0f }
        if (parallaxPlane.attachedLayersFrontPositions.isEmpty()) parallaxPlane.attachedLayersFrontPositions = MutableList(numberAttachedFrontLayers) { 0f }

        // TODO this below will not work after deserialization because below entities might not yet exist in the world
        //      they are created later
        //      -> after deserialization some cleanup and setup needs to be done
        //         check how the tasks here can be done in a deserialization post process step

        // Create new entities for controlling position and color of each layer e.g. by the TweenEngineSystem
        // We share here the component objects from ParallaxComponent
        backgroundLayers.forEach { layer ->
            // Create new entity if it does not yet exist - after deserialization of the world the entity was already created
            if (layer.entity == Entity.NONE) layer.entity = entity {}
            // Add or overwrite existing (in case after deserialization) components
            layer.entity = entity {
                it += layer.position
                it += layer.rgba
            }
        }
        foregroundLayers.forEach { layer ->
            // Create new entity if it does not yet exist - after deserialization of the world the entity was already created
            if (layer.entity == Entity.NONE) layer.entity = entity {}
            // Add or overwrite existing (in case after deserialization) components
            layer.entity = entity {
                it += layer.position
                it += layer.rgba
            }
        }
        if (parallaxPlane.entity == Entity.NONE) parallaxPlane.entity = entity {}
        parallaxPlane.entity = entity {
            it += parallaxPlane.position
            it += parallaxPlane.rgba
        }
    }

    override fun World.onRemove(entity: Entity) {

    }

    override fun type(): ComponentType<ParallaxComponent> = ParallaxComponent
    companion object : ComponentType<ParallaxComponent>()
}
