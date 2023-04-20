package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import korlibs.korge.fleks.entity.config.nullEntity
import korlibs.korge.fleks.utils.SerializeBase
import korlibs.io.lang.format
import korlibs.io.lang.substr
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt


/**
 * The [Drawable] component is used to specify that an entity is visible on the display.
 * I.e. it is drawn by KorGe to the Scene.
 * This component is used only in family hook DrawableFamily to set up the view data.
 *
 * @param [drawOnLayer] The number of the layer in the KorGe view scene where the entity is placed.
 *
 */
@Serializable
@SerialName("Drawable")
data class Drawable(
    var drawOnLayer: String = ""
) : Component<Drawable>, SerializeBase {
    override fun type(): ComponentType<Drawable> = Drawable
    companion object : ComponentType<Drawable>()
}

/**
 * The [Appearance] component is used to specify that an entity is visible on the display.
 * This component directly controls the KorgeViewSystem by giving visibility aspects to the entity.
 *
 * @param [alpha] is used to control the alpha channel of the sprite image data.
 * @param [visible] controls visibility (on/off)
 * @param [tint] can be used optionally to tint the sprite with a specific RGB color.
 *
 */
@Serializable
@SerialName("Appearance")
data class Appearance(
    var alpha: Float = 1.0f,
    var visible: Boolean = true,
    var tint: Rgb? = null
) : Component<Appearance>, SerializeBase {
    override fun type(): ComponentType<Appearance> = Appearance
    companion object : ComponentType<Appearance>()
}

/**
 * This component adds the control-specific-layer aspect to the entity.
 * I.e. when this component is added to an entity than that entity will control e.g. [Appearance], [PositionShape] or
 * [InputTouchButton] aspects of a specific layer of a sprite.
 *
 * Hint: Usually [PositionShape] and [Offset] components are also added to that entity in order to change the layer
 * position relatively to the [Sprite] position or pivot point.
 *
 * @param [spriteLayer] has to be set to the same layer name as in Aseprite to select that layer.
 * @param [parentEntity] is the entity (ID) which defines [Sprite] data.
 */
@Serializable
@SerialName("SpecificLayer")
data class SpecificLayer(
    var parentEntity: Entity = nullEntity,  // The entity which contains the sprite data with layers (ImageAnimView)
    var spriteLayer: String? = null,
    var parallaxPlaneLine: Int? = null
) : Component<SpecificLayer>, SerializeBase {
    override fun type(): ComponentType<SpecificLayer> = SpecificLayer
    companion object : ComponentType<SpecificLayer>()
}

@Serializable
@SerialName("Appearance.Rgb")
data class Rgb(
    var r: Int = 0xff,
    var g: Int = 0xff,
    var b: Int = 0xff
) : SerializeBase {
    operator fun plus(other: Rgb) = Rgb(r + other.r, g + other.g, b + other.b)
    operator fun times(f: Float) = Rgb(
        (r.toFloat() * f).roundToInt(),
        (g.toFloat() * f).roundToInt(),
        (b.toFloat() * f).roundToInt()
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

/**
 * This component is used to switch [visible][Appearance.visible] property of [Appearance] component.
 */
@Serializable
@SerialName("SwitchLayerVisibility")
data class SwitchLayerVisibility(
    var offVariance: Float = 0.0f,  // variance in switching value off: (1.0) - every frame switching possible, (0.0) - no switching at all
    var onVariance: Float = 0.0f,   // variance in switching value on again: (1.0) - changed value switches back immediately, (0.0) - changed value stays forever
    var spriteLayers: List<LayerVisibility> = listOf()
) : Component<SwitchLayerVisibility>, SerializeBase {
    override fun type(): ComponentType<SwitchLayerVisibility> = SwitchLayerVisibility
    companion object : ComponentType<SwitchLayerVisibility>()
}

@Serializable
@SerialName("SwitchLayerVisibility.LayerVisibility")
data class LayerVisibility(
    var name: String = "",
    var visible: Boolean = true
) : SerializeBase
