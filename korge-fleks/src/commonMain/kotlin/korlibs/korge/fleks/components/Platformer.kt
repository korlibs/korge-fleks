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
@Serializable @SerialName("Platformer")
class Platformer private constructor(
    var onGround: Boolean = true
) : PoolableComponents<Platformer>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: Platformer) {
        onGround = from.onGround
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        onGround = true
    }

    override fun type() = PlatformerComponent

    companion object {
        val PlatformerComponent = componentTypeOf<Platformer>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticPlatformerComponent(config: Platformer.() -> Unit ): Platformer =
            Platformer().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun World.PlatformerComponent(config: Platformer.() -> Unit ): Platformer =
            getPoolable(PlatformerComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addPlatformerComponentPool(preAllocate: Int = 0) {
            addPool(PlatformerComponent, preAllocate) { Platformer() }
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): Platformer =
        getPoolable(PlatformerComponent).apply { init(from = this@Platformer ) }
}
