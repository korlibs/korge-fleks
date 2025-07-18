package korlibs.korge.fleks.components.data

import korlibs.korge.fleks.components.data.Point.Companion.point
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This class is used to create a mutable list of [Point] objects which itself is owned by a pool.
 */
@Serializable @SerialName("ListOfPoints")
class ListOfPoints private constructor(
    val points: MutableList<Point> = mutableListOf()
) : Poolable<ListOfPoints> {
    // Init an existing data instance with data from another one
    override fun init(from: ListOfPoints) {
        points.init(from.points)
    }

    // Cleanup data instance manually
    // This is used for data instances when they are a value property of a component
    override fun cleanup() {
        points.freeAndClear()
    }

    // Clone a new data instance from the pool
    override fun clone(): ListOfPoints = listOfPoints { init(from = this@ListOfPoints) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticListOfPoints(config: ListOfPoints.() -> Unit): ListOfPoints =
            ListOfPoints().apply(config)

        // Use this function to get a new instance of the data object from the pool
        fun listOfPoints(config: ListOfPoints.() -> Unit): ListOfPoints =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "ListOfPoints") { ListOfPoints() }
    }

    /**
     * Adds a new [Point] to the list of points.
     * @param x The x-coordinate of the point.
     * @param y The y-coordinate of the point.
     */
    fun addPoint(x: Float, y: Float) {
        points.add( point { this.x = x; this.y = y } )
    }
}
