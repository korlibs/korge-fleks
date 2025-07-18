package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.data.Point
import korlibs.korge.fleks.components.data.Point.Companion.staticPoint
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to store collision information of a game object.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("Collision")
class Collision private constructor(
    var name: String = "",
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
    var jumpVelocity: Float = 0f,  // TODO: check with MotionComponent
    var justHit: Boolean = false,
    var isHit: Boolean = false,
    val hitPosition: Point = staticPoint {}
) : PoolableComponent<Collision>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: Collision) {
        name = from.name
        right = from.right
        left = from.left
        isCollidingAbove = from.isCollidingAbove
        isGrounded = from.isGrounded
        becameGroundedThisFrame = from.becameGroundedThisFrame
        wasGroundedLastFrame = from.wasGroundedLastFrame
        movingDownSlope = from.movingDownSlope
        slopeAngle = from.slopeAngle
        isFalling = from.isFalling
        collisionWithStaticObject = from.collisionWithStaticObject
        jumpVelocity = from.jumpVelocity
        justHit = from.justHit
        isHit = from.isHit
        hitPosition.init(from = from.hitPosition)
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
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
        hitPosition.cleanup()
    }

    override fun type() = CollisionComponent

    companion object {
        val CollisionComponent = componentTypeOf<Collision>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticCollisionComponent(config: Collision.() -> Unit): Collision =
            Collision().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun collisionComponent(config: Collision.() -> Unit): Collision =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "Collision") { Collision() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): Collision = collisionComponent { init(from = this@Collision) }

    // Initialize the component automatically when it is added to an entity
    override fun World.initComponent(entity: Entity) {
    }

    // Cleanup/Reset the component automatically when it is removed from an entity (component will be returned to the pool eventually)
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }

    // Initialize an external prefab when the component is added to an entity
    override fun World.initPrefabs(entity: Entity) {
    }

    // Cleanup/Reset an external prefab when the component is removed from an entity
    override fun World.cleanupPrefabs(entity: Entity) {
    }

    // Free the component and return it to the pool - this is called directly by the SnapshotSerializerSystem
    override fun free() {
        cleanup()
        pool.free(this)
    }
}
