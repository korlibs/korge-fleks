package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.fleks.systems.*
import korlibs.korge.fleks.renderSystems.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This component is setting up the parallax background entity with all of its layers.
 * It is used in [ParallaxSystem]. There the position data for each layer is updated
 * according to world level movements.
 * In the [ParallaxRenderSystem] for drawing the background.
 */
@Serializable @SerialName("Parallax")
data class ParallaxComponent(
    var name: String = "",

    // Do not set below properties directly - they will be set by the onAdd hook function
    // List of layers
    var backgroundLayers: List<Layer> = listOf(),
    var parallaxPlane: Plane = Plane(),
    var foregroundLayers: List<Layer> = listOf(),

    // internal
    var initialized: Boolean = false
) : Poolable<ParallaxComponent>() {

    @Serializable @SerialName("Parallax.Layer")
    data class Layer(
        var entity: Entity = Entity.NONE,  // Link to entity for tween animation
        /**
         * Local position of layer relative to the top-left point of the parallax entity (global PositionComponent).
         */
        val position: PositionComponent = PositionComponent(),
        val rgba: RgbaComponent = RgbaComponent()
    ) : Poolable<Layer> {

        // Perform deep copy with special handling for entity, position and rgba.
        override fun clone(): Layer =
            Layer(
                entity = entity.clone(),
                position = position.clone(),
                rgba = rgba.clone()
            )
    }

    @Serializable @SerialName("Parallax.Plane")
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
    ) : Poolable<Plane> {

        // Perform deep copy with special handling
        override fun clone(): Plane =
            Plane(
                entity = entity.clone(),
                position = position.clone(),
                rgba = rgba.clone(),
                linePositions = linePositions.toMutableList(),
                attachedLayersRearPositions = attachedLayersRearPositions.toMutableList(),
                attachedLayersFrontPositions = attachedLayersFrontPositions.toMutableList()
            )
    }

    /**
     * Hint: The onAdd hook function is not called when a fleks world is loaded from a snapshot.
     */
    override fun World.onAdd(entity: Entity) {
        // Make sure that initialization is skipped on world snapshot loading (deserialization of save game)
        if (initialized) return
        else initialized = true

        val assetStore: AssetStore = this.inject(name = "AssetStore")

        // Get size for all layer lists to make sure that they fit to the parallax configuration
        // Same list sizes are also assumed in ParallaxRenderSystem
        val numberBackgroundLayers = assetStore.getBackground(name).config.backgroundLayers?.size ?: 0
        val numberAttachedRearLayers = assetStore.getBackground(name).config.parallaxPlane?.attachedLayersRear?.size ?: 0
        val numberParallaxPlaneLines = assetStore.getBackground(name).parallaxPlane?.imageDatas?.size ?: 0
        val numberAttachedFrontLayers = assetStore.getBackground(name).config.parallaxPlane?.attachedLayersFront?.size ?: 0
        val numberForegroundLayers = assetStore.getBackground(name).config.foregroundLayers?.size ?: 0

        // Initialize all layer lists on component creation
        backgroundLayers = List(numberBackgroundLayers) { Layer() }
        foregroundLayers = List(numberForegroundLayers) { Layer() }
        parallaxPlane.attachedLayersRearPositions = MutableList(numberAttachedRearLayers) { 0f }
        parallaxPlane.linePositions = MutableList(numberParallaxPlaneLines) { 0f }
        parallaxPlane.attachedLayersFrontPositions = MutableList(numberAttachedFrontLayers) { 0f }

        // Create new entities for controlling position and color of each layer e.g. by the TweenEngineSystem
        // We share here the component objects from ParallaxComponent
        backgroundLayers.forEach { layer ->
            // Create new entity and add existing components from the parallax layer config
            layer.entity = this.entity("Parallax BG layer of entity '${entity.id}'") {
                it += layer.position
                it += layer.rgba
            }
        }
        foregroundLayers.forEach { layer ->
            layer.entity = this.entity("Parallax FG layer of entity '${entity.id}'") {
                it += layer.position
                it += layer.rgba
            }
        }
        parallaxPlane.entity = this.entity("Parallax plane of entity '${entity.id}'") {
            it += parallaxPlane.position
            it += parallaxPlane.rgba
        }

        // Get height of the parallax background
        val parallaxDataContainer = assetStore.getBackground(name)
        val imageHeight: Float = (parallaxDataContainer.backgroundLayers?.height ?:
            parallaxDataContainer.foregroundLayers?.height ?:
            parallaxDataContainer.parallaxPlane?.default?.height ?: throw Error("ParallaxComponent: Parallax image data has no height!")
            ).toFloat()
        val parallaxLayerHeight: Float = imageHeight
        system<CameraSystem>().parallaxHeight = parallaxLayerHeight - parallaxDataContainer.config.offset.toFloat()
        system<CameraSystem>().parallaxOffset = parallaxDataContainer.config.offset.toFloat()
    }

    /**
     * After deserialization some cleanup and setup needs to be done
     * go through each layer entity and copy the Position and RgbaComponent from the ParallaxComponent lists.
     */
    fun World.updateLayerEntities() {
        // Overwrite existing components with those from the parallax layer config
        backgroundLayers.forEach { layer ->
            layer.entity.configure {
                it += layer.position
                it += layer.rgba
            }
        }
        foregroundLayers.forEach { layer ->
            layer.entity.configure {
                it += layer.position
                it += layer.rgba
            }
        }
        parallaxPlane.entity.configure {
            it += parallaxPlane.position
            it += parallaxPlane.rgba
        }
    }

    override fun type(): ComponentType<ParallaxComponent> = ParallaxComponent
    companion object : ComponentType<ParallaxComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): ParallaxComponent {
        val bgLayers = mutableListOf<Layer>()
        val fgLayers = mutableListOf<Layer>()
        // Perform special deep copy of list elements
        backgroundLayers.forEach { element -> bgLayers.add(element.clone()) }
        foregroundLayers.forEach { element -> fgLayers.add(element.clone()) }

        return this.copy(
            backgroundLayers = bgLayers,
            parallaxPlane = parallaxPlane.clone(),
            foregroundLayers = fgLayers
        )
    }
}
