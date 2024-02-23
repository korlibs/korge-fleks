package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import korlibs.korge.fleks.entity.config.invalidEntity
import korlibs.korge.fleks.utils.SerializeBase
import korlibs.io.lang.substr
import korlibs.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt


/**
 * The [DrawableComponent] component is used to specify that an entity is visible on the display.
 * I.e. it is drawn by KorGe to the Scene.
 * This component is used only in family hook DrawableFamily to set up the view data.
 *
 * @param [drawOnLayer] The number of the layer in the KorGe view scene where the entity is placed.
 *
 */
@Serializable
@SerialName("Drawable")
data class DrawableComponent(
    var drawOnLayer: String = ""
) : Component<DrawableComponent> {
    override fun type() = DrawableComponent
    companion object : ComponentType<DrawableComponent>()
}

/**
 * The [AppearanceComponent] component is used to specify that an entity is visible on the display.
 * This component directly controls the KorgeViewSystem by giving visibility aspects to the entity.
 *
 * @param [alpha] is used to control the alpha channel of the sprite image data.
 * @param [visible] controls visibility (on/off)
 * @param [tint] can be used optionally to tint the sprite with a specific RGB color.
 *
 */
@Serializable
@SerialName("Appearance")
data class AppearanceComponent(
    var alpha: Float = 1.0f,
    var visible: Boolean = true,
    var tint: Rgb? = null
) : Component<AppearanceComponent> {
    override fun type() = AppearanceComponent
    companion object : ComponentType<AppearanceComponent>()
}

/**
 * This component adds the control-specific-layer aspect to the entity.
 * I.e. when this component is added to an entity than that entity will control e.g. [AppearanceComponent], [PositionShapeComponent] or
 * [InputTouchButtonComponent] aspects of a specific layer of a sprite.
 *
 * Hint: Usually [PositionShapeComponent] and [OffsetComponent] components are also added to that entity in order to change the layer
 * position relatively to the [SpriteComponent] position or pivot point.
 *
 * @param [spriteLayer] has to be set to the same layer name as in Aseprite to select that layer.
 * @param [parentEntity] is the entity (ID) which defines [SpriteComponent] data.
 */
@Serializable
@SerialName("SpecificLayer")
data class SpecificLayerComponent(
    var parentEntity: Entity = invalidEntity,  // The entity which contains the sprite data with layers (ImageAnimView)
    var spriteLayer: String? = null,
    var parallaxPlaneLine: Int? = null
) : Component<SpecificLayerComponent> {
    override fun type() = SpecificLayerComponent
    companion object : ComponentType<SpecificLayerComponent>()
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
 * This component is used to switch [visible][AppearanceComponent.visible] property of [AppearanceComponent] component.
 */
@Serializable
@SerialName("SwitchLayerVisibility")
data class SwitchLayerVisibilityComponent(
    var offVariance: Float = 0.0f,  // variance in switching value off: (1.0) - every frame switching possible, (0.0) - no switching at all
    var onVariance: Float = 0.0f,   // variance in switching value on again: (1.0) - changed value switches back immediately, (0.0) - changed value stays forever
    var spriteLayers: List<LayerVisibility> = listOf()
) : Component<SwitchLayerVisibilityComponent> {
    override fun type() = SwitchLayerVisibilityComponent
    companion object : ComponentType<SwitchLayerVisibilityComponent>()
}

@Serializable
@SerialName("SwitchLayerVisibility.LayerVisibility")
data class LayerVisibility(
    var name: String = "",
    var visible: Boolean = true
) : SerializeBase
