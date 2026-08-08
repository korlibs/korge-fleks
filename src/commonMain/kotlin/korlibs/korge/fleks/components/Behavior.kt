package korlibs.korge.fleks.components


import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to store the name of the behavior configuration object for a game object.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("Behavior")
class Behavior private constructor(
    var name: String = "",
    var state: Int = 0
) : PoolableComponent<Behavior>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are a value property of another component
    fun init(from: Behavior) {
        name = from.name
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are a value property of another component
    fun cleanup() {
        name = ""
    }

    override fun type() = BehaviorComponent

    companion object {
        val BehaviorComponent = componentTypeOf<Behavior>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticBehaviorComponent(config: Behavior.() -> Unit): Behavior =
            Behavior().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun behaviorComponent(config: Behavior.() -> Unit): Behavior =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "Behavior") { Behavior() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): Behavior = behaviorComponent { init(from = this@Behavior) }

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