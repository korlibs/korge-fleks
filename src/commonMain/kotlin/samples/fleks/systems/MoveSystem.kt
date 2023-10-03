package samples.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import samples.fleks.components.*

/**
 * A system which moves entities. It either takes the rididbody of an entity into account or if not
 * it moves the entity linear without caring about gravity.
 */
class MoveSystem : IteratingSystem(
    family {
        all(Position)  // Position component absolutely needed for movement of entity objects
        any(Position, Rigidbody)  // Rigidbody not necessarily needed for movement
           },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val pos = entity[Position]

        if (entity has Rigidbody) {
            // Entity has a rigidbody - that means the movement will be calculated depending on it
            val rigidbody = entity[Rigidbody]
            // Currently we just add gravity to the entity
            pos.yAcceleration += rigidbody.mass * 9.81
            // TODO implement more sophisticated movement with rigidbody taking damping and friction into account
        }

        pos.x += pos.xAcceleration * deltaTime
        pos.y += pos.yAcceleration * deltaTime
    }
}
