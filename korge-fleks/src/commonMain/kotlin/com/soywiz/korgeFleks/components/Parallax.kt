package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Parallax")
data class Parallax(
    var assetName: String = "",
    var layerNames: List<String> = listOf(),  // List of layer names which shall be used from the Aseprite file
    var disableScrollingX: Boolean = false,
    var disableScrollingY: Boolean = false
) : Component<Parallax> {
    override fun type(): ComponentType<Parallax> = Parallax
    companion object : ComponentType<Parallax>()
}

/**
 * This component is used in the parallax layer entities during the intro to animate the
 * layer movement and opacity independently.
 */
@Serializable
@SerialName("ParallaxLayer")
data class ParallaxLayer(
    var layerName: String = ""
) : Component<ParallaxLayer> {
    override fun type(): ComponentType<ParallaxLayer> = ParallaxLayer
    companion object : ComponentType<ParallaxLayer>()
}

/**
 * A component to identify the parallax background when used in the intro.
 */
@Serializable
@SerialName("Parallax.ParallaxIntro")
class ParallaxIntro : Component<ParallaxIntro> {
    override fun type(): ComponentType<ParallaxIntro> = ParallaxIntro
    companion object : ComponentType<ParallaxIntro>()
}