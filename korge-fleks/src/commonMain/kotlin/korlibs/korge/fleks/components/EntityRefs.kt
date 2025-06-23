package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to link one entity to multiple other entities.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("EntityRefs")
class EntityRefs private constructor(
    val entities: MutableList<Entity> = mutableListOf(),
    // Configure what to do with the linked entities
    var moveWith: Boolean = true
) : PoolableComponent<EntityRefs>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: EntityRefs) {
        entities.addAll(from.entities)
        moveWith = from.moveWith
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        entities.clear()
        moveWith = true
    }

    override fun type() = EntityRefsComponent

    companion object {
        val EntityRefsComponent = componentTypeOf<EntityRefs>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticEntityRefsComponent(config: EntityRefs.() -> Unit ): EntityRefs =
            EntityRefs().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun entityRefsComponent(config: EntityRefs.() -> Unit ): EntityRefs =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "EntityRefs") { EntityRefs() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): EntityRefs = entityRefsComponent { init(from = this@EntityRefs ) }

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

    fun addAll(vararg newEntities: Entity) {
        entities.addAll(newEntities)
    }
}
