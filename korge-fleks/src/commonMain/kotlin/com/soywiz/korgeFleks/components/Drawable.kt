package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.soywiz.korgeFleks.entity.config.nullEntity
import com.soywiz.korio.lang.format
import com.soywiz.korio.lang.substr
import com.soywiz.krypto.encoding.fromHex
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

/**
 * The [Drawable] and [Appearance] components are used to specify that an entity is visible on the display.
 * I.e. it is drawn by KorGe to the Scene.
 *
 * @param [drawOnLayer] The number of the layer in the KorGe view scene where the entity is placed.
 *
 */
@Serializable
@SerialName("Drawable")
data class Drawable(
    var drawOnLayer: String = ""
) : Component<Drawable> {
    override fun type(): ComponentType<Drawable> = Drawable
    companion object : ComponentType<Drawable>()
}

@Serializable
@SerialName("Appearance")
data class Appearance(
    var alpha: Double = 1.0,
    var visible: Boolean = true,
    var tint: Rgb? = null
) : Component<Appearance> {
    override fun type(): ComponentType<Appearance> = Appearance
    companion object : ComponentType<Appearance>()
}

/**
 * This component adds the control-specific-layer aspect to the entity.
 * I.e. when this component is added to an entity than that entity will control e.g. [Appearance] or [PositionShape]
 * aspects of a specific layer of a sprite.

 * Hint: Usually [PositionShape] and [Offset] components are also added to that entity in order to change the layer
 * position relatively to the [Sprite] position or pivot point.
 *
 * @param [spriteLayer] has to be set to the same layer name as in Aseprite to select that layer.
 * @param [parentEntity] is the entity (ID) which defines [Sprite] data.
 */
@Serializable
@SerialName("SpecificLayer")
data class SpecificLayer(
    var spriteLayer: String = "",
    var parentEntity: Entity = nullEntity  // The entity which contains the sprite data with layers (ImageAnimView)
) : Component<SpecificLayer> {
    override fun type(): ComponentType<SpecificLayer> = SpecificLayer
    companion object : ComponentType<SpecificLayer>()
}

@Serializable
@SerialName("Appearance.Rgb")
data class Rgb(
    var r: Int = 0xff,
    var g: Int = 0xff,
    var b: Int = 0xff
) {
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

/**
 * This component is used to switch [visible][Appearance.visible] property of [Appearance] component.
 */
@Serializable
@SerialName("SwitchLayerVisibility")
data class SwitchLayerVisibility(
    var offVariance: Double = 0.0,  // variance in switching value off: (1.0) - every frame switching possible, (0.0) - no switching at all
    var onVariance: Double = 0.0,   // variance in switching value on again: (1.0) - changed value switches back immediately, (0.0) - changed value stays forever
    var spriteLayers: List<LayerVisibility> = listOf()
) : Component<SwitchLayerVisibility> {
    override fun type(): ComponentType<SwitchLayerVisibility> = SwitchLayerVisibility
    companion object : ComponentType<SwitchLayerVisibility>()
}

@Serializable
@SerialName("SwitchLayerVisibility.LayerVisibility")
data class LayerVisibility(
    var name: String = "",
    var visible: Boolean = true
)
