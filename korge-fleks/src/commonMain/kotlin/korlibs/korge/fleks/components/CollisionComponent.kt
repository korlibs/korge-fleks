package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.fleks.utils.Point
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This component is used to store collision information of a game object.
 */
@Serializable @SerialName("Collision")
data class CollisionComponent(
    var configName: String = "",

    var right: Boolean = false,
    var left: Boolean = false,
    var isCollidingAbove: Boolean = false,
    var isGrounded: Boolean = false,
    var becameGroundedThisFrame: Boolean = false,
    var wasGroundedLastFrame: Boolean = false,
    var movingDownSlope: Boolean = false,
    var slopeAngle: Float = 0f,
    var isFalling: Boolean = false,
    var collisionWithStaticObject: Boolean = false,  // used currently e.g. by shoot objects
    var jumpVelocity: Float = 0f,
    var justHit: Boolean = false,
    var isHit: Boolean = false,
    var hitPosition: Point = Point.ZERO
) : CloneableComponent<CollisionComponent>() {
    override fun type() = CollisionComponent

    companion object : ComponentType<CollisionComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): CollisionComponent =
        this.copy()

    // TODO: Not yet used - take into use with poolable components
    fun reset() {
        right = false
        left = false
        isCollidingAbove = false
        isGrounded = false
        becameGroundedThisFrame = false
        wasGroundedLastFrame = false
        movingDownSlope = false
        slopeAngle = 0f
        isFalling = false
        collisionWithStaticObject = false
        jumpVelocity = 0f
        justHit = false
        isHit = false
        hitPosition = Point.ZERO
    }
}
