package korlibs.korge.fleks.utils.poolableData

import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


// TODO: remove later and use Point instead
@Serializable @SerialName("Point")
data class PointOld (
    var x: Float = 0f,
    var y: Float = 0f
) : CloneableData<PointOld> {

    // Perform deep copy with special handling for entity, position and rgba.
    override fun clone(): PointOld = this.copy()
}

/**
 * A simple 2D point with x and y coordinates which is cloneable and serializable.
 */
@Serializable @SerialName("Point")
class Point private constructor(
    var x: Float = 0f,
    var y: Float = 0f
) : PoolableData<Point> {
    override fun clone(from: Point) {
        this.x = from.x
        this.y = from.y
    }

    companion object {
        // TODO: get from PointPool
        val ZERO = Point()

        fun point(): Point {
            // TODO: get from PointPool
            return Point()
        }
    }

    override fun free() {
        // TODO: return to PointPool
    }
}
