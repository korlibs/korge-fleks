package korlibs.korge.fleks.components


import com.github.quillraven.fleks.*
import korlibs.korge.fleks.state.PlayerInputState.Companion.NEUTRAL_DIRECTION
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to store the direction and flip state of an entity in a 2D game world.
 * This rotation component is designed to work with pixel art and is intended for use in games that
 * require precise control over the orientation of entities.
 *
 * It contains properties for the direction (an integer value representing the direction), as well as
 * boolean flags for flipping the entity along the X and Y axes.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("RotationPixel2D")
class RotationPixel2D private constructor(
    var direction: Int = NEUTRAL_DIRECTION,  // [0..32]
    var flipX: Boolean = false,
    var flipY: Boolean = false
) : PoolableComponent<RotationPixel2D>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are a value property of another component
    fun init(from: RotationPixel2D) {
        direction = from.direction
        flipX = from.flipX
        flipY = from.flipY
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are a value property of another component
    fun cleanup() {
        direction = NEUTRAL_DIRECTION
        flipX = false
        flipY = false
    }

    override fun type() = RotationPixel2DComponent

    companion object {
        val RotationPixel2DComponent = componentTypeOf<RotationPixel2D>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticDirectionComponent(config: RotationPixel2D.() -> Unit): RotationPixel2D =
            RotationPixel2D().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun rotationPixel2DComponent(config: RotationPixel2D.() -> Unit): RotationPixel2D =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "Direction") { RotationPixel2D() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): RotationPixel2D = rotationPixel2DComponent { init(from = this@RotationPixel2D) }

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
