package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.data.Point
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.interpolate
import korlibs.math.interpolation.toRatio
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to ...
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("Grid")
class Grid private constructor() : PoolableComponent<Grid>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: Grid) {
        cx = from.cx
        cy = from.cy
        xr = from.xr
        yr = from.yr
        zr = from.zr
        interpolatePixelPosition = from.interpolatePixelPosition
        interpolationAlpha = from.interpolationAlpha
        lastPx = from.lastPx
        lastPy = from.lastPy
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
//        TODO()
    }

    override fun type() = GridComponent

    companion object {
        val GridComponent = componentTypeOf<Grid>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticGridComponent(config: Grid.() -> Unit): Grid =
            Grid().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun gridComponent(config: Grid.() -> Unit): Grid =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "Grid") { Grid() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): Grid = gridComponent { init(from = this@Grid) }

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
    var xr: Float = 0f
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

    var interpolatePixelPosition: Boolean = true

    /**
     * The ratio to interpolate the last position to the new position.
     * This will need updated before each update.
     */
    var interpolationAlpha: Float = 1f
    var x: Float
        get() {
            return if (interpolatePixelPosition) {
                interpolationAlpha.toRatio().interpolate(lastPx, attachX)
            } else {
                attachX
            }
        }
        set(value) {
            cx = (value / AppConfig.GRID_CELL_SIZE).toInt()
            xr = (value - cx * AppConfig.GRID_CELL_SIZE) / AppConfig.GRID_CELL_SIZE
            onPositionManuallyChanged()
        }

    var y: Float
        get() {
            return if (interpolatePixelPosition) {
                interpolationAlpha.toRatio().interpolate(lastPy, attachY)
            } else {
                 attachY
            }
        }
        set(value) {
            cy = (value / AppConfig.GRID_CELL_SIZE).toInt()
            yr = (value - cy * AppConfig.GRID_CELL_SIZE) / AppConfig.GRID_CELL_SIZE
            onPositionManuallyChanged()
        }

    var lastPx: Float = 0f
    var lastPy: Float = 0f

    // The position of the entity in level coordinates.
    val attachX get() = (cx + xr) * AppConfig.GRID_CELL_SIZE
    val attachY get() = (cy + yr - zr) * AppConfig.GRID_CELL_SIZE

    private fun onPositionManuallyChanged() {
        lastPx = attachX
        lastPy = attachY
    }

    /**
     * Convert the position of the entity to world coordinates.
     * This is useful to convert the position of a touch position into world coordinates.
     */
    fun World.convertToWorldCoordinates(camera: Entity) {
        val cameraPosition = camera[PositionComponent]
        x += cameraPosition.x - cameraPosition.offsetX - AppConfig.VIEW_PORT_WIDTH_HALF
        y += cameraPosition.y - cameraPosition.offsetY - AppConfig.VIEW_PORT_HEIGHT_HALF
    }

    /**
     * Convert the position of the entity to screen coordinates.
     * This is useful to convert the position of an entity to screen coordinates for rendering.
     */
    fun World.convertToScreenCoordinates(camera: Entity, position: Point) {
        val cameraPosition = camera[PositionComponent]
        position.x = x  - cameraPosition.x + cameraPosition.offsetX + AppConfig.VIEW_PORT_WIDTH_HALF
        position.y = y - cameraPosition.y + cameraPosition.offsetY + AppConfig.VIEW_PORT_HEIGHT_HALF
    }
}
