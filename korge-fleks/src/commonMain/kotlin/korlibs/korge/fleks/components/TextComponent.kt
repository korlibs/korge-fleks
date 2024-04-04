package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component contains [text] which can be drawn with [fontName] with the DialogRenderView.
 */
@Serializable
@SerialName("Text")
data class TextComponent(
    var text: String = "",
    var fontName: String = ""
) : Component<TextComponent> {
    override fun type(): ComponentType<TextComponent> = TextComponent
    companion object : ComponentType<TextComponent>()
}
