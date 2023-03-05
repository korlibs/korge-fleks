package com.soywiz.korgeFleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.soywiz.korgeFleks.components.*

/**
 * A system which moves entities. It either takes the rigidbody of an entity into account or if not
 * it moves the entity linear without caring about gravity.
 */
class PositionSystem : IteratingSystem(
    family {
        all(PositionShape)  // Position component absolutely needed for movement of entity objects
        any(PositionShape, Motion, Rigidbody, SubEntities)  // Rigidbody, CubicBezierLine, ect. not necessarily needed for movement
    },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val positionShape = entity[PositionShape]

        if (entity has Rigidbody && entity has Motion) {
            // Entity has a rigidbody - that means the movement will be calculated depending on it
            val rigidbody = entity[Rigidbody]
            // Currently we just add gravity to the entity
            entity[Motion].accelY += rigidbody.mass * 9.81
            // TODO implement more sophisticated movement with rigidbody taking damping and friction into account
        }

        if (entity has Motion) {
            val motion = entity[Motion]
            // s(t) = a/2 * t^2 + v * t + s(t-1)
            positionShape.x = motion.accelX * 0.5 * deltaTime * deltaTime + motion.velocityX * deltaTime + positionShape.x
            positionShape.y = motion.accelX * 0.5 * deltaTime * deltaTime + motion.velocityY * deltaTime + positionShape.y
        }

        if (entity has SubEntities) {
            entity[SubEntities].entities.forEach {
                val subEntity = it.value
                subEntity.getOrNull(PositionShape)?.let { subEntityPosition ->
                    subEntityPosition.x = positionShape.x
                    subEntityPosition.y = positionShape.y
                }
            }
        }
    }
}