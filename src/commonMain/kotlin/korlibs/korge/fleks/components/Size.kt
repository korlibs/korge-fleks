package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to add a size to a game object.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("Size")
class Size private constructor(
    var width: Float = 0f,
    var height: Float = 0f,
) : PoolableComponent<Size>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: Size) {
        width = from.width
        height = from.height
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        width = 0f
        height = 0f
    }

    override fun type() = SizeComponent

    companion object {
        val SizeComponent = componentTypeOf<Size>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticSizeComponent(config: Size.() -> Unit): Size =
            Size().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun sizeComponent(config: Size.() -> Unit): Size =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "Size") { Size() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): Size = sizeComponent { init(from = this@Size) }

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


/**
 * This component is used to add a size (as integer numbers) to a game object.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("SizeInt")
class SizeInt private constructor(
    var width: Int = 0,
    var height: Int = 0,
) : PoolableComponent<SizeInt>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: SizeInt) {
        width = from.width
        height = from.height
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        width = 0
        height = 0
    }

    override fun type() = SizeIntComponent

    companion object {
        val SizeIntComponent = componentTypeOf<SizeInt>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticSizeIntComponent(config: SizeInt.() -> Unit): SizeInt =
        SizeInt().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun sizeIntComponent(config: SizeInt.() -> Unit): SizeInt =
        pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "SizeInt") { SizeInt() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): SizeInt = sizeIntComponent { init(from = this@SizeInt) }

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
