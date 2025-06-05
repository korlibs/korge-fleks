package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This is a very basic definition of a rigid body which does not take rotation into account.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("Rigidbody")
class Rigidbody private constructor(
    var mass: Float = 0f,      // mass to calculate inertia of the object
    var damping: Float = 0f,   // e.g. air resistance of the object when falling
    var friction: Float = 0f,  // e.g. friction of the object when it moves over surfaces
) : PoolableComponent<Rigidbody>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: Rigidbody) {
        mass = from.mass
        damping = from.damping
        friction = from.friction
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        mass = 0f
        damping = 0f
        friction = 0f
    }

    override fun type() = RigidbodyComponent

    companion object {
        val RigidbodyComponent = componentTypeOf<Rigidbody>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticRigidbodyComponent(config: Rigidbody.() -> Unit ): Rigidbody =
        Rigidbody().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun rigidbodyComponent(config: Rigidbody.() -> Unit ): Rigidbody =
        pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE) { Rigidbody() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): Rigidbody = rigidbodyComponent { init(from = this@Rigidbody ) }

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
