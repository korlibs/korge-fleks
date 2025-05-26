package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to add health to a game object.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("LifeCycle")
class LifeCycle private constructor(
    var healthCounter: Int = 100
) : Poolable<LifeCycle>() {
    override fun type() = LifeCycleComponent

    companion object {
        val LifeCycleComponent = componentTypeOf<LifeCycle>()

        // Use this function to get a new instance from the pool
        fun World.LifeCycleComponent(config: LifeCycle.() -> Unit ): LifeCycle =
        getPoolable(LifeCycleComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addLifeCycleComponentPool(preAllocate: Int = 0) {
            addPool(LifeCycleComponent, preAllocate) { LifeCycle() }
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): LifeCycle =
    getPoolable(LifeCycleComponent).apply {
        healthCounter = this@LifeCycle.healthCounter
    }

    // Cleanup the component automatically when it is removed from an entity
    override fun World.cleanupComponent(entity: Entity) {
        healthCounter = 100
    }
}
