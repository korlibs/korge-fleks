package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * A component to define movement for an entity.
 *
 * @param velocityX in "world units" per delta time
 * @param velocityY in "world units" per delta time
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("Motion")
class Motion private constructor(
    var accelX: Float = 0f,
    var accelY: Float = 0f,

    var velocityX: Float = 0f,
    var velocityY: Float = 0f,
    var frictionX: Float = 0.82f,
    var frictionY: Float = 0.82f
) : Poolable<Motion>() {
    // Init an existing component instance with data from another component
    fun init(from: Motion) {
        accelX = from.accelX
        accelY = from.accelY
        velocityX = from.velocityX
        velocityY = from.velocityY
        frictionX = from.frictionX
        frictionY = from.frictionY
    }

    // Cleanup the component instance manually
    fun cleanup() {
        accelX = 0f
        accelY = 0f
        velocityX = 0f
        velocityY = 0f
        frictionX = 0.82f
        frictionY = 0.82f
    }

    override fun type() = MotionComponent

    companion object {
        val MotionComponent = componentTypeOf<Motion>()

        // Use this function to create a new instance as val inside another component
        fun staticMotionComponent(config: Motion.() -> Unit ): Motion =
            Motion().apply(config)

        // Use this function to get a new instance from the pool
        fun World.MotionComponent(config: Motion.() -> Unit ): Motion =
        getPoolable(MotionComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addMotionComponentPool(preAllocate: Int = 0) {
            addPool(MotionComponent, preAllocate) { Motion() }
        }
    }

    // Create a new instance of the component from the pool
    override fun World.clone(): Motion =
    getPoolable(MotionComponent).apply { init(from = this@Motion ) }

    // Initialize the component automatically when it is added to an entity
    override fun World.initComponent(entity: Entity) {
    }

    // Cleanup the component automatically when it is removed from an entity
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }
}
