package korlibs.korge.fleks.components

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
@Serializable @SerialName("Grid")
class Grid private constructor(
) : PoolableComponents<Grid>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: Grid) {
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
    }

    override fun type() = GridComponent

    companion object {
        val GridComponent = componentTypeOf<Grid>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticGridComponent(config: Grid.() -> Unit ): Grid =
            Grid().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun World.GridComponent(config: Grid.() -> Unit ): Grid =
            getPoolable(GridComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addGridComponentPool(preAllocate: Int = 0) {
            addPool(GridComponent, preAllocate) { Grid() }
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): Grid =
        getPoolable(GridComponent).apply { init(from = this@Grid ) }


// tODO: dirty flag is used for view port boundary check - is entity within the view port?

    var cx: Int = 0
        set(value) {
            if (field == value) return
            field = value
//            dirty = true
        }
    var cy: Int = 0
        set(value) {
            if (field == value) return
            field = value
//            dirty = true
        }
    var xr: Float = 0.5f
        set(value) {
            if (field == value) return
            field = value
//            dirty = true
        }
    var yr: Float = 0f
        set(value) {
            if (field == value) return
            field = value
//            dirty = true
        }
    // Z ordering index for the entity in the grid -- not used
    var zr: Float = 0f
        set(value) {
            if (field == value) return
            field = value
//            dirty = true
        }

    var lastPx: Float = 0f
    var lastPy: Float = 0f

    val attachX get() = (cx + xr) * AppConfig.GRID_CELL_SIZE
    val attachY get() = (cy + yr - zr) * AppConfig.GRID_CELL_SIZE
}
