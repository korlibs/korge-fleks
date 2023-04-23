package korlibs.korge.fleks.familyHooks

import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.FamilyHook
import com.github.quillraven.fleks.World
import korlibs.image.color.RGBA
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.utils.KorgeViewCache
import korlibs.korge.input.mouse
import korlibs.korge.parallax.ParallaxDataView
import korlibs.korge.view.View

/**
 * This Family-Hook specifies an entity which controls an image layer of a sprite.
 * The graphics of a sprites, when created and loaded from Aseprite, can contain layers.
 * Those layers can be controlled independently. E.g. the relative position of the layer
 * graphics inside the sprite or the visibility (alpha value) can be manipulated
 * independently per sprite layer. Also touch input can be caught and specific Invokable
 * functions can be started.
 *
 * The related components contain following details:
 * - [SpecificLayer] : Contains the [Layer name][SpecificLayer.spriteLayer] and the parent entity
 * with the [Sprite] data (graphics).
 * - [Appearance] (optional) : Contains values for [visibility][Appearance.visible], [alpha][Appearance.alpha] and
 * [color tint][Appearance.tint].
 * - [PositionShape] (optional) : Contains the [x][PositionShape.x] and [y][PositionShape.y] position of the
 * layer relative to the [Sprite].
 * - [InputTouchButton] (optional) : Contains the [Invokable] functions when the player touches that layer on the display.
 *
 * One of the optional components must be added to the specific-layer entity. Otherwise, the [SpecificLayer] component
 * will be useless.
 */
fun specificLayerFamily(): Family = World.family { all(SpecificLayer).any(SpecificLayer, PositionShape, Appearance, InputTouchButton, Offset) }

val onSpecificLayerFamilyAdded: FamilyHook = { entity ->
    val world = this
    val korgeViewCache = inject<KorgeViewCache>("normalViewCache")

    // Need to get parent entity to search for view object which contains the sprite layer
    val specificLayer = entity[SpecificLayer]

    val view: View = if (specificLayer.parallaxPlaneLine != null) {
        val pView = korgeViewCache[specificLayer.parentEntity]
        pView as ParallaxDataView
        pView.parallaxLines[specificLayer.parallaxPlaneLine!!] ?: error("OnSpecificLayerFamily: Parallax Line '${specificLayer.parallaxPlaneLine}' is null!")
    } else if (specificLayer.spriteLayer != null) {
        korgeViewCache.getLayer(specificLayer.parentEntity, specificLayer.spriteLayer!!)
    } else {
        error("OnSpecificLayerFamily: No sprite layer name or parallax plane line number set for entity '${entity.id}'!")
    }

    entity.getOrNull(Appearance)?.also {
        view.visible = it.visible
        view.alpha = it.alpha
        it.tint?.also { tint -> view.colorMul = RGBA(tint.r, tint.g, tint.b, 0xff) }
    }

    // Set properties in TouchInput when touch input was recognized
    // TouchInputSystem checks for those properties and executes specific Invokable function
    entity.getOrNull(InputTouchButton)?.let { touchInput ->
        view.mouse {
            onDown {
                if (touchInput.triggerImmediately) touchInput.action.invoke(world, entity)
                touchInput.pressed = true
            }
            onUp {
                if (touchInput.pressed) {
                    touchInput.pressed = false
                    touchInput.action.invoke(world, entity)
                }
            }
            onUpOutside {
                if (touchInput.pressed) {
                    touchInput.pressed = false
                    if (touchInput.triggerImmediately) touchInput.action.invoke(world, entity)
                }
            }
        }
    }

    // Save current position of layer into PositionShape component
    entity.getOrNull(PositionShape)?.let {
        if (!it.initialized) {
            it.x = view.x
            it.y = view.y
            it.initialized = true
        }
    }

    korgeViewCache.addOrUpdate(entity, view)
}

val onSpecificLayerFamilyRemoved: FamilyHook = { entity ->
    inject<KorgeViewCache>("normalViewCache").remove(entity)
}
