package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.InputTouchButton

/**
 *
 *
 */
class TouchInputSystem(
    private val assets: AssetStore = World.inject<AssetStore>("AssetStore")
) : IteratingSystem(
    family {
        all(InputTouchButton)
    },
    interval = EachFrame
) {


    override fun onTickEntity(entity: Entity) {
        val inputTouchButton = entity[InputTouchButton]
//        if (inputTouchButton.onDown) {
//            inputTouchButton.onDown = false
//            println("down")
//        }
//        if (inputTouchButton.onUp) {
//            inputTouchButton.onUp = false
//            println("up")
//        }
    }
}