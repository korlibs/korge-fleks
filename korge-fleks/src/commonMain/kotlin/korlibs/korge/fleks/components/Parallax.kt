package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.Parallax.Layer.Companion.addLayerComponentPool
import korlibs.korge.fleks.components.Parallax.Layer.Companion.staticLayerComponent
import korlibs.korge.fleks.components.Parallax.Plane.Companion.addPlaneComponentPool
import korlibs.korge.fleks.components.Parallax.Plane.Companion.staticPlaneComponent
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Position.Companion.staticPositionComponent
import korlibs.korge.fleks.components.Rgba.Companion.RgbaComponent
import korlibs.korge.fleks.components.Rgba.Companion.staticRgbaComponent
import korlibs.korge.fleks.systems.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is setting up the parallax background entity with all of its layers.
 * It is used in [ParallaxSystem]. There the position data for each layer is updated
 * according to world level movements.
 * In [ParallaxRenderSystem] the parallax background layers are drawn according their positions.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("Parallax")
class Parallax private constructor(
    var name: String = "",

    // Do not set below properties directly - they will be set by the onAdd hook function
    // List of layers
    val backgroundLayers: MutableList<Layer> = mutableListOf(),
    val parallaxPlane: Plane = staticPlaneComponent(),
    val foregroundLayers: MutableList<Layer> = mutableListOf()
) : Poolable<Parallax>() {
    override fun type() = ParallaxComponent

    companion object {
        val ParallaxComponent = componentTypeOf<Parallax>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticParallaxComponent(config: Parallax.() -> Unit ): Parallax =
            Parallax().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun World.ParallaxComponent(config: Parallax.() -> Unit ): Parallax =
        getPoolable(ParallaxComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addParallaxComponentPool(preAllocate: Int = 0) {
            addPool(ParallaxComponent, preAllocate) { Parallax() }
            addLayerComponentPool(preAllocate)
            addPlaneComponentPool(preAllocate)
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): Parallax =
    getPoolable(ParallaxComponent).apply {
        name = this@Parallax.name
        backgroundLayers.init(world = this@clone, from = this@Parallax.backgroundLayers)
        parallaxPlane.init(from = this@Parallax.parallaxPlane)
        foregroundLayers.init(world = this@clone, from = this@Parallax.foregroundLayers)
    }

    private fun MutableList<Layer>.init(world: World, from: List<Layer>) {
        from.forEach { item ->
            this.add(item.run { world.clone() } )
        }
    }

    // Initialize the component automatically when it is added to an entity
    override fun World.initComponent(entity: Entity) {
        val assetStore: AssetStore = inject(name = "AssetStore")

        // Get size for all layer lists to make sure that they fit to the parallax configuration
        // Same list sizes are also assumed in ParallaxRenderSystem
        val numberBackgroundLayers = assetStore.getBackground(name).config.backgroundLayers?.size ?: 0
        val numberAttachedRearLayers = assetStore.getBackground(name).config.parallaxPlane?.attachedLayersRear?.size ?: 0
        val numberParallaxPlaneLines = assetStore.getBackground(name).parallaxPlane?.imageDatas?.size ?: 0
        val numberAttachedFrontLayers = assetStore.getBackground(name).config.parallaxPlane?.attachedLayersFront?.size ?: 0
        val numberForegroundLayers = assetStore.getBackground(name).config.foregroundLayers?.size ?: 0

        // Initialize all layer lists on component creation
        repeat(numberBackgroundLayers) {
            // Create new entities for controlling position and color of each layer e.g. by the TweenEngineSystem
            // We share here the component objects from ParallaxComponent
            backgroundLayers.add(
                staticLayerComponent {
                    // Create new entity and add existing components from the parallax layer data object
                    this.entity = this@initComponent.entity("Parallax BG layer of entity '${entity.id}'") {
                        it += position
                        it += rgba
                    }
                }
            )
        }
        repeat(numberForegroundLayers) {
            foregroundLayers.add(
                staticLayerComponent {
                    this.entity = this@initComponent.entity("Parallax FG layer of entity '${entity.id}'") {
                        it += position
                        it += rgba
                    }
                }
            )
        }

        parallaxPlane.attachedLayersRearPositions = MutableList(numberAttachedRearLayers) { 0f }
        parallaxPlane.linePositions = MutableList(numberParallaxPlaneLines) { 0f }
        parallaxPlane.attachedLayersFrontPositions = MutableList(numberAttachedFrontLayers) { 0f }
        parallaxPlane.entity = this.entity("Parallax plane of entity '${entity.id}'") {
            it += parallaxPlane.position
            it += parallaxPlane.rgba
        }

        // Get height of the parallax background
        val parallaxDataContainer = assetStore.getBackground(name)
        val imageHeight: Float = (parallaxDataContainer.backgroundLayers?.height
            ?: parallaxDataContainer.foregroundLayers?.height
            ?: parallaxDataContainer.parallaxPlane?.default?.height
            ?: throw Error("ParallaxComponent: Parallax image data has no height!")).toFloat()
        val parallaxLayerHeight: Float = imageHeight

        // Set parallax height and offset in the camera system
        system<CameraSystem>().parallaxHeight = parallaxLayerHeight - parallaxDataContainer.config.offset.toFloat()
        system<CameraSystem>().parallaxOffset = parallaxDataContainer.config.offset.toFloat()
    }

    // Cleanup/Reset the component automatically when it is removed from an entity
    override fun World.cleanupComponent(entity: Entity) {
        name = ""
        backgroundLayers.cleanup(world = this)
        parallaxPlane.cleanup()
        foregroundLayers.cleanup(world = this)
    }

    // Cleanup and put Layer data object back to the pool
    private fun MutableList<Layer>.cleanup(world: World) {
        this.forEach { item ->
            item.cleanup()
            item.run { world.free() }
        }
        this.clear()
    }

    // Initialize an external prefab when the component is added to an entity
    override fun World.initPrefabs(entity: Entity) {
    }

    // Cleanup/Reset an external prefab when the component is removed from an entity
    override fun World.cleanupPrefabs(entity: Entity) {
    }

    /**
     * After deserialization some cleanup and setup needs to be done
     * go through each layer entity and copy the Position and RgbaComponent from the ParallaxComponent lists.
     */
    fun World.updateLayerEntities() {
        // Overwrite existing components with those from the parallax layer config
        backgroundLayers.forEach { layer ->
            layer.entity.configure {
                // Remove existing components - this triggers cleanup and freeing to component pool
                it -= PositionComponent
                it -= RgbaComponent
                // Add existing components from the layer object - they need to be a reference to the same component objects
                it += layer.position
                it += layer.rgba
            }
        }
        foregroundLayers.forEach { layer ->
            layer.entity.configure {
                // Remove existing components - this triggers cleanup and freeing to component pool
                it -= PositionComponent
                it -= RgbaComponent
                // Add existing components from the layer object - they need to be a reference to the same component objects
                it += layer.position
                it += layer.rgba
            }
        }
        parallaxPlane.entity.configure {
            // Remove existing components - this triggers cleanup and freeing to component pool
            it -= PositionComponent
            it -= RgbaComponent
            // Add existing components from the layer object - they need to be a reference to the same component objects
            it += parallaxPlane.position
            it += parallaxPlane.rgba
        }
    }

    // TODO: Change both below to data objects - not component anymore
    // Layer data object used inside ParallaxComponent
    @Serializable @SerialName("Parallax.Layer")
    class Layer private constructor(
        var entity: Entity = Entity.NONE,  // Link to entity for tween animation
        /**
         * Local position of layer relative to the top-left point of the parallax entity (global PositionComponent).
         */
        val position: Position = staticPositionComponent(),
        val rgba: Rgba = staticRgbaComponent()
    ) : Poolable<Layer>() {
        override fun type() = LayerComponent

        companion object {
            val LayerComponent = componentTypeOf<Layer>()

            // Use this function to create a new instance of component data as val inside another component
            fun staticLayerComponent(config: Layer.() -> Unit ): Layer =
                Layer().apply(config)

            // Use this function to get a new instance of a component from the pool and add it to an entity
            fun World.LayerComponent(config: Layer.() -> Unit ): Layer =
                getPoolable(LayerComponent).apply(config)

            // Call this function in the fleks world configuration to create the component pool
            fun InjectableConfiguration.addLayerComponentPool(preAllocate: Int = 0) {
                addPool(LayerComponent, preAllocate) { Layer() }
            }
        }

        // Clone a new instance of the component from the pool
        override fun World.clone(): Layer =
            getPoolable(LayerComponent).apply { init(from = this@Layer ) }

        // Init an existing component data instance with data from another component
        // This is used for component instances when they are part (val property) of another component
        fun init(from: Layer) {
            entity = from.entity
            position.init(from.position)
            rgba.init(from.rgba)
        }

        // Cleanup the component data instance manually
        // This is used for component instances when they are part (val property) of another component
        fun cleanup() {
            entity = Entity.NONE
            position.cleanup()
            rgba.cleanup()
        }

        // Cleanup/Reset the component automatically when it is removed from an entity
        override fun World.cleanupComponent(entity: Entity) {
            cleanup()
        }
    }

    // Plane data object used inside ParallaxComponent
    @Serializable @SerialName("Parallax.Plane")
    class Plane private constructor(
        var entity: Entity = Entity.NONE,  // Link to entity for tween animation
        /**
         * Local position of parallax plane relative to the top-left point of the parallax entity (global PositionComponent).
         * Here we use position.offsetX or position.offsetY perpendicular to below *Positions depending on ParallaxMode.
         */
        val position: Position = staticPositionComponent(),
        val rgba: Rgba = staticRgbaComponent(),
        // Used for horizontal or vertical movements of line and attached layers depending on ParallaxMode
        var linePositions: MutableList<Float> = mutableListOf(),
        var attachedLayersRearPositions: MutableList<Float> = mutableListOf(),
        var attachedLayersFrontPositions: MutableList<Float> = mutableListOf()
    ) : Poolable<Plane>() {
        override fun type() = PlaneComponent

        companion object {
            val PlaneComponent = componentTypeOf<Plane>()

            // Use this function to create a new instance of component data as val inside another component
            fun staticPlaneComponent(): Plane = Plane()

            // Use this function to get a new instance of a component from the pool and add it to an entity
            fun World.PlaneComponent(config: Plane.() -> Unit ): Plane =
            getPoolable(PlaneComponent).apply(config)

            // Call this function in the fleks world configuration to create the component pool
            fun InjectableConfiguration.addPlaneComponentPool(preAllocate: Int = 0) {
                addPool(PlaneComponent, preAllocate) { Plane() }
            }
        }

        // Clone a new instance of the component from the pool
        override fun World.clone(): Plane =
        getPoolable(PlaneComponent).apply { init(from = this@Plane ) }

        // Init an existing component data instance with data from another component
        // This is used for component instances when they are part (val property) of another component
        fun init(from: Plane) {
            entity = from.entity
            position.init(from.position)
            rgba.init(from.rgba)
            // lists are static and do not change so copy reference
            // TODO: NO!! - make a deep copy of lists
//            linePositions.init(from = from.linePositions)
            linePositions = from.linePositions
            attachedLayersRearPositions = from.attachedLayersRearPositions
            attachedLayersFrontPositions = from.attachedLayersFrontPositions
        }

        // Cleanup the component data instance manually
        // This is used for component instances when they are part (val property) of another component
        fun cleanup() {
            entity = Entity.NONE
            position.cleanup()
            rgba.cleanup()
            // lists contain only reference to the data and do not need to be cleaned up
            linePositions = mutableListOf()
            attachedLayersRearPositions = mutableListOf()
            attachedLayersFrontPositions = mutableListOf()
        }

        // Cleanup/Reset the component automatically when it is removed from an entity (and return it to the pool)
        override fun World.cleanupComponent(entity: Entity) {
            cleanup()
        }
    }
}
