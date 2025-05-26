package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is manipulating the rgba value of [SpriteLayersComponent].
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("SwitchLayerVisibility")
class SwitchLayerVisibility private constructor(
    var offVariance: Float = 0f,  // variance in switching value off: 1f - every frame switching possible, 0f - no switching at all
    var onVariance: Float = 1f,   // variance in switching value on again: 1f - changed value switches back immediately, 0f - changed value stays forever
) : Poolable<SwitchLayerVisibility>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: SwitchLayerVisibility) {
        offVariance = from.offVariance
        onVariance = from.onVariance
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        offVariance = 0f
        onVariance = 1f
    }

    override fun type() = SwitchLayerVisibilityComponent

    companion object {
        val SwitchLayerVisibilityComponent = componentTypeOf<SwitchLayerVisibility>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticSwitchLayerVisibilityComponent(config: SwitchLayerVisibility.() -> Unit ): SwitchLayerVisibility =
            SwitchLayerVisibility().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun World.SwitchLayerVisibilityComponent(config: SwitchLayerVisibility.() -> Unit ): SwitchLayerVisibility =
        getPoolable(SwitchLayerVisibilityComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addSwitchLayerVisibilityComponentPool(preAllocate: Int = 0) {
            addPool(SwitchLayerVisibilityComponent, preAllocate) { SwitchLayerVisibility() }
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): SwitchLayerVisibility =
        getPoolable(SwitchLayerVisibilityComponent).apply { init(from = this@SwitchLayerVisibility ) }

    // Cleanup/Reset the component automatically when it is removed from an entity (component will be returned to the pool eventually)
    override fun World.cleanupComponent(entity: Entity) { cleanup() }
}
