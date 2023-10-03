package samples.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import samples.fleks.components.Destruct
import samples.fleks.components.Impulse
import samples.fleks.components.Position

class CollisionSystem : IteratingSystem(
    family { all(Position) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val pos = entity[Position]

        // To make collision detection easy we check here just the Y position if it is below 200 which means
        // that the object is colliding - In real games here is a more sophisticated collision check necessary ;-)
        if (pos.y > 200.0) {
            pos.y = 200.0
            // Check if entity has a Destruct or Impulse component
            if (entity has Destruct) {
                // Delegate "destruction" of the entity to the DestructSystem - it will destroy the entity after some other task are done
                entity[Destruct].triggerDestruction = true
            } else if (entity has Impulse) {
                // Do not destruct entity but let it bounce on the surface
                pos.xAcceleration = pos.xAcceleration * 0.7
                pos.yAcceleration = -pos.yAcceleration * 0.9
            } else {
                // Entity gets destroyed immediately
                world -= entity
            }
        }
    }
}
