package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * The [Sprite] component adds visible details to an [Drawable] entity. By adding [Sprite] to an entity the entity will be
 * able to handle animations.
 */
data class Sprite(
    var assetName: String = "",
    var animationName: String? = null,

    var isPlaying: Boolean = false,
    var forwardDirection: Boolean = true,
    var loop: Boolean = false,
    var destroyOnPlayingFinished: Boolean = true,
) : Component<Sprite> {
    override fun type(): ComponentType<Sprite> = Sprite
    companion object : ComponentType<Sprite>()
}
