package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.InputTouchButtonComponent

/**
 *
 *
 */
class TouchInputSystem : IteratingSystem(
    family {
        all(InputTouchButtonComponent)
    },
    interval = EachFrame
) {


    override fun onTickEntity(entity: Entity) {
        val inputTouchButton = entity[InputTouchButtonComponent]
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
