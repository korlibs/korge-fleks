package korlibs.korge.fleks.assets.data.gameObject

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable @SerialName("CollisionRect")
data class CollisionRect(
    // Anchor point of the collision rectangle to the pivot point of the entity
    val x: Int,
    val y: Int,
    // Size of the collision rectangle
    val width: Float,
    val height: Float
) {
    companion object {
        val EMPTY = CollisionRect(0, 0, 0f, 0f)
    }
}
