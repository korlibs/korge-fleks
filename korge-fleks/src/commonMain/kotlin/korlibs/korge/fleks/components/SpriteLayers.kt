package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.data.SpriteLayer
import korlibs.korge.fleks.components.data.SpriteLayer.Companion.freeAndClear
import korlibs.korge.fleks.components.data.SpriteLayer.Companion.init
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component enables controlling of layer properties of a sprite texture.
 *
 * @param [layerMap] contains offset and rgba properies for specific layers of a sprite texture.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("SpriteLayers")
class SpriteLayers private constructor(
    val layerMap: MutableMap<String, SpriteLayer> = mutableMapOf()
) : PoolableComponent<SpriteLayers>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: SpriteLayers) {
        layerMap.init(from.layerMap)
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        layerMap.freeAndClear()
    }

    override fun type() = SpriteLayersComponent

    companion object {
        val SpriteLayersComponent = componentTypeOf<SpriteLayers>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticSpriteLayersComponent(config: SpriteLayers.() -> Unit ): SpriteLayers =
        SpriteLayers().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun spriteLayersComponent(config: SpriteLayers.() -> Unit ): SpriteLayers =
        pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE) { SpriteLayers() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): SpriteLayers = spriteLayersComponent { init(from = this@SpriteLayers ) }

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
