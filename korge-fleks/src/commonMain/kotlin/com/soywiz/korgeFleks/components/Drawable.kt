package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.soywiz.korgeFleks.entity.config.nullEntity
import com.soywiz.korio.serialization.json.Json.CustomSerializer
import kotlin.math.roundToInt

/**
 * The [Drawable] and [Appearance] components are used to specify that an entity is visible on the display.
 * I.e. it is drawn by KorGe to the Scene.
 *
 * @param [drawOnLayer] The number of the layer in the KorGe view scene where the entity is placed.
 *
 */
data class Drawable(
    var drawOnLayer: String = ""
) : Component<Drawable>, CustomSerializer {
    override fun type(): ComponentType<Drawable> = Drawable
    companion object : ComponentType<Drawable>()

    override fun encodeToJson(b: StringBuilder) {
        b.append("""{"Drawable":{"drawOnLayer":"$drawOnLayer"}}""")
    }
}

data class Appearance(
    var alpha: Double = 1.0,
    var visible: Boolean = true,
    var tint: Rgb? = null
) : Component<Appearance>, CustomSerializer {
    override fun type(): ComponentType<Appearance> = Appearance
    companion object : ComponentType<Appearance>()

    override fun encodeToJson(b: StringBuilder) {
        b.append("""{"Drawable":{"alpha":$alpha,"visible":$visible,"tint":$tint}}""")
    }
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
data class SpecificLayer(
    var spriteLayer: String = "",
    var parentEntity: Entity = nullEntity  // The entity which contains the sprite data with layers (ImageAnimView)
) : Component<SpecificLayer> {
    override fun type(): ComponentType<SpecificLayer> = SpecificLayer
    companion object : ComponentType<SpecificLayer>()
}

data class Rgb(
    var r: Int = 0xff,
    var g: Int = 0xff,
    var b: Int = 0xff
) : CustomSerializer {
    operator fun plus(other: Rgb) = Rgb(r + other.r, g + other.g, b + other.b)

    operator fun times(f: Double) = Rgb(
        (r.toDouble() * f).roundToInt(),
        (g.toDouble() * f).roundToInt(),
        (b.toDouble() * f).roundToInt()
    )

    companion object {
        val WHITE = Rgb(0xff, 0xff, 0xff)
        val BLACK = Rgb(0x00, 0x00, 0x00)
        val MIDDLE_GREY = Rgb(0x8f, 0x8f, 0x8f)
    }

    override fun encodeToJson(b: StringBuilder) {
        TODO("Not yet implemented")
    }
}

/**
 * This component is used to switch [visible][Appearance.visible] property of [Appearance] component.
 */
data class SwitchLayerVisibility(
    var offVariance: Double = 0.0,  // variance in switching value off: (1.0) - every frame switching possible, (0.0) - no switching at all
    var onVariance: Double = 0.0,   // variance in switching value on again: (1.0) - changed value switches back immediately, (0.0) - changed value stays forever
    var spriteLayers: List<LayerVisibility> = listOf()
) : Component<SwitchLayerVisibility>, CustomSerializer {
    override fun type(): ComponentType<SwitchLayerVisibility> = SwitchLayerVisibility
    companion object : ComponentType<SwitchLayerVisibility>()

    override fun encodeToJson(b: StringBuilder) { b.append(this) }
}

data class LayerVisibility(
    var name: String = "",
    var visible: Boolean = true
)
