package korlibs.korge.fleks.familyHooks

import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.FamilyHook
import com.github.quillraven.fleks.World
import korlibs.image.color.RGBA
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.entity.config.Invokable
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
 * - [SpecificLayerComponent] : Contains the [Layer name][SpecificLayerComponent.spriteLayer] and the parent entity
 * with the [SpriteComponent] data (graphics).
 * - [AppearanceComponent] (optional) : Contains values for [visibility][AppearanceComponent.visible], [alpha][AppearanceComponent.alpha] and
 * [color tint][AppearanceComponent.tint].
 * - [PositionShapeComponent] (optional) : Contains the [x][PositionShapeComponent.x] and [y][PositionShapeComponent.y] position of the
 * layer relative to the [SpriteComponent].
 * - [InputTouchButtonComponent] (optional) : Contains the [Invokable] functions when the player touches that layer on the display.
 *
 * One of the optional components must be added to the specific-layer entity. Otherwise, the [SpecificLayerComponent] component
 * will be useless.
 */
fun specificLayerFamily(): Family = World.family { all(SpecificLayerComponent).any(SpecificLayerComponent, PositionShapeComponent, AppearanceComponent, InputTouchButtonComponent, OffsetComponent) }

val onSpecificLayerFamilyAdded: FamilyHook = { entity ->
    val world = this

    // Need to get parent entity to search for view object which contains the sprite layer
    val specificLayer = entity[SpecificLayerComponent]

    val view: View = if (specificLayer.parallaxPlaneLine != null) {
        val pView = KorgeViewCache[specificLayer.parentEntity]
        pView as ParallaxDataView
        pView.parallaxLines[specificLayer.parallaxPlaneLine!!] ?: error("onSpecificLayerFamilyAdded: Parallax Line '${specificLayer.parallaxPlaneLine}' is null!")
    } else if (specificLayer.spriteLayer != null) {
        KorgeViewCache.getLayer(specificLayer.parentEntity, specificLayer.spriteLayer!!)
    } else {
        error("onSpecificLayerFamilyAdded: No sprite layer name or parallax plane line number set for entity '${entity.id}'!")
    }

    entity.getOrNull(AppearanceComponent)?.also {
        view.visible = it.visible
        view.alpha = it.alpha.toDouble()
        it.tint?.also { tint -> view.colorMul = RGBA(tint.r, tint.g, tint.b, 0xff) }
    }

    // Set properties in TouchInput when touch input was recognized
    // TouchInputSystem checks for those properties and executes specific Invokable function
    entity.getOrNull(InputTouchButtonComponent)?.let { touchInput ->
        view.mouse {
            onDown {
                if (touchInput.triggerImmediately) Invokable.invoke(touchInput.function, world, entity, touchInput.config)
                touchInput.pressed = true
            }
            onUp {
                if (touchInput.pressed) {
                    touchInput.pressed = false
                    Invokable.invoke(touchInput.function, world, entity, touchInput.config)
                }
            }
            onUpOutside {
                if (touchInput.pressed) {
                    touchInput.pressed = false
                    if (touchInput.triggerImmediately) Invokable.invoke(touchInput.function, world, entity, touchInput.config)
                }
            }
        }
    }

    // Save current position of layer into PositionShape component
    entity.getOrNull(PositionShapeComponent)?.let {
        if (!it.initialized) {
            it.x = view.x
            it.y = view.y
            it.initialized = true
        }
    }

    KorgeViewCache.addOrUpdate(entity, view)
}

val onSpecificLayerFamilyRemoved: FamilyHook = { entity ->
    KorgeViewCache.remove(entity)
}
