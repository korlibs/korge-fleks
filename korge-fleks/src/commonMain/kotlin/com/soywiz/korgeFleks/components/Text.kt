package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Text")
data class Text(
    var text: String = "",
    var fontName: String = ""
) : Component<Text> {
    override fun type(): ComponentType<Text> = Text
    companion object : ComponentType<Text>()
}

@Serializable
@SerialName("MultiLineText")
data class MultiLineText(
    var textLines: List<Entity> = emptyList()
) : Component<MultiLineText> {
    override fun type(): ComponentType<MultiLineText> = MultiLineText
    companion object : ComponentType<MultiLineText>()
}
