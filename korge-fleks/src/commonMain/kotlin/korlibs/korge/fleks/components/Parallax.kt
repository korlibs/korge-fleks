package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.Parallax.Plane.Companion.staticPlane
import korlibs.korge.fleks.components.Parallax.Layer.Companion.init
import korlibs.korge.fleks.components.Parallax.Layer.Companion.freeAndClear
import korlibs.korge.fleks.components.Parallax.Layer.Companion.layer
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Position.Companion.staticPositionComponent
import korlibs.korge.fleks.components.Rgba.Companion.RgbaComponent
import korlibs.korge.fleks.components.Rgba.Companion.staticRgbaComponent
import korlibs.korge.fleks.systems.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.fleks.utils.entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is setting up the parallax background entity with all of its layers.
 * It is used in [ParallaxSystem]. There the position data for each layer is updated
 * according to world level movements.
 * In [ParallaxRenderSystem] the parallax background layers are drawn according their positions.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("Parallax")
class Parallax private constructor(
    var name: String = "",

    // Do not set below properties directly - they will be set by the onAdd hook function
    // List of layers
    val backgroundLayers: MutableList<Layer> = mutableListOf(),
    val parallaxPlane: Plane = staticPlane(),
    val foregroundLayers: MutableList<Layer> = mutableListOf()
) : PoolableComponent<Parallax>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: Parallax) {
        name = from.name
        backgroundLayers.init(from.backgroundLayers)
        parallaxPlane.init(from.parallaxPlane)
        foregroundLayers.init(from.foregroundLayers)
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        name = ""
        backgroundLayers.freeAndClear()
        parallaxPlane.cleanup()  // static property - no need to free
        foregroundLayers.freeAndClear()
    }

    override fun type() = ParallaxComponent

    companion object {
        val ParallaxComponent = componentTypeOf<Parallax>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticParallaxComponent(config: Parallax.() -> Unit ): Parallax =
            Parallax().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun parallaxComponent(config: Parallax.() -> Unit ): Parallax =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE) { Parallax() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): Parallax = parallaxComponent { init(from = this@Parallax ) }

    // Initialize the component automatically when it is added to an entity
    override fun World.initComponent(entity: Entity) {
        val world = this
        val assetStore: AssetStore = inject(name = "AssetStore")

        // Get size for all layer lists to make sure that they fit to the parallax configuration
        // Same list sizes are also assumed in ParallaxRenderSystem
        val numberBackgroundLayers = assetStore.getBackground(name).config.backgroundLayers?.size ?: 0
        val numberAttachedRearLayers = assetStore.getBackground(name).config.parallaxPlane?.attachedLayersRear?.size ?: 0
        val numberParallaxPlaneLines = assetStore.getBackground(name).parallaxPlane?.imageDatas?.size ?: 0
        val numberAttachedFrontLayers = assetStore.getBackground(name).config.parallaxPlane?.attachedLayersFront?.size ?: 0
        val numberForegroundLayers = assetStore.getBackground(name).config.foregroundLayers?.size ?: 0

        // Initialize all layer lists on component creation
        repeat(numberBackgroundLayers) { index ->
            // Create new entities for controlling position and color of each layer e.g. by the TweenEngineSystem
            // We share here the component objects from ParallaxComponent
            backgroundLayers.add(
                layer {
                    // Create new entity and add existing components from the parallax layer data object
                    this.entity = world.entity("Parallax BG layer '$index' of entity '${entity.id}'") {
                        // Add already existing components from the layer object
                        it += position
                        it += rgba
                    }
                }
            )
        }
        repeat(numberForegroundLayers) { index ->
            foregroundLayers.add(
                layer {
                    this.entity = world.entity("Parallax FG layer '$index' of entity '${entity.id}'") {
                        it += position
                        it += rgba
                    }
                }
            )
        }

        repeat(numberAttachedRearLayers) { parallaxPlane.attachedLayersRearPositions.add(0f) }
        repeat(numberParallaxPlaneLines) { parallaxPlane.linePositions.add(0f) }
        repeat(numberAttachedFrontLayers) { parallaxPlane.attachedLayersFrontPositions.add(0f) }

        parallaxPlane.entity = this.entity("Parallax plane of entity '${entity.id}'") {
            it += parallaxPlane.position
            it += parallaxPlane.rgba
        }

        // Initialize an external prefab when the component is added to an entity
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

    // Cleanup/Reset the component automatically when it is removed from an entity (component will be returned to the pool eventually)
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }

    // Initialize an external prefab when the component is added to an entity
    override fun World.initPrefabs(entity: Entity) {
    }

    // Cleanup/Reset an external prefab when the component is removed from an entity
    override fun World.cleanupPrefabs(entity: Entity) {
    }

    // Free the component and return it to the pool - this is called directly by the SnapshotSerializerSystem
    override fun free() {
        cleanup()
        pool.free(this)
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

    // Layer data object used inside ParallaxComponent
    @Serializable @SerialName("Layer")
    class Layer private constructor(
        var entity: Entity = Entity.NONE,  // Link to entity for tween animation
        /**
         * Local position of layer relative to the top-left point of the parallax entity (global PositionComponent).
         */
        val position: Position = staticPositionComponent {},
        val rgba: Rgba = staticRgbaComponent {}
    ) : Poolable<Layer> {
        // Init an existing data instance with data from another one
        override fun init(from: Layer) {
            entity = from.entity
            position.init(from.position)
            rgba.init(from.rgba)
        }

        // Cleanup data instance manually
        // This is used for data instances when they are part (val property) of a component
        override fun cleanup() {
            entity = Entity.NONE
            position.cleanup()
            rgba.cleanup()
        }

        // Clone a new data instance from the pool
        override fun clone(): Layer = layer { init(from = this@Layer ) }

        // Cleanup the tween data instance manually
        override fun free() {
            cleanup()
            pool.free(this)
        }

        companion object {
            // Use this function to create a new instance of data as val inside a component
            fun staticLayer(config: Layer.() -> Unit ): Layer =
                Layer().apply(config)

            // Use this function to get a new instance of a component from the pool and add it to an entity
            fun layer(config: Layer.() -> Unit ): Layer =
                pool.alloc().apply(config)

            private val pool = Pool(AppConfig.POOL_PREALLOCATE) { Layer() }

            fun MutableList<Layer>.init(from: List<Layer>) {
                from.forEach { item ->
                    this.add(item.clone())
                }
            }

            // Cleanup and put Layer data object back to the pool
            fun MutableList<Layer>.freeAndClear() {
                this.forEach { item ->
                    item.cleanup()
                    item.free()
                }
                this.clear()
            }
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
        val position: Position = staticPositionComponent {},
        val rgba: Rgba = staticRgbaComponent {},
        // Used for horizontal or vertical movements of line and attached layers depending on ParallaxMode
        val linePositions: MutableList<Float> = mutableListOf(),
        // Below lists are static and can be cloned by reference
        val attachedLayersRearPositions: MutableList<Float> = mutableListOf(),
        val attachedLayersFrontPositions: MutableList<Float> = mutableListOf()
    ) : Poolable<Plane> {

        override fun init(from: Plane) {
            // Copy all properties from the original Plane object
            entity = from.entity
            position.init(from.position)
            rgba.init(from.rgba)
            // Make deep copy of the line and layer positions - they are changing
            linePositions.addAll(from.linePositions)
            attachedLayersRearPositions.addAll(from.attachedLayersRearPositions)
            attachedLayersFrontPositions.addAll(from.attachedLayersFrontPositions)
        }

        override fun cleanup() {
            entity = Entity.NONE
            position.cleanup()
            rgba.cleanup()
            linePositions.clear()
            attachedLayersRearPositions.clear()
            attachedLayersFrontPositions.clear()
        }

        override fun free() {
            TODO("Check if needed - Free Plane component from pool")
            cleanup()
            pool.free(this)
        }

        override fun clone(): Plane = pool.alloc()

        companion object {
            fun staticPlane(): Plane = Plane()

            // Use this function to get a new instance of a component from the pool and add it to an entity
            fun plane(config: Plane.() -> Unit ): Plane = pool.alloc().apply(config)

            private val pool = Pool(AppConfig.POOL_PREALLOCATE) {
//                println("Plane created: $it")
                Plane()
            }
        }
    }
}
