package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.data.Point
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to ...
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("DebugCollisionShapes")
class DebugCollisionShapes private constructor(
    val gridCells: MutableList<Point> = mutableListOf(),
    val ratioPositions: MutableList<Point> = mutableListOf()
) : PoolableComponent<DebugCollisionShapes>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are a value property of another component
    fun init(from: DebugCollisionShapes) {
        gridCells.init(from.gridCells)
        ratioPositions.init(from.ratioPositions)
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are a value property of another component
    fun cleanup() {
        gridCells.freeAndClear()
        ratioPositions.freeAndClear()
    }

    override fun type() = DebugCollisionShapesComponent

    companion object {
        val DebugCollisionShapesComponent = componentTypeOf<DebugCollisionShapes>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticDebugCollisionShapesComponent(config: DebugCollisionShapes.() -> Unit ): DebugCollisionShapes =
            DebugCollisionShapes().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun debugCollisionShapesComponent(config: DebugCollisionShapes.() -> Unit ): DebugCollisionShapes =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "DebugCollisionShapes") { DebugCollisionShapes() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): DebugCollisionShapes = debugCollisionShapesComponent { init(from = this@DebugCollisionShapes ) }

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
