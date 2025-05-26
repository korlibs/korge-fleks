package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.SpriteLayers.Companion.SpriteLayersComponent
import korlibs.korge.fleks.components.SwitchLayerVisibility.Companion.SwitchLayerVisibilityComponent
import korlibs.korge.fleks.utils.*


class SpriteLayersSystem : IteratingSystem(
    family = family { all(SpriteLayersComponent, SwitchLayerVisibilityComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val layerMap = entity[SpriteLayersComponent].layerMap
        val switchLayerVisibilityComponent = entity[SwitchLayerVisibilityComponent]

        // Change visibility of all specified texture layers of a sprite
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

        // TODO implement changing offset of specific texture layer
        //      needed for moving parallax clouds up on the screen
    }
}
