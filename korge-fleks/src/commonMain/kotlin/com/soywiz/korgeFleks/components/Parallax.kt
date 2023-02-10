package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.soywiz.korgeFleks.utils.SerializeBase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This component is setting up the parallax background entity with its sub-entities.
 * It is used in the hook function for DrawableFamily.
 */
@Serializable
@SerialName("Parallax")
data class Parallax(
    var assetName: String = ""
) : Component<Parallax>, SerializeBase {
    override fun type(): ComponentType<Parallax> = Parallax
    companion object : ComponentType<Parallax>()
}
