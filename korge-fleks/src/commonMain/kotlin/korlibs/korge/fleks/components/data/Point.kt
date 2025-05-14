package korlibs.korge.fleks.components.data

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


/**
 * A simple 2D point with x, y coordinates which is poolable and serializable.
 */
@Serializable @SerialName("Point")
class Point private constructor(
    var x: Float = 0f,
    var y: Float = 0f
) : Poolable<Point>() {
    fun init(from: Point) {
        x = from.x
        y = from.y
    }

    fun cleanup() {
        x = 0f
        y = 0f
    }

    override fun type() = PointData
    companion object {
        val PointData = componentTypeOf<Point>()

        // Use this function to create a new Point instance as val inside another component.
        fun value(): Point = Point()

        fun InjectableConfiguration.addPointDataPool(preAllocate: Int = 0) {
            addPool(PointData, preAllocate) { Point() }
        }
    }

    override fun World.clone(): Point = getPoolable(PointData).apply { init(from = this@Point) }
}
