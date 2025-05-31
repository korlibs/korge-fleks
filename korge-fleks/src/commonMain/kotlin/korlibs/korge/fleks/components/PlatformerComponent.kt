package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to ...
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("PlatformerCollision")
class PlatformerCollision private constructor(
    var onGround: Boolean = false
) : Poolable<PlatformerCollision>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: PlatformerCollision) {
        onGround = from.onGround
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    override fun reset() {
        onGround = false
    }

    override fun type() = PlatformerCollisionComponent

    companion object {
        val PlatformerCollisionComponent = componentTypeOf<PlatformerCollision>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticPlatformerComponent(config: PlatformerCollision.() -> Unit ): PlatformerCollision =
            PlatformerCollision().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun World.PlatformerCollisionComponent(config: PlatformerCollision.() -> Unit ): PlatformerCollision =
            getPoolable(PlatformerCollisionComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addPlatformerComponentPool(preAllocate: Int = 0) {
            addPool(PlatformerCollisionComponent, preAllocate) { PlatformerCollision() }
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): PlatformerCollision =
        getPoolable(PlatformerCollisionComponent).apply { init(from = this@PlatformerCollision ) }
}
