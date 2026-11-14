package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.tags.CameraFollowTag
import korlibs.math.geom.*


/**
 * This system is used to move the entity which has the [CameraFollowTag] attached (usually POV-Drone or player sprite)
 * towards a specific position (touch input, mouse input) on the screen.
 * This is used for debugging purposes to quickly move the camera inside the game world.
 *
 * Usage:
 * Call [moveDebugEntity] with the desired world position to move the debug entity to that
 */
class DebugSystem: IteratingSystem(
    family { all(CameraFollowTag).all(PositionComponent) },
    interval = Fixed(1 / 60f)
) {
    private val systemRuntimeConfigs = world.inject<SystemRuntimeConfigs>("SystemRuntimeConfigs")
    private var positionTrigger = false
    private var xPos = 0.0
    private var yPos = 0.0

    /**
     * This function is used to move the debug entity to the given position. This is useful for quickly
     * moving inside the game world to a specific position for debugging purposes.
     */
    fun moveDebugEntity(position: Vector2D) {
        // Use up position to store current touch position
        xPos = position.x
        yPos = position.y
        positionTrigger = true
    }

    override fun onTickEntity(entity: Entity) {
        // Get main camera position or exit if it does not exist
        val cameraPosition = systemRuntimeConfigs.getCameraPositionComponent(world) ?: return

        if (positionTrigger) {
            positionTrigger = false

            val positionComponent = entity[PositionComponent]
            positionComponent.x = xPos
            positionComponent.y = yPos
            positionComponent.convertToWorldCoordinates(cameraPosition)
        }
    }
}
