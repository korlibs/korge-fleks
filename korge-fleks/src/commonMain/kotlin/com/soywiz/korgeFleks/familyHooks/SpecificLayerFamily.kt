package com.soywiz.korgeFleks.familyHooks

import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.FamilyHook
import com.github.quillraven.fleks.World
import com.soywiz.korgeFleks.components.*
import com.soywiz.korgeFleks.utils.ImageAnimView
import com.soywiz.korgeFleks.utils.KorgeViewCache
import com.soywiz.korim.color.RGBA

/**
 * This Family-Hook specifies an entity which controls an image layer of a sprite.
 * The graphics of a sprites, when created and loaded from Aseprite, can contain layers.
 * Those layers can be controlled independently. E.g. the relative position of the layer
 * graphics inside the sprite or the visibility (alpha value) can be manipulated
 * independently per sprite layer.
 *
 * The related components contain following details:
 * - [SpecificLayer] : Contains the [Layer name][SpecificLayer.spriteLayer] and the parent entity
 * with the [Sprite] data (graphics).
 * - [Appearance] (optional) : Contains values for [visibility][Appearance.visible], [alpha][Appearance.alpha] and
 * [color tint][Appearance.tint].
 * - [PositionShape] (optional) : Contains the [x][PositionShape.x] and [y][PositionShape.y] position of the
 * layer relative to the [Sprite].
 *
 * One of the two optional components must be added to the specific-layer entity.
 */
object SpecificLayerFamily {
    val define: Family = World.family { all(SpecificLayer).any(SpecificLayer, Appearance, PositionShape, Offset) }

    val onEntityAdded: FamilyHook = { entity ->
        val korgeViewCache = inject<KorgeViewCache>("normalViewCache")

        // Need to get parent entity to search for view object which contains the sprite layer
        val specificLayer = entity[SpecificLayer]
        val view = (korgeViewCache[specificLayer.parentEntity] as ImageAnimView).getLayer(specificLayer.spriteLayer)
                ?: error("SpecificLayerFamily.onEntityAdded: Could not find layer with name '${specificLayer.spriteLayer}'!")

        entity.getOrNull(Appearance)?.also {
            view.visible = it.visible
            view.alpha = it.alpha
            it.tint?.also { tint -> view.colorMul = RGBA(tint.r, tint.g, tint.b, 0xff) }
        }
        entity.getOrNull(PositionShape)?.also {
            // Save current position of layer into PositionShape component
            it.x = view.x
            it.y = view.y
        }

        korgeViewCache.addOrUpdate(entity, view)
    }

    val onEntityRemoved: FamilyHook = { entity ->
        (inject<KorgeViewCache>("normalViewCache").getOrNull(entity)
            ?: error("SpecificLayerFamily.onEntityRemoved: Cannot remove view from entity '${entity.id}' with layer name '${entity[SpecificLayer].spriteLayer}'!"))
            .removeFromParent()
    }
}