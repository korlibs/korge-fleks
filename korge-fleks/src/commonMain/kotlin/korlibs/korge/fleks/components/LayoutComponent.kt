package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.view.align.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Currently this is used to get the layout details for an Entity from the asset object.
 * The asset object is configured from GameModel.
 *
 * Precondition:
 *   The component hook function needs the view for the entity.
 *   Thus, [DrawableComponent] and [PositionShapeComponent] needs to be added before adding LayoutComponent to the entity.
 */
@Serializable
@SerialName("Layout")
data class LayoutComponent(
    var centerX: Boolean = false,
    var centerY: Boolean = false,
    var offsetX: Double = 0.0,
    var offsetY: Double = 0.0
) : Component<LayoutComponent> {
    override fun type(): ComponentType<LayoutComponent> = LayoutComponent
    companion object : ComponentType<LayoutComponent>()

    /**
     * This function is invoked when a [LayoutComponent] gets added to an entity.
     */
    override fun World.onAdd(entity: Entity) {
/*
        println("Layout: onAdd  (id: ${entity.id})")

        if ()
        val view = KorgeViewCache[entity]
        if (centerX) view.centerXOnStage()
        if (centerY) view.centerYOnStage()

        if (entity has PositionShapeComponent) {
            val positionShapeComponent = entity[PositionShapeComponent]
            positionShapeComponent.x = view.x + offsetX  // view is needed otherwise the Sprite System will not take possible center values from above
            positionShapeComponent.y = view.y + offsetY
        }

*/
    }

    override fun World.onRemove(entity: Entity) {
    }

}
