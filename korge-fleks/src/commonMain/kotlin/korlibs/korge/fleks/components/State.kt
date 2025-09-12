package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.data.StateType
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to store a state value, which can be used for various purposes,
 * such as game settings, player preferences, or any other data that needs to be stored
 * and accessed throughout the game.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("State")
class State private constructor(
    var name: String = "",  // Name of the game object - currently we do not save it anywhere else
    var current: StateType = StateType.ILLEGAL,
    var last: StateType = StateType.ILLEGAL,

    var direction: Int = Geometry.RIGHT_DIRECTION,
    var resetAnimFrameCounter: Boolean = false
) : PoolableComponent<State>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are a value property of another component
    fun init(from: State) {
        name = from.name
        current = from.current
        last = from.last
        direction = from.direction
        resetAnimFrameCounter = from.resetAnimFrameCounter
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are a value property of another component
    fun cleanup() {
        name = ""
        current = StateType.ILLEGAL
        last = StateType.ILLEGAL
        direction = Geometry.RIGHT_DIRECTION
        resetAnimFrameCounter = false
    }

    override fun type() = StateComponent

    companion object {
        val StateComponent = componentTypeOf<State>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticStateComponent(config: State.() -> Unit ): State =
            State().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun stateComponent(config: State.() -> Unit ): State =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "State") { State() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): State = stateComponent { init(from = this@State ) }

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
