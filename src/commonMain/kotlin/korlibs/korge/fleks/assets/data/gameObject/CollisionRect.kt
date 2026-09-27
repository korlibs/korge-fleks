package korlibs.korge.fleks.assets.data.gameObject

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This class represents a collision rectangle for a game object.
 * It defines the position and size of the rectangle relative to the pivot point of the entity.
 *
 * Objects of this class are immutable and are used to specify rect in the collision component.
 * It is therefore serializable by itself and used as a base type within the collision component.
 */
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
