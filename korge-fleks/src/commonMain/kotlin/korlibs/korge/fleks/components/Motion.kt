package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * A component to define movement for an entity.
 *
 * @param velocityX in "world units" per delta time
 * @param velocityY in "world units" per delta time
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("Motion")
class Motion private constructor(
    var accelX: Float = 0f,
    var accelY: Float = 0f,

    var velocityX: Float = 0f,
    var velocityY: Float = 0f,
    var velocityZ: Float = 0f,
    var frictionX: Float = 0.82f,
    var frictionY: Float = 0.82f,
    var frictionZ: Float = 0.82f
) : PoolableComponent<Motion>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: Motion) {
        accelX = from.accelX
        accelY = from.accelY
        velocityX = from.velocityX
        velocityY = from.velocityY
        frictionX = from.frictionX
        frictionY = from.frictionY
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        accelX = 0f
        accelY = 0f
        velocityX = 0f
        velocityY = 0f
        frictionX = 0.82f
        frictionY = 0.82f
    }

    override fun type() = MotionComponent

    companion object {
        val MotionComponent = componentTypeOf<Motion>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticMotionComponent(config: Motion.() -> Unit ): Motion =
        Motion().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun motionComponent(config: Motion.() -> Unit ): Motion =
        pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE) { Motion() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): Motion = motionComponent { init(from = this@Motion ) }

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
