package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.Grid.Companion.GridComponent
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.Parallax.Companion.ParallaxComponent
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Rigidbody.Companion.RigidbodyComponent

/**
 * A system which moves entities independent of gravity and collision.
 */
class PositionSystem : IteratingSystem(
    family {
        all(PositionComponent)  // Position component absolutely needed for movement of entity objects
        .any(MotionComponent, RigidbodyComponent, GridComponent)  // Motion, Rigidbody, ect. not necessarily needed for movement
        .none(ParallaxComponent)
    },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val positionComponent = entity[PositionComponent]

// TODO move into GridMoveSystem
//        if (entity has RigidbodyComponent && entity has MotionComponent) {
//            // Entity has a rigidbody - that means the movement will be calculated depending on it
//            val rigidbody = entity[RigidbodyComponent]
//            // Currently we just add gravity to the entity
//            entity[MotionComponent].accelY += rigidbody.mass * 9.81f
//            // TODO implement more sophisticated movement with rigidbody taking damping and friction into account
//        }

        if (entity has GridComponent) {
            // Take over the position from the grid component (interpolation used for smooth movement)
            val gridComponent = entity[GridComponent]
            positionComponent.x = gridComponent.x
            positionComponent.y = gridComponent.y
        } else if (entity has MotionComponent) {
            // If no grid component is present then we use the motion component to calculate the position
            // This is used for entities which are statically moving like in the intro sequence
            val motion = entity[MotionComponent]
            // s(t) = a/2 * t^2 + v * t + s(t-1)
            positionComponent.x = motion.accelerationX * 0.5f * deltaTime * deltaTime + motion.velocityX * deltaTime + positionComponent.x
            positionComponent.y = motion.accelerationX * 0.5f * deltaTime * deltaTime + motion.velocityY * deltaTime + positionComponent.y
        }
    }
}
