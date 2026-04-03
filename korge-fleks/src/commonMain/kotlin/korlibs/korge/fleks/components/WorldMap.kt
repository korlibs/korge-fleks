package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.systems.SystemRuntimeConfigs
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to store chunk configs for the world map.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("WorldMap")
class WorldMap private constructor(
    var currentChunk: Int = 0,  // Current chunk number where the camera is located
    // List of chunk number which entities were spawned
    val activatedChunks: MutableSet<Int> = mutableSetOf()
) : PoolableComponent<WorldMap>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: WorldMap) {
        currentChunk = from.currentChunk
        activatedChunks.init(from.activatedChunks)
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        currentChunk = 0
        activatedChunks.cleanup()
    }

    override fun type() = WorldMapComponent

    companion object {
        val WorldMapComponent = componentTypeOf<WorldMap>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticWorldMapComponent(config: WorldMap.() -> Unit): WorldMap =
            WorldMap().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun worldMapComponent(config: WorldMap.() -> Unit): WorldMap =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "WorldMap") { WorldMap() }

        fun MutableSet<Int>.init(from: Set<Int>) {
            addAll(from)
        }

        fun MutableSet<Int>.cleanup() {
            clear()
        }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): WorldMap = worldMapComponent { init(from = this@WorldMap) }

    // Initialize the component automatically when it is added to an entity
    override fun World.initComponent(entity: Entity) {
    }

    // Cleanup/Reset the component automatically when it is removed from an entity (component will be returned to the pool eventually)
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }

    // Initialize an external prefab when the component is added to an entity
    override fun World.initPrefabs(entity: Entity) {
        val systemRuntimeConfigs = inject<SystemRuntimeConfigs>("SystemRuntimeConfigs")
        systemRuntimeConfigs.worldChunk = entity
    }

    // Cleanup/Reset an external prefab when the component is removed from an entity
    override fun World.cleanupPrefabs(entity: Entity) {
        val systemRuntimeConfigs = inject<SystemRuntimeConfigs>("SystemRuntimeConfigs")
        systemRuntimeConfigs.worldChunk = null
    }

    // Free the component and return it to the pool - this is called directly by the SnapshotSerializerSystem
    override fun free() {
        cleanup()
        pool.free(this)
    }
}
