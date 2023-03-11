package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Currently this is used to get the layout details for an Entity from the asset object.
 * The asset object is configured from GameModel.
 */
@Serializable
@SerialName("Layout")
data class Layout(
    var centerX: Boolean = false,
    var centerY: Boolean = false,
    var offsetX: Double = 0.0,
    var offsetY: Double = 0.0
) : Component<Layout> {
    override fun type(): ComponentType<Layout> = Layout
    companion object : ComponentType<Layout>()
}
