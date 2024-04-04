package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import korlibs.image.color.*
import korlibs.io.lang.*
import korlibs.korge.fleks.utils.*
import korlibs.util.*
import kotlinx.serialization.*
import kotlin.math.*


/**
 * The [SpriteComponent] component adds visible details to an [DrawableComponent] entity. By adding [SpriteComponent] to an entity the entity will be
 * able to handle animations.
 *
 * @param [anchorX] X offset of the sprite graphic to the zero-point of the sprite (pivot-point).
 * @param [anchorY] Y offset of the sprite graphic to the zero-point of the sprite (pivot-point).
 * @param [alpha] is used to control the alpha channel of the sprite.
 * @param [tint] can be used to tint the sprite with a specific RGB color.
 */
@Serializable
@SerialName("Sprite")
data class SpriteComponent(
    var assetName: String = "",
    var animationName: String? = null,

    var anchorX: Float = 0f,  // x,y position of the pivot point within the sprite
    var anchorY: Float = 0f,

    var alpha: Int = 255,  // Range: 0..255 (0..0xff)
    var tint: Rgb = Rgb.WHITE,

    // Animation related properties
    var isPlaying: Boolean = false,
    var forwardDirection: Boolean = true,
    var loop: Boolean = false,
    var destroyOnPlayingFinished: Boolean = true,
) : Component<SpriteComponent> {
    override fun type(): ComponentType<SpriteComponent> = SpriteComponent

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
            val BLACK = Rgb(0x00, 0x00, 0x00)
            val MIDDLE_GREY = Rgb(0x8f, 0x8f, 0x8f)
            fun fromString(value: String): Rgb = Rgb(
                value.substr(1,2).toInt(16),
                value.substr(3,2).toInt(16),
                value.substr(5,2).toInt(16))
        }
    }
    companion object : ComponentType<SpriteComponent>()
}
