package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to apply gravity to an entity.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("Gravity")
class Gravity private constructor(
    var gravityX: Float = 0f,
    var gravityY: Float = 0f,
    var gravityZ: Float = 0f,

    var gravityMultiplier: Float = 0f,

    var enableGravityX: Boolean = false,
    var enableGravityY: Boolean = false,
    var enableGravityZ: Boolean = false
) : PoolableComponent<Gravity>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: Gravity) {
        gravityX = from.gravityX
        gravityY = from.gravityY
        gravityZ = from.gravityZ
        gravityMultiplier = from.gravityMultiplier
        enableGravityX = from.enableGravityX
        enableGravityY = from.enableGravityY
        enableGravityZ = from.enableGravityZ
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        gravityX = 0f
        gravityY = 0f
        gravityZ = 0f
        gravityMultiplier = 0f
        enableGravityX = false
        enableGravityY = false
        enableGravityZ = false
    }

    override fun type() = GravityComponent

    companion object {
        val GravityComponent = componentTypeOf<Gravity>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticGravityComponent(config: Gravity.() -> Unit): Gravity =
            Gravity().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun gravityComponent(config: Gravity.() -> Unit): Gravity =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "Gravity") { Gravity() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): Gravity = gravityComponent { init(from = this@Gravity) }

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

    fun calculateDeltaXGravity(): Float {
        return if (enableGravityX) {
            gravityMultiplier * gravityX
        } else {
            0f
        }
    }

    fun calculateDeltaYGravity(): Float {
        return if (enableGravityY) {
            gravityMultiplier * gravityY
        } else {
            0f
        }
    }

    fun calculateDeltaZGravity(): Float {
        return if (enableGravityZ) {
            gravityMultiplier * gravityZ
        } else {
            0f
        }
    }

    fun enableAll(enable: Boolean) {
        enableGravityX = enable
        enableGravityY = enable
        enableGravityZ = enable
    }
}
