package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.*

/**
 * A system which moves entities. It either takes the rigidbody of an entity into account or if not
 * it moves the entity linear without caring about gravity.
 */
class PositionSystem : IteratingSystem(
    family {
        all(PositionShapeComponent)  // Position component absolutely needed for movement of entity objects
        any(PositionShapeComponent, Motion, ParallaxMotion, Rigidbody, SubEntities)  // Rigidbody, CubicBezierLine, ect. not necessarily needed for movement
    },
    interval = EachFrame
) {
    // Overall world moving (playfield)
    val deltaX: Float = -110.0f  // TODO this will come from tiledMap scrolling
    val deltaY: Float = 0.0f

    override fun onTickEntity(entity: Entity) {
        val positionShapeComponent = entity[PositionShapeComponent]

        if (entity has Rigidbody && entity has Motion) {
            // Entity has a rigidbody - that means the movement will be calculated depending on it
            val rigidbody = entity[Rigidbody]
            // Currently we just add gravity to the entity
            entity[Motion].accelY += rigidbody.mass * 9.81f
            // TODO implement more sophisticated movement with rigidbody taking damping and friction into account
        }

        if (entity has Motion) {
            val motion = entity[Motion]
            // s(t) = a/2 * t^2 + v * t + s(t-1)
            positionShapeComponent.x = motion.accelX * 0.5f * deltaTime * deltaTime + motion.velocityX * deltaTime + positionShapeComponent.x
            positionShapeComponent.y = motion.accelX * 0.5f * deltaTime * deltaTime + motion.velocityY * deltaTime + positionShapeComponent.y
        }

        if (entity has ParallaxMotion) {
            val motion = entity[ParallaxMotion]
            // s(t) = v * t + s(t-1)
            if (motion.isScrollingHorizontally) {
                positionShapeComponent.x = ((deltaX * motion.speedFactor) + (motion.selfSpeedX * motion.speedFactor)) * deltaTime + positionShapeComponent.x
                // TODO get height of parallax background and scroll Y per deltaY [0..1] inside of height
            } else {
                positionShapeComponent.y = ((deltaY * motion.speedFactor) + (motion.selfSpeedY * motion.speedFactor)) * deltaTime + positionShapeComponent.y
                // TODO same as above for X
            }
        }

        if (entity has SubEntities && entity[SubEntities].moveWithParent) {
            entity[SubEntities].entities.forEach {
                val subEntity = it.value
                subEntity.getOrNull(PositionShapeComponent)?.let { subEntityPosition ->
                    subEntityPosition.x = positionShapeComponent.x
                    subEntityPosition.y = positionShapeComponent.y
                }
            }
        }
    }
}
