package korlibs.korge.fleks.components.data

import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This class is used as simple 2D point with x, y coordinates. It is poolable and serializable.
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
    // This is used for data instances when they are a value property of a component
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
        // Use this function to create a new instance of data as value property inside a component
        fun staticPoint(config: Point.() -> Unit ): Point =
            Point().apply(config)

        // Use this function to get a new instance of the data object from the pool
        fun point(config: Point.() -> Unit ): Point =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "Point") { Point() }
    }
}
