package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.componentPool.*
import korlibs.korge.fleks.utils.poolableData.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to store collision information of a game object.
 *
 * Author's hint: When adding new properties to the component, make sure to initialize them in the
 *                [reset] function and copy them in the [clone] function.
 */
@Serializable @SerialName("Collision")
class Collision private constructor(
    var configName: PoolableString = PoolableString.EMPTY,
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
    val hitPosition: Point = Point.ZERO
) : PoolableComponent<Collision>() {

    override fun type() = CollisionComponent
    companion object {
        val CollisionComponent = componentTypeOf<Collision>()

        fun World.CollisionComponent(config: Collision.() -> Unit ): Collision =
            getPoolable(CollisionComponent).apply { config() }

        fun InjectableConfiguration.addCollisionComponentPool(preAllocate: Int = 0) {
            addPool(CollisionComponent, preAllocate) { Collision() }
        }
    }

    override fun World.clone(): Collision =
        getPoolable(CollisionComponent).apply {
            configName = this@Collision.configName
            right = this@Collision.right
            left = this@Collision.left
            isCollidingAbove = this@Collision.isCollidingAbove
            isGrounded = this@Collision.isGrounded
            becameGroundedThisFrame = this@Collision.becameGroundedThisFrame
            wasGroundedLastFrame = this@Collision.wasGroundedLastFrame
            movingDownSlope = this@Collision.movingDownSlope
            slopeAngle = this@Collision.slopeAngle
            isFalling = this@Collision.isFalling
            collisionWithStaticObject = this@Collision.collisionWithStaticObject
            jumpVelocity = this@Collision.jumpVelocity
            justHit = this@Collision.justHit
            isHit = this@Collision.isHit
            hitPosition.clone(this@Collision.hitPosition)
        }

    override fun reset() {
        // TODO: Cleanup properties which does not need a reset because they will be overwritten anyway at the beginning of the frame cycle
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
        // Deep init of hit position - reuse object
        hitPosition.x = 0f
        hitPosition.y = 0f
    }
}
