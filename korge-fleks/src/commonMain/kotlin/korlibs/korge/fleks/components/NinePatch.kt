package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to define a NinePatch texture for an entity.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("NinePatch")
class NinePatch private constructor(
    var name: String = "",
    var width: Float = 0f,
    var height: Float = 0f
) : PoolableComponent<NinePatch>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: NinePatch) {
        name = from.name
        width = from.width
        height = from.height
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        name = ""
        width = 0f
        height = 0f
    }

    override fun type() = NinePatchComponent

    companion object {
        val NinePatchComponent = componentTypeOf<NinePatch>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticNinePatchComponent(config: NinePatch.() -> Unit ): NinePatch =
        NinePatch().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun ninePatchComponent(config: NinePatch.() -> Unit ): NinePatch =
        pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE) { NinePatch() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): NinePatch = ninePatchComponent { init(from = this@NinePatch ) }

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
