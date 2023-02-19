package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.soywiz.korio.serialization.json.Json

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
) : Component<Sprite>, Json.CustomSerializer {
    override fun type(): ComponentType<Sprite> = Sprite
    companion object : ComponentType<Sprite>()

    override fun encodeToJson(b: StringBuilder) {
        val animName = if (animationName == null) null else "\"$animationName\""
        b.append("""{"Sprite":{"assetName":"$assetName","animationName":$animName,"isPlaying":$isPlaying,""" +
                 """"forwardDirection":$forwardDirection,"loop":$loop,"destroyOnPlayingFinished":$destroyOnPlayingFinished}}""")
    }
}
