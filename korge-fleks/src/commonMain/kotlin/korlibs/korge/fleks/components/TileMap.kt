package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to add a tile map to an entity. The tile map is treated as a "normal" game object and not
 * as a tile map for a level. For using tile maps for levels add [LevelMapComponent] to an entity.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("TileMap")
class TileMap private constructor(
    var name: String = ""
) : PoolableComponent<TileMap>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are a value property of another component
    fun init(from: TileMap) {
        name = from.name
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are a value property of another component
    fun cleanup() {
        name = ""
    }

    override fun type() = TileMapComponent

    companion object {
        val TileMapComponent = componentTypeOf<TileMap>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticTileMapComponent(config: TileMap.() -> Unit ): TileMap =
            TileMap().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun tileMapComponent(config: TileMap.() -> Unit ): TileMap =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "TileMap") { TileMap() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): TileMap = tileMapComponent { init(from = this@TileMap ) }

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