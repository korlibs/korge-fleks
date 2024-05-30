package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.utils.*


class SpriteLayersSystem : IteratingSystem(
    family = family { all(SpriteLayersComponent, SwitchLayerVisibilityComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val (layerMap) = entity[SpriteLayersComponent]
        val (offVariance, onVariance) = entity[SwitchLayerVisibilityComponent]

        // Change visibility of all specified texture layers of a sprite
        layerMap.forEach { (_, property) ->
            property.visibility = if (property.visibility) {
                // try to switch off -> visibility to false
                // variance in switching value off - 1: every frame switching possible - 0: no switching at all
                (0f..1f).random() >= offVariance
            } else {
                // try to switch on again -> visibility to true
                // variance in switching value on again - 1: changed value switches back immediately - 0: changed value stays forever
                (0f..1f).random() <= onVariance
            }
        }

        // TODO implement changing offset of specific texture layer
        //      needed for moving parallax clouds up on the screen
    }
}
