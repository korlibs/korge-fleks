package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.utils.*

/**
 * System for handling touch input on entities with [TouchInputComponent].
 * The system checks if the touch input is within the bounds of the entity and if the entity is enabled.
 * If the entity is touched and the touch input is released then the [TouchInputComponent.entityConfig] will be executed on the entity.
 * The [TouchInputComponent.entityConfig] is a string that represents the name of an [EntityConfig] that will be executed on the entity.
 * The [TouchInputComponent.entity] is the entity that will be affected by the [TouchInputComponent.entityConfig].
 *
 * @property downX The x coordinate of the touch input when it was pressed
 * @property downY The y coordinate of the touch input when it was pressed
 * @property isDown Flag to indicate if display is pressed
 * @property upX The x coordinate of the touch input when it was released
 * @property upY The y coordinate of the touch input when it was released
 * @property isUp Flag to indicate if display is released
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
        val inputTouchButton = entity[TouchInputComponent]

        if (inputTouchButton.enabled && isUp) {
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
