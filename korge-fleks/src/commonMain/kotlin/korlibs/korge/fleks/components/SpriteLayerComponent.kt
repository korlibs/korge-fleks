package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import korlibs.image.color.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This component enable controlling of layer properties of a sprite texture.
 *
 * @param [layerMap] contains offset and rgba properies for specific layers of a sprite texture.
 */
@Serializable
@SerialName("SpecificLayer")
data class SpriteLayersComponent(
    var layerMap: Map<String, LayerProperties> = mapOf()
) : Component<SpriteLayersComponent> {

    @Serializable
    @SerialName("LayerVisibility")
    data class LayerProperties(
        var offsetX: Float = 0f,
        var offsetY: Float = 0f,
        @Serializable(with = RGBAAsInt::class)
        var rgba: RGBA = Colors.WHITE
    ) : SerializeBase {
        // true (alpha > 0) - false (alpha == 0)
        var visibility: Boolean = rgba.a != 0
            get() = rgba.a != 0
            set(value) {
                rgba = if (value) rgba.withAf(1f) else rgba.withAf(0f)
                field = value
            }
    }

    override fun type() = SpriteLayersComponent
    companion object : ComponentType<SpriteLayersComponent>()
}
