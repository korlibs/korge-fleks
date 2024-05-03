package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import korlibs.image.text.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


/**
 * This component contains [text] which can be drawn with [fontName] with the DialogRenderView.
 */
@Serializable
@SerialName("Text")
data class TextComponent(
    var text: String = "",
    var fontName: String = "",

    var textRangeStart: Int = 0,
    var textRangeEnd: Int = Int.MAX_VALUE,

    // size of text bounds
    var width: Float = 0f,
    var height: Float = 0f,
    var wordWrap: Boolean = true,
    @Serializable(with = HorizontalAlignAsDouble::class) var horizontalAlign: HorizontalAlign = HorizontalAlign.LEFT,
    @Serializable(with = VerticalAlignAsDouble::class) var verticalAlign: VerticalAlign = VerticalAlign.TOP
) : Component<TextComponent> {
    override fun type(): ComponentType<TextComponent> = TextComponent
    companion object : ComponentType<TextComponent>()
}
