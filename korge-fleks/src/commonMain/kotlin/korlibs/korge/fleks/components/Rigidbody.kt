package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This is a very basic definition of a rigid body which does not take rotation into account.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("Rigidbody")
class Rigidbody private constructor(
    var mass: Float = 0f,      // mass to calculate inertia of the object
    var damping: Float = 0f,   // e.g. air resistance of the object when falling
    var friction: Float = 0f,  // e.g. friction of the object when it moves over surfaces
) : Poolable<Rigidbody>() {
    override fun type() = RigidbodyComponent

    companion object {
        val RigidbodyComponent = componentTypeOf<Rigidbody>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticRigidbodyComponent(config: Rigidbody.() -> Unit ): Rigidbody =
            Rigidbody().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun World.RigidbodyComponent(config: Rigidbody.() -> Unit ): Rigidbody =
        getPoolable(RigidbodyComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addRigidbodyComponentPool(preAllocate: Int = 0) {
            addPool(RigidbodyComponent, preAllocate) { Rigidbody() }
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): Rigidbody =
        getPoolable(RigidbodyComponent).apply { init(from = this@Rigidbody ) }

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

    // Cleanup/Reset the component automatically when it is removed from an entity
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }
}
