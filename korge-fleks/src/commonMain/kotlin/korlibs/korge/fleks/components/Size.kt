package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to add a size to a game object.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("Size")
class Size private constructor(
    var width: Float = 0f,
    var height: Float = 0f,
) : Poolable<Size>() {
    override fun type() = SizeComponent

    companion object {
        val SizeComponent = componentTypeOf<Size>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticSizeComponent(config: Size.() -> Unit ): Size =
            Size().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun World.SizeComponent(config: Size.() -> Unit ): Size =
        getPoolable(SizeComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addSizeComponentPool(preAllocate: Int = 0) {
            addPool(SizeComponent, preAllocate) { Size() }
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): Size =
    getPoolable(SizeComponent).apply { init(from = this@Size ) }

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

    // Cleanup/Reset the component automatically when it is removed from an entity
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }
}

/**
 * This component is used to add a size (as integer numbers) to a game object.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("SizeInt")
class SizeInt private constructor(
    var width: Int = 0,
    var height: Int = 0,
) : Poolable<SizeInt>() {
    override fun type() = SizeIntComponent

    companion object {
        val SizeIntComponent = componentTypeOf<SizeInt>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticSizeIntComponent(config: SizeInt.() -> Unit ): SizeInt =
            SizeInt().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun World.SizeIntComponent(config: SizeInt.() -> Unit ): SizeInt =
        getPoolable(SizeIntComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addSizeIntComponentPool(preAllocate: Int = 0) {
            addPool(SizeIntComponent, preAllocate) { SizeInt() }
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): SizeInt =
    getPoolable(SizeIntComponent).apply { init(from = this@SizeInt ) }

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

    // Cleanup/Reset the component automatically when it is removed from an entity
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }
}
