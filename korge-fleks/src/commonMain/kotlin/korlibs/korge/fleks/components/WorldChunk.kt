package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to specify the world chunk in which an entity is located.
 * This is needed to determine the position of the entity in the world. Since the size of the world
 * is not fixed, the world is divided into chunks and each entity is assigned to a chunk. This allows for efficient
 * management of entities in the world, as only the entities in the current chunk and neighboring chunks
 * need to be updated and rendered.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("WorldChunk")
class WorldChunk private constructor(
    var chunk: Int = 0
) : PoolableComponent<WorldChunk>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are a value property of another component
    fun init(from: WorldChunk) {
        chunk = from.chunk
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are a value property of another component
    fun cleanup() {
        chunk = 0
    }

    override fun type() = WorldChunkComponent

    companion object {
        val WorldChunkComponent = componentTypeOf<WorldChunk>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticWorldChunkComponent(config: WorldChunk.() -> Unit ): WorldChunk =
            WorldChunk().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun worldChunkComponent(config: WorldChunk.() -> Unit ): WorldChunk =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "WorldChunk") { WorldChunk() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): WorldChunk = worldChunkComponent { init(from = this@WorldChunk ) }

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
