package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.Point
import korlibs.korge.fleks.utils.componentPool.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This component is used to store collision information of a game object.
 */
@Serializable @SerialName("Collision")
data class Collision(
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
) : PoolableComponent<Collision>() {

    override fun type(): ComponentType<Collision> = CollisionComponent

    companion object {
        val CollisionComponent = componentTypeOf<Collision>()

        fun InjectableConfiguration.addCollisionComponentPool(preAllocate: Int = 0) {
            addPool(CollisionComponent, preAllocate) { Collision() }
        }
    }

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun World.clone(): Collision =
        getPoolable(CollisionComponent).apply {
            configName = this@Collision.configName

            // TODO

            hitPosition = this@Collision.hitPosition.clone()
        }

    override fun reset() {
        // TODO: Cleanup properties which does not need a reset
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
