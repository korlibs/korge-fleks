package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to define an entity that spawns other entities.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("Spawner")
class Spawner private constructor(
    var numberOfObjects: Int = 1,         // The spawner will generate this number of object when triggered after interval time
    var interval: Int = 1,                // 0 - disabled, 1 - every frame, 2 - every second frame, 3 - every third frame,...
    var timeVariation: Int = 0,           // 0 - no variation, 1 - one frame variation, 2 - two frames variation, ...
    var positionVariation: Float = 0f,    // variation radius where objects will be spawned - 0.0 = no variation
    var newEntity: Entity = Entity.NONE,  // If spawner shall take a specific entity for spawning it can be set here
    var entityConfig: String = "",        // Name of entity configuration which is used to create and configure the new entity
    var totalNumberOfObjects: Int = -1,   // -1 - unlimited number of objects spawned, x = x-number of objects spawned in total
    // internal state
    var nextSpawnIn: Int = 0,
    var numberOfObjectsSpawned: Int = 0
) : PoolableComponent<Spawner>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: Spawner) {
        numberOfObjects = from.numberOfObjects
        interval = from.interval
        timeVariation = from.timeVariation
        positionVariation = from.positionVariation
        newEntity = from.newEntity
        entityConfig = from.entityConfig
        totalNumberOfObjects = from.totalNumberOfObjects
        nextSpawnIn = from.nextSpawnIn
        numberOfObjectsSpawned = from.numberOfObjectsSpawned
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        numberOfObjects = 1
        interval = 1
        timeVariation = 0
        positionVariation = 0f
        newEntity = Entity.NONE
        entityConfig = ""
        totalNumberOfObjects = -1
        nextSpawnIn = 0
        numberOfObjectsSpawned = 0
    }

    override fun type() = SpawnerComponent

    companion object {
        val SpawnerComponent = componentTypeOf<Spawner>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticSpawnerComponent(config: Spawner.() -> Unit ): Spawner =
        Spawner().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun spawnerComponent(config: Spawner.() -> Unit ): Spawner =
        pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE) { Spawner() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): Spawner = spawnerComponent { init(from = this@Spawner ) }

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
