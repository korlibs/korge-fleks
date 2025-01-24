package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.utils.*

/**
 * System for handling touch input on entities with [TouchInputComponent].
 * The system checks if the touch input area is enabled and if the touch input is within the bounds of the entity.
 *
 * Touch behavior can be controlled via [TouchInputComponent.continuousTouch]. If this flag is set to true then the
 * touch input will be processed as long as the touch input is within the bounds of the entity. Otherwise. touch
 * behaves like a button and the touch input will only be processed if the touch is released.
 *
 * In any way [TouchInputComponent.entityConfig] will be executed on the entity is touch input is detected.
 * The [TouchInputComponent.entityConfig] is a string that represents the name of an [EntityConfig] that will be executed on the entity.
 * The [TouchInputComponent.entity] is the entity that will be affected by the [TouchInputComponent.entityConfig].
 *
 * Internal touch input handling is done via following properties:
 * @property downX The x coordinate of the touch input when it was pressed
 * @property downY The y coordinate of the touch input when it was pressed
 * @property isJustDown Flag to indicate if display was just pressed (currently not used - no immediate action on touch down)
 * @property upX The x coordinate of the touch input when it was released
 * @property upY The y coordinate of the touch input when it was released
 * @property isJustUp Flag to indicate if display touch was just released
 * @property isTouchActive Flag to indicate if touch input is active (i.e. touch is pressed, mouse button is hold)
 */
class TouchInputSystem : IteratingSystem(
    family {
        all(TouchInputComponent, PositionComponent, SizeComponent)
    },
    interval = EachFrame
) {

    private var downX: Float = -1f
    private var downY: Float = -1f
    private var isJustDown: Boolean = false  // -- currently not used - no immediate action on touch down
    private var upX: Float = -1f
    private var upY: Float = -1f
    private var isJustUp: Boolean = false
    private var isTouchActive: Boolean = false

    /**
     * Check if the touch input is inside the bounds of the entity. Bounds are in screen coordinates.
     */
    private fun checkTouchInputInsideBounds(left: Float, top: Float, right: Float, bottom: Float): Boolean =
         (downX in left..right) && (downY in top..bottom) &&
             (upX in left..right) && (upY in top..bottom)

    /**
     * Set touch input position when screen is touched resp. mouse button is clicked.
     */
    fun onTouchDown(x: Float, y: Float) {
        downX = x
        downY = y
        // Set also up position to initial down position because it will be used for continuous touch input
        upX = x
        upY = y
//        isJustDown = true  -- currently not used
        isTouchActive = true
    }

    /**
     * Set touch input position when finger is moved on the touch screen resp. when mouse button is hold and mouse is moved.
     */
    fun onTouchMove(x: Float, y: Float) {
        if (isTouchActive) {
            // Use up position to store current touch position
            upX = x
            upY = y
        }
    }

    /**
     * Set touch input position when touch is released resp. mouse button is released.
     */
    fun onTouchUp(x: Float, y: Float) {
        upX = x
        upY = y
        isJustUp = true
    }

    override fun onTickEntity(entity: Entity) {
        val inputTouch = entity[TouchInputComponent]

        // Start touch input processing only if touch area is enabled
        if (inputTouch.enabled) {
            // Either touch is released (isUp) or continuous touch is enabled and touch is still down
            val touched = if (isJustUp) {
                val positionComponent = entity[PositionComponent]
                val sizeComponent = entity[SizeComponent]
                checkTouchInputInsideBounds(
                    left = positionComponent.x,
                    top = positionComponent.y,
                    right = positionComponent.x + sizeComponent.width,
                    bottom = positionComponent.y + sizeComponent.height
                )
            } else if (isTouchActive && inputTouch.continuousTouch) true
            else false
            // If touch input is within the bounds of the touch area then execute the entity config
            if (touched) {
                if (inputTouch.passPositionToEntity) {
                    // Pass touch position to entity if needed
                    inputTouch.entity.configure { toBeChangedEntity ->
                        val position = toBeChangedEntity[PositionComponent]
                        position.x = upX
                        position.y = upY
                    }
                }
                //println("TouchInputSystem: Execute '${inputTouch.entityConfig}' config on entity '${inputTouch.entity.id}'.")
                world.execute(inputTouch.entityConfig, inputTouch.entity)
            }
        }
    }

    override fun onUpdate() {
        super.onUpdate()

//        isJustDown = false  -- currently not used
        if (isJustUp) {
            isTouchActive = false
            isJustUp = false
        }
    }
}
