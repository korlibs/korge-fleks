package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component enables rendering objects in layers.
 *
 * @param [index] defines the order in which objects like textures or shapes will be drawn.
 * Higher numbers mean that the object will be rendered on top of other objects with smaller number.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("Layer")
class Layer private constructor(
    var index: Int = 0
) : PoolableComponent<Layer>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: Layer) {
        index = from.index
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        index = 0
    }

    override fun type() = LayerComponent

    companion object {
        val LayerComponent = componentTypeOf<Layer>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticLayerComponent(config: Layer.() -> Unit): Layer =
            Layer().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun layerComponent(config: Layer.() -> Unit): Layer =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "Layer") { Layer() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): Layer = layerComponent { init(from = this@Layer) }

    // Initialize the component automatically when it is added to an entity
    override fun World.initComponent(entity: Entity) {
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
}
