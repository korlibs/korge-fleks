package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.Parallax.Companion.ParallaxComponent
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Rigidbody.Companion.RigidbodyComponent

/**
 * A system which moves entities. It either takes the rigidbody of an entity into account or if not
 * it moves the entity linear without caring about gravity.
 */
class PositionSystem : IteratingSystem(
    family {
        all(PositionComponent)  // Position component absolutely needed for movement of entity objects
        .any(MotionComponent, RigidbodyComponent)  // Motion, Rigidbody, ect. not necessarily needed for movement
        .none(ParallaxComponent)
    },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val positionComponent = entity[PositionComponent]

        if (entity has RigidbodyComponent && entity has MotionComponent) {
            // Entity has a rigidbody - that means the movement will be calculated depending on it
            val rigidbody = entity[RigidbodyComponent]
            // Currently we just add gravity to the entity
            entity[MotionComponent].accelY += rigidbody.mass * 9.81f
            // TODO implement more sophisticated movement with rigidbody taking damping and friction into account
        }

        if (entity has MotionComponent) {
            val motion = entity[MotionComponent]
            // s(t) = a/2 * t^2 + v * t + s(t-1)
            positionComponent.x = motion.accelX * 0.5f * deltaTime * deltaTime + motion.velocityX * deltaTime + positionComponent.x
            positionComponent.y = motion.accelX * 0.5f * deltaTime * deltaTime + motion.velocityY * deltaTime + positionComponent.y
        }
    }
}
