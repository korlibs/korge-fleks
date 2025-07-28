package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.data.ListOfPoints
import korlibs.korge.fleks.components.data.ListOfPoints.Companion.listOfPoints
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component can store different offsets for each animation frame of a sprite.
 *
 * @param mapOfOffsetLists A map which saves a list of offsets (values) for each animation frame per animation name (keys).
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("OffsetByFrameIndex")
class OffsetByFrameIndex private constructor(
    var entity: Entity = Entity.NONE,
    // Map of list of points is static - therefore use references to the map when creating copies of the component in init function
    val mapOfOffsetLists: MutableMap<String, ListOfPoints> = mutableMapOf()
) : PoolableComponent<OffsetByFrameIndex>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: OffsetByFrameIndex) {
        entity = from.entity
        mapOfOffsetLists.init(from.mapOfOffsetLists)
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        entity = Entity.NONE
        // Lists of Points are static and will be freed to the pool in cleanupComponent function when entity is destroyed
        mapOfOffsetLists.freeAndClear()
    }

    override fun type() = OffsetByFrameIndexComponent

    companion object {
        val OffsetByFrameIndexComponent = componentTypeOf<OffsetByFrameIndex>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticOffsetByFrameIndexComponent(config: OffsetByFrameIndex.() -> Unit): OffsetByFrameIndex =
            OffsetByFrameIndex().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun offsetByFrameIndexComponent(config: OffsetByFrameIndex.() -> Unit): OffsetByFrameIndex =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "OffsetByFrameIndex") { OffsetByFrameIndex() }

        /**
         * Init function (deep copy) for [MutableMap] of String keys and [ListOfPoints] values.
         * This will clone each list of points and add it to the map.
         */
        fun MutableMap<String, ListOfPoints>.init(from: Map<String, ListOfPoints>) {
            from.forEach { (key, list) ->
                this[key] = list.clone()
            }
        }

        /**
         * Free all lists of points in the map and clear the map.
         * This will free each list of points and clear the map.
         */
        fun MutableMap<String, ListOfPoints>.freeAndClear() {
            this.forEach { (_, list) ->
                list.free()
            }
            this.clear()
        }

    }

    // Clone a new instance of the component from the pool
    override fun clone(): OffsetByFrameIndex = offsetByFrameIndexComponent { init(from = this@OffsetByFrameIndex) }

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

    /**
     * Creates a new list of points and adds it to the map of offset lists.
     * The name is used as a key to access the list later.
     *
     * @param name The name of the list of points.
     * @param config Optional configuration block to initialize the list of points.
     * @return The created [ListOfPoints] instance.
     */
    fun createListOfPoints(name: String, config: ListOfPoints.() -> Unit = {}): ListOfPoints {
        val listOfPoints = listOfPoints { config() }
        mapOfOffsetLists[name] = listOfPoints
        return listOfPoints
    }
}
