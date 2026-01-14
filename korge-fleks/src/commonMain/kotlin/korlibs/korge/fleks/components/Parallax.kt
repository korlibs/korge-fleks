package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.Position.Companion.positionComponent
import korlibs.korge.fleks.components.Rgba.Companion.rgbaComponent
import korlibs.korge.fleks.components.data.ParallaxPlane
import korlibs.korge.fleks.components.data.ParallaxPlane.Companion.staticParallaxPlane
import korlibs.korge.fleks.systems.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.fleks.utils.createEntity
import korlibs.korge.fleks.utils.cleanup
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
    /**
     * Name of the parallax asset info in the [AssetStore].
     */
    var name: String = "",

    // Do not set below properties directly - they will be set by the initComponent hook function
    // List of layer entities
    val bgLayerEntities: MutableMap<String, Entity> = mutableMapOf(),
    var parallaxPlane: ParallaxPlane = staticParallaxPlane {},
    val fgLayerEntities: MutableMap<String, Entity> = mutableMapOf(),
) : PoolableComponent<Parallax>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: Parallax) {
        name = from.name
        bgLayerEntities.init(from.bgLayerEntities)
        parallaxPlane.init(from.parallaxPlane)
        fgLayerEntities.init(from.fgLayerEntities)
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        name = ""
        // Entities are freed in the cleanupComponent function because of world scope
        bgLayerEntities.cleanup()
        parallaxPlane.cleanup()
        fgLayerEntities.cleanup()
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
        // Prepare all layer entities according to the parallax config
        val assetStore: AssetStore = inject(name = "AssetStore")
        val parallaxConfig = assetStore.getParallaxLayers(name)

        parallaxConfig.backgroundLayers.forEach { layer ->
            bgLayerEntities[layer.name] = createEntity("Parallax BG layer '${layer.name}' of entity '${entity.id}'") {
                it += positionComponent {}
                it += rgbaComponent {}
            }
        }

        parallaxConfig.foregroundLayers.forEach { layer ->
            fgLayerEntities[layer.name] = createEntity("Parallax FG layer '${layer.name}' of entity '${entity.id}'") {
                it += positionComponent {}
                it += rgbaComponent {}
            }
        }

        parallaxConfig.parallaxPlane?.let { plane ->
            parallaxPlane.entity = createEntity("Parallax plane of entity '${entity.id}'") {
                it += positionComponent {}
                it += rgbaComponent {}
            }
            repeat(plane.lineTextures.size) { parallaxPlane.linePositions.add(0f) }
            repeat(plane.topAttachedLayers.size) { parallaxPlane.topAttachedLayerPositions.add(0f) }
            repeat(plane.bottomAttachedLayers.size) { parallaxPlane.bottomAttachedLayerPositions.add(0f) }
        }
    }

    // Cleanup/Reset the component automatically when it is removed from an entity (component will be returned to the pool eventually)
    override fun World.cleanupComponent(entity: Entity) {
        // Remove all layer entities when we are in scope of the world
        bgLayerEntities.removeFromWorld(this)
        fgLayerEntities.removeFromWorld(this)
        if (parallaxPlane.entity != Entity.NONE) this -= parallaxPlane.entity
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
