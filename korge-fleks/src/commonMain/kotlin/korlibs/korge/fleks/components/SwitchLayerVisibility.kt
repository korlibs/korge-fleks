package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is manipulating the rgba value of [SpriteLayersComponent].
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("SwitchLayerVisibility")
class SwitchLayerVisibility private constructor(
    var offVariance: Float = 0f,  // variance in switching value off: 1f - every frame switching possible, 0f - no switching at all
    var onVariance: Float = 1f,   // variance in switching value on again: 1f - changed value switches back immediately, 0f - changed value stays forever
) : PoolableComponent<SwitchLayerVisibility>() {
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
        fun switchLayerVisibilityComponent(config: SwitchLayerVisibility.() -> Unit ): SwitchLayerVisibility =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "SwitchLayerVisibility") { SwitchLayerVisibility() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): SwitchLayerVisibility = switchLayerVisibilityComponent { init(from = this@SwitchLayerVisibility ) }

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
