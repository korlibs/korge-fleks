package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to add a name to an entity. Useful for making a save-game snapshot readable.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("Info")
class Info private constructor(
    //val num: Int = 0,
    var name: String = "noName",
    var entityId: Int = -1,
) : PoolableComponent<Info>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: Info) {
        name = this@Info.name
        entityId = this@Info.entityId
        //println("Cloned: Info '$num' from '${this@Info.num}'")
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        name = "noName"
        entityId = -1
        //println("Reset: Info '$num'")
    }

    override fun type() = InfoComponent

    companion object {
        val InfoComponent = componentTypeOf<Info>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticInfoComponent(config: Info.() -> Unit ): Info =
            Info().apply { config() /*; println("Static created: Info")*/ }

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun infoComponent(config: Info.() -> Unit ): Info =
            pool.alloc().apply { config() /*; println("Created: Info '$num'")*/ }

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "Info") { Info(/* num = it */) }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): Info = infoComponent { init(from = this@Info ) }

    // Initialize the component automatically when it is added to an entity
    override fun World.initComponent(entity: Entity) {
    }

    // Cleanup/Reset the component automatically when it is removed from an entity (component will be returned to the pool eventually)
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }

    // Initialize an external prefab when the component is added to an entity
    override fun World.initPrefabs(entity: Entity) {
        entityId = entity.id
        EntityByName.add(name, entity)
    }

    // Cleanup/Reset an external prefab when the component is removed from an entity
    override fun World.cleanupPrefabs(entity: Entity) {
        EntityByName.remove(name)
    }

    // Free the component and return it to the pool - this is called directly by the SnapshotSerializerSystem
    override fun free() {
        cleanup()
        pool.free(this)
    }
}
