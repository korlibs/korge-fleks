package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.EntityRefsByName.Companion.EntityRefsByNameComponent
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Rgba.Companion.RgbaComponent
import korlibs.korge.fleks.components.SpriteLayers.Companion.SpriteLayersComponent
import korlibs.korge.fleks.components.SwitchLayerVisibility.Companion.SwitchLayerVisibilityComponent
import korlibs.korge.fleks.utils.*
import kotlin.let


/**
 * This system iterates over all entities that have a [SpriteLayersComponent] and updates the
 * sprite layers as follows:
 * - [SwitchLayerVisibilityComponent] which defines how often the visibility of the
 *                                    sprite texture layers should be changed.
 *                                    The visibility can be switched on or off with variance values "onVariance" and "offVariance".
 *                                    The variance values define the probability when the visibility is changed.
 * - [EntityRefsByNameComponent] which defines entities that are linked to the sprite layers.
 *                               Those entities can be used to animate positions and colors of the
 *                               sprite layers.
 *
 * @see [SpriteLayersComponent]
 * @see [SwitchLayerVisibilityComponent]
 * @see [EntityRefsByNameComponent]
 */
class SpriteLayersSystem : IteratingSystem(
    family = family {
        all(SpriteLayersComponent)
            .any(SwitchLayerVisibilityComponent, EntityRefsByNameComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val layerMap = entity[SpriteLayersComponent].layerMap

        // Change visibility of all specified texture layers of a sprite
        if (entity has SwitchLayerVisibilityComponent) {
            val switchLayerVisibilityComponent = entity[SwitchLayerVisibilityComponent]

            layerMap.forEach { (_, property) ->
                property.visibility = if (property.visibility) {
                    // try to switch off -> visibility to false
                    // variance in switching value off - 1: every frame switching possible - 0: no switching at all
                    (0f..1f).random() >= switchLayerVisibilityComponent.offVariance
                } else {
                    // try to switch on again -> visibility to true
                    // variance in switching value on again - 1: changed value switches back immediately - 0: changed value stays forever
                    (0f..1f).random() <= switchLayerVisibilityComponent.onVariance
                }
            }
        }

        // Update positions and colors of the sprite layers based on linked entities
        if (entity has EntityRefsByNameComponent) {
            val entityRefsByNameComponent = entity[EntityRefsByNameComponent]
            layerMap.forEach { (name, property) ->
                entityRefsByNameComponent.entitiesByName[name]?.let { linkedEntity ->

                    val positionComponent = linkedEntity[PositionComponent]
                    property.offsetX = positionComponent.x + positionComponent.offsetX
                    property.offsetY = positionComponent.y + positionComponent.offsetY
                    property.rgba = linkedEntity[RgbaComponent].rgba
                }
            }
        }
    }
}
