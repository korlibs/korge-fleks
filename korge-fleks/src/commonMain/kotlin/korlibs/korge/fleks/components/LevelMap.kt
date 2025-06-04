package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.assets.ChunkArray2
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to store the level name and the layer names of a game object.
 *
 * @param levelName The unique identifier for the level.
 * @param layerNames List of layer names which shall be drawn by the specific render system.
 *                   Render order is specified by order of strings in the list.
 *                   Example: ["Background", "Playfield", "Collisions"]
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("LevelMap")
class LevelMap private constructor(
    var levelName: String = "",
    val layerNames: MutableList<String> = mutableListOf(),

    var levelChunks: ChunkArray2 = ChunkArray2.empty
) : PoolableComponent<LevelMap>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: LevelMap) {
        levelName = from.levelName
        layerNames.addAll(from.layerNames)
        // TODO: Refactor levelChunks data class to support poolable
        levelChunks = from.levelChunks.clone()
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        // level name will be set on initialization of the component
        layerNames.clear()  // Make list empty for reuse
        levelChunks = ChunkArray2.empty
    }

    override fun type() = LevelMapComponent

    companion object {
        val LevelMapComponent = componentTypeOf<LevelMap>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticLevelMapComponent(config: LevelMap.() -> Unit ): LevelMap =
        LevelMap().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun levelMapComponent(config: LevelMap.() -> Unit ): LevelMap =
        pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE) { LevelMap() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): LevelMap = levelMapComponent { init(from = this@LevelMap ) }

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
