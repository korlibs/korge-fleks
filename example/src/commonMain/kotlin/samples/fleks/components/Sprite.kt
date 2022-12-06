package samples.fleks.components

import com.github.quillraven.fleks.*
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.Image
import com.soywiz.korge.view.addTo
import com.soywiz.korge.view.animation.ImageAnimationView
import com.soywiz.korim.bitmap.Bitmaps
import com.soywiz.korim.format.ImageAnimation
import samples.fleks.assets.Assets

/**
 * The sprite component adds visible details to an entity. By adding sprite to an entity the entity will be
 * visible on the screen.
 */
data class Sprite(
    var imageData: String = "",
    var animation: String = "",
    var isPlaying: Boolean = false,
    var forwardDirection: Boolean = true,
    var loop: Boolean = false,
    // internal data
    var imageAnimView: ImageAnimationView<Image> = ImageAnimationView { Image(Bitmaps.transparent) }.apply { smoothing = false }
) : Component<Sprite> {
    override fun type(): ComponentType<Sprite> = Sprite
    companion object : ComponentType<Sprite>() {
        /**
         * Initialize internal waitTime property with delay value of first tweens if available.
         */
        val onComponentAdded: ComponentHook<Sprite> = { entity, component ->
            val layerContainer = inject<Container>("layer0")
             val assets = inject<Assets>()

            // Set animation object
            val asset = assets.getImage(component.imageData)
            component.imageAnimView.animation = asset.animationsByName.getOrElse(component.animation) { asset.defaultAnimation }
            component.imageAnimView.onPlayFinished = {
                // when animation finished playing trigger destruction of entity
                this -= entity
            }
            component.imageAnimView.addTo(layerContainer)
            // Set play status
            component.imageAnimView.direction = when {
                component.forwardDirection && !component.loop -> ImageAnimation.Direction.ONCE_FORWARD
                !component.forwardDirection && component.loop -> ImageAnimation.Direction.REVERSE
                !component.forwardDirection && !component.loop -> ImageAnimation.Direction.ONCE_REVERSE
                else -> ImageAnimation.Direction.FORWARD
            }
            if (component.isPlaying) { component.imageAnimView.play() }
        }

        val onComponentRemoved: ComponentHook<Sprite> = { entity, component ->
            component.imageAnimView.removeFromParent()
        }
    }
}
