package korlibs.korge.fleks.components.data

import com.github.quillraven.fleks.*
import korlibs.io.lang.*
import korlibs.korge.fleks.utils.*
import korlibs.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.*
import korlibs.korge.fleks.systems.TweenSequenceSystem


/**
 * An RGB color data object which can be animated by the [TweenSequenceSystem].
 */
@Serializable @SerialName("Rgb")
class Rgb private constructor(
    var r: Int = 0xff,
    var g: Int = 0xff,
    var b: Int = 0xff
) : Poolable<Rgb>() {
    override fun type() = RgbData

    companion object {
        val RgbData= componentTypeOf<Rgb>()

        // Use this function to create a new instance as val inside a component
        fun value(): Rgb = Rgb()

        fun InjectableConfiguration.addRgbDataPool(preAllocate: Int = 0) {
            addPool(RgbData, preAllocate) { Rgb() }
        }

        val WHITE = Rgb(0xff, 0xff, 0xff)
        val RED = Rgb(0xff, 0x00, 0x00)
        val BLACK = Rgb(0x00, 0x00, 0x00)
        val MIDDLE_GREY = Rgb(0x8f, 0x8f, 0x8f)
        fun fromString(value: String): Rgb = Rgb(
            value.substr(1,2).toInt(16),
            value.substr(3,2).toInt(16),
            value.substr(5,2).toInt(16))
    }

    override fun World.clone(): Rgb =
        getPoolable(RgbData).apply { init(from = this@Rgb ) }

    fun init(from: Rgb) {
        r = from.r
        g = from.g
        b = from.b
    }

    fun cleanup() {
        r = 0xff
        g = 0xff
        b = 0xff
    }

    operator fun plus(other: Rgb) = Rgb(r + other.r, g + other.g, b + other.b)
    operator fun times(f: Float) = Rgb(
        (r.toFloat() * f).roundToInt(),
        (g.toFloat() * f).roundToInt(),
        (b.toFloat() * f).roundToInt()
    )

    override fun toString(): String = "#%02x%02x%02x".format(r, g, b)
}
