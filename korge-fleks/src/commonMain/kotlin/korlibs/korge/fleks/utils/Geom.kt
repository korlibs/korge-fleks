package korlibs.korge.fleks.utils

import kotlinx.serialization.*


/**
 * A simple 2D point with x and y coordinates which is cloneable and serializable.
 */
@Serializable @SerialName("Point")
data class Point(
    var x: Float = 0f,
    var y: Float = 0f
) : CloneableData<Point> {
    override fun clone(): Point = this.copy()

    companion object {
        val ZERO = Point()
    }
}
