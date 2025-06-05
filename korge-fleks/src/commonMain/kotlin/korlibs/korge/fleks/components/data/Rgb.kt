package korlibs.korge.fleks.components.data

import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * An RGB color data object which can be animated by the [TweenSequenceSystem].
 */
@Serializable @SerialName("Rgb")
class Rgb private constructor(
    var value: Int = 0xffffff
) : Poolable<Rgb> {
    // Init an existing data instance with data from another one
    override fun init(from: Rgb) {
        value = from.value
    }

    // Cleanup data instance manually
    // This is used for data instances when they are part (val property) of a component
    override fun cleanup() {
        value = 0xffffff
    }

    // Clone a new data instance from the pool
    override fun clone(): Rgb = rgb { init(from = this@Rgb ) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as val inside a component
        fun staticRgb(config: Rgb.() -> Unit ): Rgb =
            Rgb().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun rgb(config: Rgb.() -> Unit ): Rgb =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE) { Rgb() }

        val WHITE = Rgb(0xffffff)
        val RED = Rgb(0xff0000)
        val BLACK = Rgb(0x000000)
        val MIDDLE_GREY = Rgb(0x8f8f8f)
//        fun fromString(value: String): Rgb = Rgb(
//            value.substr(1,2).toInt(16),
//            value.substr(3,2).toInt(16),
//            value.substr(5,2).toInt(16))
   }

    operator fun plus(other: Rgb) = Rgb(value + other.value)
//    operator fun times(f: Float) = Rgb(
//        (r.toFloat() * f).roundToInt(),
//        (g.toFloat() * f).roundToInt(),
//        (b.toFloat() * f).roundToInt()
//    )

//    override fun toString(): String = "#%02x%02x%02x".format(r, g, b)

}
