package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.Sprite.Companion.SpriteComponent
import korlibs.korge.fleks.components.SwitchVisibility.Companion.SwitchVisibilityComponent
import korlibs.korge.fleks.utils.*


/**
 * This system iterates over all entities that have a [SwitchVisibilityComponent] and [SpriteComponent] and updates the
 * visible property of the sprite component based on the probabilities (on-/off variance) defined in the
 * [SwitchVisibilityComponent].
 *
 * @see [SwitchVisibilityComponent]
 */
class SpriteVisibilitySystem : IteratingSystem(
    family = family { all(SpriteComponent, SwitchVisibilityComponent) },
    interval = Fixed(1 / 60f)
) {
    override fun onTickEntity(entity: Entity) {
        val switchVisibilityComponent = entity[SwitchVisibilityComponent]
        val spriteComponent = entity[SpriteComponent]
        val visible = spriteComponent.visible

        // Change visibility of a sprite
        spriteComponent.visible = if (visible) {
            // try to switch off -> visible to false
            // variance in switching value off - 1: every frame switching possible - 0: no switching at all
            (0f..1f).random() >= switchVisibilityComponent.offVariance
        } else {
            // try to switch on again -> visible to true
            // variance in switching value on again - 1: changed value switches back immediately - 0: changed value stays forever
            (0f..1f).random() <= switchVisibilityComponent.onVariance
        }
    }
}
