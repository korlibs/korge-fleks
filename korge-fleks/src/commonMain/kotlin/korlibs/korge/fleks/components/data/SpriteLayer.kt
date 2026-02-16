package korlibs.korge.fleks.components.data

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This class is used to reference a sprite layer and being able to animate the layer position offset and layer color properties.
 */
@Serializable @SerialName("SpriteLayer")
class SpriteLayer private constructor(
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
    @Serializable(with = RGBAAsInt::class) var rgba: RGBA = Colors.WHITE
) : Poolable<SpriteLayer> {
    // Init an existing data instance with data from another one
    override fun init(from: SpriteLayer) {
        offsetX = from.offsetX
        offsetY = from.offsetY
        rgba = from.rgba.cloneRgba()
    }

    // Cleanup data instance manually
    // This is used for data instances when they are part (val property) of a component
    override fun cleanup() {
        offsetX = 0f
        offsetY = 0f
        rgba = Colors.WHITE
    }

    // Clone a new data instance from the pool
    override fun clone(): SpriteLayer = spriteLayer { init(from = this@SpriteLayer) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as val inside a component
        fun staticSpriteLayer(config: SpriteLayer.() -> Unit): SpriteLayer =
            SpriteLayer().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun spriteLayer(config: SpriteLayer.() -> Unit): SpriteLayer =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "SpriteLayer") { SpriteLayer() }

        // Init and cleanup functions for collections of SpriteLayer
        fun MutableMap<String, SpriteLayer>.init(from: Map<String, SpriteLayer>) {
            from.forEach { (key, item) ->
                this[key] = item.clone()
            }
        }

        fun MutableMap<String, SpriteLayer>.freeAndClear() {
            this.forEach { (_, item) ->
                item.cleanup()
                item.free()
            }
            this.clear()
        }
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

    // true (alpha > 0) - false (alpha == 0)
    var visibility: Boolean = rgba.a != 0
        get() = rgba.a != 0
        set(value) {
            rgba = if (value) rgba.withAf(1f) else rgba.withAf(0f)
            field = value
        }
}
