package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * The [SpriteComponent] component adds visible details to an [DrawableComponent] entity. By adding [SpriteComponent] to an entity the entity will be
 * able to handle animations.
 */
@Serializable
@SerialName("Sprite")
data class SpriteComponent(
    var assetName: String = "",
    var animationName: String? = null,

    var isPlaying: Boolean = false,
    var forwardDirection: Boolean = true,
    var loop: Boolean = false,
    var destroyOnPlayingFinished: Boolean = true,
) : Component<SpriteComponent> {
    override fun type(): ComponentType<SpriteComponent> = SpriteComponent
    companion object : ComponentType<SpriteComponent>()
}
