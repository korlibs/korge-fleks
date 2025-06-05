package korlibs.korge.fleks.components.data

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * A simple 2D point with x, y coordinates which is poolable and serializable.
 */
@Serializable @SerialName("Point")
class Point private constructor(
    var x: Float = 0f,
    var y: Float = 0f
) : Poolable<Point> {
    // Init an existing data instance with data from another one
    override fun init(from: Point) {
        x = from.x
        y = from.y
    }

    // Cleanup data instance manually
    // This is used for data instances when they are part (val property) of a component
    override fun cleanup() {
        x = 0f
        y = 0f
    }

    // Clone a new data instance from the pool
    override fun clone(): Point = point { init(from = this@Point ) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as val inside a component
        fun staticPoint(config: Point.() -> Unit ): Point =
            Point().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun point(config: Point.() -> Unit ): Point =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE) { Point() }
    }

    /**
     * Convert the position to screen coordinates.
     * This is useful to convert the position of an entity to screen coordinates for rendering.
     */
    fun World.convertToScreenCoordinates(camera: Entity) {
        val cameraPosition = camera[PositionComponent]
        x = x  - cameraPosition.x + cameraPosition.offsetX + AppConfig.VIEW_PORT_WIDTH_HALF
        y = y - cameraPosition.y + cameraPosition.offsetY + AppConfig.VIEW_PORT_HEIGHT_HALF
    }
}
