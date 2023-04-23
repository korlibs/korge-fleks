package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import korlibs.korge.fleks.utils.SerializeBase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * The [Sprite] component adds visible details to an [Drawable] entity. By adding [Sprite] to an entity the entity will be
 * able to handle animations.
 */
@Serializable
@SerialName("Sprite")
data class Sprite(
    var assetName: String = "",
    var animationName: String? = null,

    var isPlaying: Boolean = false,
    var forwardDirection: Boolean = true,
    var loop: Boolean = false,
    var destroyOnPlayingFinished: Boolean = true,
) : Component<Sprite>, SerializeBase {
    override fun type(): ComponentType<Sprite> = Sprite
    companion object : ComponentType<Sprite>()
}
