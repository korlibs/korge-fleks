package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.Position.Companion.positionComponent
import korlibs.korge.fleks.components.Rgba.Companion.rgbaComponent
import korlibs.korge.fleks.systems.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.fleks.utils.entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


//fun MutableList<DataBase>.init(from: List<DataBase>) {
//    from.forEach { item ->
//        this.add((item as Poolable<*>).clone() as DataBase)
//    }
//}
//
//fun MutableList<DataBase>.freeAndClear() {
//    this.forEach { item ->
//        (item as Poolable<*>).free()
//    }
//    this.clear()
//}


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

    // Do not set below properties directly - they will be set by the initComponent hook function
    // List of layer entities
    val bgLayerEntities: MutableList<Entity> = mutableListOf(),
    var parallaxPlaneEntity: Entity = Entity.NONE,
    val fgLayerEntities: MutableList<Entity> = mutableListOf(),
    // Used for horizontal or vertical movements of line and attached layers depending on ParallaxMode
    val linePositions: MutableList<Float> = mutableListOf(),
    // Below lists are static and can be cloned by reference
    val attachedLayersRearPositions: MutableList<Float> = mutableListOf(),
    val attachedLayersFrontPositions: MutableList<Float> = mutableListOf()
) : PoolableComponent<Parallax>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: Parallax) {
        name = from.name
        bgLayerEntities.init(from.bgLayerEntities)
        parallaxPlaneEntity = from.parallaxPlaneEntity
        fgLayerEntities.init(from.fgLayerEntities)
        // Make deep copy of the line and layer positions - they are changing
        linePositions.addAll(from.linePositions)
        attachedLayersRearPositions.addAll(from.attachedLayersRearPositions)
        attachedLayersFrontPositions.addAll(from.attachedLayersFrontPositions)
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        name = ""
        // Entities are freed in the cleanupComponent function because of world scope
        bgLayerEntities.clear()
        parallaxPlaneEntity = Entity.NONE
        fgLayerEntities.clear()
        linePositions.clear()
        attachedLayersRearPositions.clear()
        attachedLayersFrontPositions.clear()
    }

    override fun type() = ParallaxComponent

    companion object {
        val ParallaxComponent = componentTypeOf<Parallax>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticParallaxComponent(config: Parallax.() -> Unit): Parallax =
            Parallax().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun parallaxComponent(config: Parallax.() -> Unit): Parallax =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "Parallax") { Parallax() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): Parallax = parallaxComponent { init(from = this@Parallax) }

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
            bgLayerEntities.add(
                world.entity("Parallax BG layer '$index' of entity '${entity.id}'") {
                    it += positionComponent {}
                    it += rgbaComponent {}
                }
            )
        }
        repeat(numberForegroundLayers) { index ->
            fgLayerEntities.add(
                world.entity("Parallax FG layer '$index' of entity '${entity.id}'") {
                    it += positionComponent {}
                    it += rgbaComponent {}
                }
            )
        }

        repeat(numberAttachedRearLayers) { attachedLayersRearPositions.add(0f) }
        repeat(numberParallaxPlaneLines) { linePositions.add(0f) }
        repeat(numberAttachedFrontLayers) { attachedLayersFrontPositions.add(0f) }

        parallaxPlaneEntity = world.entity("Parallax plane of entity '${entity.id}'") {
            it += positionComponent {}
            it += rgbaComponent {}
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
        // Remove all layer entities when we are in scope of the world
        bgLayerEntities.free(this)
        fgLayerEntities.free(this)
        // Lists will be cleared in the cleanup function
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
        // Entities do not need to be removed from the work because we are not in the scope of the world
        cleanup()
        pool.free(this)
    }
}
