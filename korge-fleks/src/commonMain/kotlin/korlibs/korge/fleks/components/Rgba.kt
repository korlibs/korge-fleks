package korlibs.korge.fleks.components


import com.github.quillraven.fleks.*
import korlibs.image.color.*
import korlibs.korge.fleks.components.data.Rgb
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This component is used to add color information to an entity.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("Rgba")
class Rgba private constructor(
//    @Serializable(with = RGBAAsString::class)  -- deserialization does not work with string???
    @Serializable(with = RGBAAsInt::class) var rgba: RGBA = Colors.WHITE
) : Poolable<Rgba>() {
    override fun type() = RgbaComponent

    companion object {
        val RgbaComponent = componentTypeOf<Rgba>()

        fun World.RgbaComponent(config: Rgba.() -> Unit ): Rgba =
            getPoolable(RgbaComponent).apply { config() }

        fun staticRgbaComponent(): Rgba = Rgba()

        fun InjectableConfiguration.addRgbaComponentPool(preAllocate: Int = 0) {
            addPool(RgbaComponent, preAllocate) { Rgba() }
        }
    }

    override fun World.clone(): Rgba =
        getPoolable(RgbaComponent).apply {
            // Perform deep copy
            rgba = rgba.cloneRgba()
        }

    override fun World.cleanupComponent(entity: Entity) { cleanup()}

    fun init(from: Rgba) {
        // Perform deep copy
        rgba = from.rgba.cloneRgba()
    }

    fun cleanup() {
        rgba = Colors.WHITE
    }

    var alpha: Float = rgba.af
        get() = rgba.af
        set(value) {
            rgba = rgba.withAf(value)
            field = value
        }

// TODO: Cleanup
//    val tint: Rgb
//        get() = Rgb(rgba.r, rgba.g, rgba.b)

    fun setTint(value: Rgb) {
            rgba = rgba.withRGB(value.r, value.g, value.b)
        }

    override fun toString(): String = "RgbaComponent(rgba=${rgba.hexString})"
}
