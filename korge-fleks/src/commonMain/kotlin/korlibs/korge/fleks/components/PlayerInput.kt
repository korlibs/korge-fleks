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
@Serializable @SerialName("PlayerInput")
class PlayerInput private constructor(
    var speed: Float = 0.03f,
    var xMoveStrength: Float = 0f,
    var yMoveStrength: Float = 0f
) : Poolable<PlayerInput>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: PlayerInput) {
        speed = from.speed
        xMoveStrength = from.xMoveStrength
        yMoveStrength = from.yMoveStrength
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        speed = 0.03f
        xMoveStrength = 0f
        yMoveStrength = 0f
    }

    override fun type() = PlayerInputComponent

    companion object {
        val PlayerInputComponent = componentTypeOf<PlayerInput>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticPlayerInputComponent(config: PlayerInput.() -> Unit ): PlayerInput =
            PlayerInput().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun World.PlayerInputComponent(config: PlayerInput.() -> Unit ): PlayerInput =
            getPoolable(PlayerInputComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addPlayerInputComponentPool(preAllocate: Int = 0) {
            addPool(PlayerInputComponent, preAllocate) { PlayerInput() }
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): PlayerInput =
        getPoolable(PlayerInputComponent).apply { init(from = this@PlayerInput ) }

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
}
