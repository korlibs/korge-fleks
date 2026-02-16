package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.Grid.Companion.GridComponent
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.tags.CameraFollowTag
import korlibs.korge.fleks.utils.*
import korlibs.math.geom.*


/**
 * This system is used to move the POV-Drone or player sprite entity towards a specific position (touch input,
 * mouse input) on the screen.
 */
class DebugSystem: IteratingSystem(
//    family { all(DebugComponent).any(PositionComponent, GridComponent) },  // Change to move debug entity only which has DebugComponent attached
    family { all(CameraFollowTag).any(PositionComponent, GridComponent) },
    interval = Fixed(1 / 60f)
) {
    private var positionTrigger = false
    private var xPos = 0f
    private var yPos = 0f

    /**
     * This function is used to move the debug entity to the given position. This is useful for quickly
     * moving inside the game world to a specific position for debugging purposes.
     */
    fun moveDebugEntity(position: Vector2F) {
        // Use up position to store current touch position
        xPos = position.x
        yPos = position.y
        positionTrigger = true
    }

    override fun onTickEntity(entity: Entity) {
        val camera: Entity = world.getMainCamera()

        if (positionTrigger) {
            positionTrigger = false

            // Transform incoming screen coordinates to world coordinates
            if (entity has GridComponent) {
                val gridComponent = entity[GridComponent]
                gridComponent.x = xPos
                gridComponent.y = yPos
                gridComponent.run { world.convertToWorldCoordinates(camera) }
            } else if (entity has PositionComponent) {
                val positionComponent = entity[PositionComponent]
                positionComponent.x = xPos
                positionComponent.y = yPos
                positionComponent.run { world.convertToWorldCoordinates(camera) }
            }
        }
    }
}
