package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.utils.*

/**
 *
 *
 */
class TouchInputSystem : IteratingSystem(
    family {
        all(TouchInputComponent, PositionComponent, SizeComponent)
    },
    interval = EachFrame
) {

    private var downX: Float = -1f
    private var downY: Float = -1f
    private var isDown: Boolean = false
    private var upX: Float = -1f
    private var upY: Float = -1f
    private var isUp: Boolean = false

    private fun checkTouchInput(left: Float, top: Float, right: Float, bottom: Float): Boolean =
         (downX in left..right) && (downY in top..bottom) &&
             (upX in left..right) && (upY in top..bottom)


    fun onTouchDown(x: Float, y: Float) {
        downX = x
        downY = y
        isDown = true
    }

    fun onTouchUp(x: Float, y: Float) {
        upX = x
        upY = y
        isUp = true
    }

    override fun onTickEntity(entity: Entity) {

        if (isUp) {
            val inputTouchButton = entity[TouchInputComponent]
            val positionComponent = entity[PositionComponent]
            val sizeComponent = entity[SizeComponent]

            val touched = checkTouchInput(
                left = positionComponent.x,
                top = positionComponent.y,
                right = positionComponent.x + sizeComponent.width,
                bottom = positionComponent.y + sizeComponent.height
            )

            if (!inputTouchButton.pressed && touched) {
                inputTouchButton.pressed = true
                println("TouchInputSystem: Execute '${inputTouchButton.entityConfig}' config on entity '${inputTouchButton.entity.id}'.")
                world.execute(inputTouchButton.entityConfig, inputTouchButton.entity)
            } else if (!touched) {
                // Reset pressed state again
                inputTouchButton.pressed = false
            }
        }
    }

    override fun onUpdate() {
        super.onUpdate()

        if (isUp) {
            isDown = false
            isUp = false
        }
    }
}
