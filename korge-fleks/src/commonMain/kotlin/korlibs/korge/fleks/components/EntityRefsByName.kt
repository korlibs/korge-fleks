package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to link one entity to multiple other entities by name.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("EntityRefsByName")
class EntityRefsByName private constructor(
    val entitiesByName: MutableMap<String, Entity> = mutableMapOf(),
    // Configure what to do with the linked entities
    var moveWith: Boolean = false
) : PoolableComponent<EntityRefsByName>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: EntityRefsByName) {
        entitiesByName.putAll(from.entitiesByName)
        moveWith = from.moveWith
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        entitiesByName.clear()
        moveWith = false
    }

    override fun type() = EntityRefsByNameComponent

    companion object {
        val EntityRefsByNameComponent = componentTypeOf<EntityRefsByName>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticEntityRefsByNameComponent(config: EntityRefsByName.() -> Unit ): EntityRefsByName =
            EntityRefsByName().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun entityRefsByNameComponent(config: EntityRefsByName.() -> Unit ): EntityRefsByName =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "EntityRefsByName") { EntityRefsByName() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): EntityRefsByName = entityRefsByNameComponent { init(from = this@EntityRefsByName ) }

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
