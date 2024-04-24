package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.image.color.*
import korlibs.io.lang.*
import korlibs.korge.fleks.utils.*
import korlibs.util.*
import kotlinx.serialization.*
import kotlin.math.*

@Serializable
@SerialName("Rgba")
class RgbaComponent(
    @Serializable(with = RGBAAsString::class)
    var rgba: RGBA = Colors.WHITE,
) : Component<RgbaComponent> {

    var alpha: Float = rgba.af
        get() = rgba.af
        set(value) {
            rgba = rgba.withAf(value)
            field = value
        }

    var rgb: Rgb = Rgb(rgba.r, rgba.g, rgba.b)
        get() = Rgb(rgba.r, rgba.g, rgba.b)
        set(value) {
            rgba = rgba.withRGB(value.r, value.g, value.b)
            field = value
        }

    @Serializable
    @SerialName("Rgb")
    data class Rgb(
        var r: Int = 0xff,
        var g: Int = 0xff,
        var b: Int = 0xff
    ) : SerializeBase {
        operator fun plus(other: Rgb) = Rgb(r + other.r, g + other.g, b + other.b)
        operator fun times(f: Double) = Rgb(
            (r.toDouble() * f).roundToInt(),
            (g.toDouble() * f).roundToInt(),
            (b.toDouble() * f).roundToInt()
        )

        override fun toString(): String = "#%02x%02x%02x".format(r, g, b)

        companion object {
            val WHITE = Rgb(0xff, 0xff, 0xff)
            val RED = Rgb(0xff, 0x00, 0x00)
            val BLACK = Rgb(0x00, 0x00, 0x00)
            val MIDDLE_GREY = Rgb(0x8f, 0x8f, 0x8f)
            fun fromString(value: String): Rgb = Rgb(
                value.substr(1,2).toInt(16),
                value.substr(3,2).toInt(16),
                value.substr(5,2).toInt(16))
        }
    }

    override fun type(): ComponentType<RgbaComponent> = RgbaComponent
    operator fun component1(): RGBA = rgba

    companion object : ComponentType<RgbaComponent>()
}
