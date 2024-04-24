package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import korlibs.image.color.*
import korlibs.io.lang.*
import korlibs.korge.fleks.utils.*
import korlibs.util.*
import kotlinx.serialization.*
import kotlin.math.*


/**
 * The [SpriteComponent] component adds visible details to an [DrawableComponent] entity. By adding [SpriteComponent] to an entity the entity will be
 * able to handle animations.
 *
 * @param [anchorX] X offset of the sprite graphic to the zero-point of the sprite (pivot-point).
 * @param [anchorY] Y offset of the sprite graphic to the zero-point of the sprite (pivot-point).
 * @param [alpha] is used to control the alpha channel of the sprite.
 * @param [tint] can be used to tint the sprite with a specific RGB color.
 */
@Serializable
@SerialName("Sprite")
data class SpriteComponent(
    var assetName: String = "",
    var animationName: String? = null,

    var anchorX: Float = 0f,  // x,y position of the pivot point within the sprite
    var anchorY: Float = 0f,

    val layerIndex: Int = 0
) : Component<SpriteComponent> {
    override fun type(): ComponentType<SpriteComponent> = SpriteComponent
    companion object : ComponentType<SpriteComponent>()
}
