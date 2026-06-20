package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to add touch input to an entity.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("TouchInput")
class TouchInput private constructor(
    var enabled: Boolean = true,
    var entity: Entity = Entity.NONE,  // If touch was triggered than below EntityConfig will be executed for this Entity
    var entityConfig: String = "",
    var passPositionToEntity: Boolean = false,
    var continuousTouch: Boolean = false
) : PoolableComponent<TouchInput>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: TouchInput) {
        enabled = from.enabled
        entity = from.entity
        entityConfig = from.entityConfig
        passPositionToEntity = from.passPositionToEntity
        continuousTouch = from.continuousTouch
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        enabled = true
        entity = Entity.NONE
        entityConfig = ""
        passPositionToEntity = false
        continuousTouch = false
    }

    override fun type() = TouchInputComponent

    companion object {
        val TouchInputComponent = componentTypeOf<TouchInput>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticTouchInputComponent(config: TouchInput.() -> Unit): TouchInput =
            TouchInput().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun touchInputComponent(config: TouchInput.() -> Unit): TouchInput =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "TouchInput") { TouchInput() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): TouchInput = touchInputComponent { init(from = this@TouchInput) }

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
