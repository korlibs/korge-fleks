package samples.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import samples.fleks.components.*

/**
 * This System takes care of displaying sprites (image-animation objects) on the screen. It takes the image configuration from
 * [Sprite] component to setup graphics from Assets and create an ImageAnimationView object for displaying in the Container.
 *
 */
class SpriteSystem : IteratingSystem(
    family { all(Sprite, Position) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {

        val sprite = entity[Sprite]
        val pos = entity[Position]
        // sync view position
        sprite.imageAnimView.x = pos.x.toFloat()
        sprite.imageAnimView.y = pos.y.toFloat()
    }
}
