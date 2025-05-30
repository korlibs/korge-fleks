package korlibs.korge.fleks.components.collision

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to ...
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("GridCollisionResult")
class GridCollisionResult private constructor(
    var axes: Axes = Axes.X,  // The axes of the collision (X, Y, Z)
    var dir: Int = 0          // -1 or 1, depending on the direction of the collision
) : Poolable<GridCollisionResult>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: GridCollisionResult) {
        axes = from.axes
        dir = from.dir
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
//    fun cleanup() {
    override fun reset() {
        axes = Axes.X
        dir = 0
    }

    enum class Axes { X, Y, Z }

    override fun type(): ComponentType<GridCollisionResult> = when (axes) {
        Axes.X -> GridCollisionXComponent
        Axes.Y -> GridCollisionYComponent
        Axes.Z -> GridCollisionZComponent
    }

    companion object {
        val GridCollisionXComponent = componentTypeOf<GridCollisionResult>()
        val GridCollisionYComponent = componentTypeOf<GridCollisionResult>()
        val GridCollisionZComponent = componentTypeOf<GridCollisionResult>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticGridCollisionResultComponent(config: GridCollisionResult.() -> Unit): GridCollisionResult =
            GridCollisionResult().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun World.gridCollisionXComponent(config: GridCollisionResult.() -> Unit): GridCollisionResult =
            getPoolable(GridCollisionXComponent).apply { axes = Axes.X }.apply(config)

        fun World.GridCollisionYComponent(config: GridCollisionResult.() -> Unit): GridCollisionResult =
            getPoolable(GridCollisionYComponent).apply { axes = Axes.Y }.apply(config)

        fun World.GridCollisionZComponent(config: GridCollisionResult.() -> Unit): GridCollisionResult =
            getPoolable(GridCollisionZComponent).apply { axes = Axes.Z }.apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addGridCollisionResultComponentPools(preAllocate: Int = 0) {
            // Add only one common pool for all TweenProperty components - they share the same data
            val pool = Pool(preAllocate) { GridCollisionResult() }
            addPool(GridCollisionXComponent, pool)
            addPool(GridCollisionYComponent, pool)
            addPool(GridCollisionZComponent, pool)
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): GridCollisionResult =
        getPoolable(GridCollisionXComponent).apply { init(from = this@GridCollisionResult ) }

//    // Initialize the component automatically when it is added to an entity
//    override fun World.initComponent(entity: Entity) {
//    }
//
//    // Cleanup/Reset the component automatically when it is removed from an entity (component will be returned to the pool eventually)
//    override fun World.cleanupComponent(entity: Entity) {
//        cleanup()
//    }
//
//    // Initialize an external prefab when the component is added to an entity
//    override fun World.initPrefabs(entity: Entity) {
//    }
//
//    // Cleanup/Reset an external prefab when the component is removed from an entity
//    override fun World.cleanupPrefabs(entity: Entity) {
//    }
}
