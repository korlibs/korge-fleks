package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import korlibs.korge.fleks.utils.SerializeBase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("Text")
data class TextComponent(
    var text: String = "",
    var fontName: String = ""
) : Component<TextComponent>, SerializeBase {
    override fun type(): ComponentType<TextComponent> = TextComponent
    companion object : ComponentType<TextComponent>()
}

@Serializable
@SerialName("MultiLineText")
data class MultiLineTextComponent(
    var textLines: List<Entity> = emptyList()
) : Component<MultiLineTextComponent>, SerializeBase {
    override fun type(): ComponentType<MultiLineTextComponent> = MultiLineTextComponent
    companion object : ComponentType<MultiLineTextComponent>()
}
