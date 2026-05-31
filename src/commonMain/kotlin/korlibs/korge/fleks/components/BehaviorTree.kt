package korlibs.korge.fleks.components


import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to store the name of the behavior tree configuration file for a game object and the name
 * of the behavior tree configuration file that should be used after the collision system has run.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("BehaviorTree")
class BehaviorTree private constructor(
    var characterConfig: String = "",
    var configAfterCollisionSystem: String = ""
) : PoolableComponent<BehaviorTree>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are a value property of another component
    fun init(from: BehaviorTree) {
        characterConfig = from.characterConfig
        configAfterCollisionSystem = from.configAfterCollisionSystem
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are a value property of another component
    fun cleanup() {
        characterConfig = ""
        configAfterCollisionSystem = ""
    }

    override fun type() = BehaviorTreeComponent

    companion object {
        val BehaviorTreeComponent = componentTypeOf<BehaviorTree>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticBehaviorTreeComponent(config: BehaviorTree.() -> Unit): BehaviorTree =
            BehaviorTree().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun behaviorTreeComponent(config: BehaviorTree.() -> Unit): BehaviorTree =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "BehaviorTree") { BehaviorTree() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): BehaviorTree = behaviorTreeComponent { init(from = this@BehaviorTree) }

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