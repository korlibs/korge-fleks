package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.Rgba.Companion.RgbaComponent
import korlibs.korge.fleks.components.SwitchVisibility.Companion.SwitchVisibilityComponent
import korlibs.korge.fleks.utils.*


/**
 * This system iterates over all entities that have a [SwitchVisibilityComponent] and updates the
 * alpha value of the [RgbaComponent] of a sprite.
 *
 * @see [SwitchVisibilityComponent]
 */
class SpriteVisibilitySystem : IteratingSystem(
    family = family { all(RgbaComponent, SwitchVisibilityComponent) },
    interval = Fixed(1 / 60f)
) {
    override fun onTickEntity(entity: Entity) {
        val rgbaComponent = entity[RgbaComponent]
        val alpha = rgbaComponent.rgba.a

        // Change visibility (alpha value) of a sprite
        val switchVisibilityComponent = entity[SwitchVisibilityComponent]
        val newAlpha = if (alpha > 0) {
            // try to switch off -> visibility to false
            // variance in switching value off - 1: every frame switching possible - 0: no switching at all
            if ((0f..1f).random() >= switchVisibilityComponent.offVariance) 0 else 1
        } else {
            // try to switch on again -> visibility to true
            // variance in switching value on again - 1: changed value switches back immediately - 0: changed value stays forever
            if ((0f..1f).random() <= switchVisibilityComponent.onVariance) 1 else 0
        }

        // Apply new alpha value
        if (newAlpha != alpha) rgbaComponent.rgba.withA(newAlpha)
    }
}
