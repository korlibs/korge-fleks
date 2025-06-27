package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to add color information to an entity.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("Rgba")
class Rgba private constructor(
//    @Serializable(with = RGBAAsString::class)  -- deserialization does not work with string???
    @Serializable(with = RGBAAsInt::class) var rgba: RGBA = Colors.WHITE
) : PoolableComponent<Rgba>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: Rgba) {
        // Perform deep copy
        rgba = from.rgba.cloneRgba()
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        rgba = Colors.WHITE
    }

    override fun type() = RgbaComponent

    companion object {
        val RgbaComponent = componentTypeOf<Rgba>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticRgbaComponent(config: Rgba.() -> Unit ): Rgba =
            Rgba().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun rgbaComponent(config: Rgba.() -> Unit ): Rgba =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "Rgba") { Rgba() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): Rgba = rgbaComponent { init(from = this@Rgba ) }

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

    var r: Int
        get() = rgba.r
        set(value) {
            rgba = rgba.withR(value)
        }

    var g: Int
        get() = rgba.g
        set(value) {
            rgba = rgba.withG(value)
        }

    var b: Int
        get() = rgba.b
        set(value) {
            rgba = rgba.withB(value)
        }

    var rgb: Int
        get() = rgba.rgb
        set(value) {
            rgba = rgba.withRGB(value)
        }

    var alpha: Float
        get() = rgba.af
        set(value) {
            rgba = rgba.withAf(value)
        }

    override fun toString(): String = "RgbaComponent(rgba=${rgba.hexString})"
}
