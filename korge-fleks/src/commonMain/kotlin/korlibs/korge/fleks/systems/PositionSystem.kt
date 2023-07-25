package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.utils.random
import korlibs.math.interpolation.Easing

/**
 * A system which moves entities. It either takes the rigidbody of an entity into account or if not
 * it moves the entity linear without caring about gravity.
 */
class PositionSystem : IteratingSystem(
    family {
        all(PositionShape)  // Position component absolutely needed for movement of entity objects
        any(PositionShape, Motion, ParallaxMotion, Rigidbody, SubEntities, NoisyMove)
    },
    interval = EachFrame
) {
    // Overall world moving (playfield)
    val deltaX: Float = -110.0f  // TODO this will come from tiledMap scrolling
    val deltaY: Float = 0.0f

    override fun onTickEntity(entity: Entity) {
        val positionShape = entity[PositionShape]

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
            positionShape.x = motion.accelX * 0.5f * deltaTime * deltaTime + motion.velocityX * deltaTime + positionShape.x
            positionShape.y = motion.accelX * 0.5f * deltaTime * deltaTime + motion.velocityY * deltaTime + positionShape.y
        }

        if (entity has ParallaxMotion) {
            val motion = entity[ParallaxMotion]
            // s(t) = v * t + s(t-1)
            if (motion.isScrollingHorizontally) {
                positionShape.x = ((deltaX * motion.speedFactor) + (motion.selfSpeedX * motion.speedFactor)) * deltaTime + positionShape.x
                // TODO get height of parallax background and scroll Y per deltaY [0..1] inside of height
            } else {
                positionShape.y = ((deltaY * motion.speedFactor) + (motion.selfSpeedY * motion.speedFactor)) * deltaTime + positionShape.y
                // TODO same as above for X
            }
        }

        if (entity has NoisyMove) {
            val noisyMove = entity[NoisyMove]


            with(noisyMove) {
                if (interval != 0f) {
                    if (timeProgress >= waitTime) {
                        timeProgress = 0f
                        waitTime = interval + if (intervalVariance != 0f) (-intervalVariance .. intervalVariance).random() else 0f
                        if (waitTime < 0f) waitTime = 0f

                        val startX = x
                        val startY = y
                        val endX = xTarget + if (xVariance != 0f) (-xVariance .. xVariance).random() else 0f
                        val endY = yTarget + if (yVariance != 0f) (-yVariance .. yVariance).random() else 0f
                        updateAnimateComponent(world, entity, AnimateComponentType.NoisyMoveX, value = startX, change = endX - startX, waitTime, Easing.EASE_IN_OLD)
                        updateAnimateComponent(world, entity, AnimateComponentType.NoisyMoveY, value = startY, change = endY - startY, waitTime, Easing.EASE_IN_OUT)

                    } else timeProgress += deltaTime
                }
            }


/*
            val random: Float = (0f .. 1f).random()
            if (!automaticMoving.triggered && random < automaticMoving.triggerVariance) {
                // Create new random variance value for adding to offset
                val startX = automaticMoving.x
                val startY = automaticMoving.y
                val endX = automaticMoving.xVariance * (-1f .. 1f).random()
                val endY = automaticMoving.yVariance * (-1f .. 1f).random()
                updateAnimateComponent(entity, AnimateComponentType.NoisyMoveX, value = startX, change = endX - startX, 3f, Easing.EASE_IN_OLD)
                updateAnimateComponent(entity, AnimateComponentType.NoisyMoveY, value = startY, change = endY - startY, 3f, Easing.EASE_IN_OUT)
                automaticMoving.triggered = true
            } else if (automaticMoving.triggered && random < automaticMoving.terminateVariance) {
                // Reset and enable switching to new random value again
                automaticMoving.triggered = false
            }
*/
        }

        if (entity has SubEntities && entity[SubEntities].moveWithParent) {
            entity[SubEntities].entities.forEach {
                val subEntity = it.value
                subEntity.getOrNull(PositionShape)?.let { subEntityPosition ->
                    if (entity has NoisyMove) {
                        val noisyMove = entity[NoisyMove]
                        subEntityPosition.x = positionShape.x - noisyMove.x
                        subEntityPosition.y = positionShape.y - noisyMove.y
                    } else {
                        subEntityPosition.x = positionShape.x
                        subEntityPosition.y = positionShape.y
                    }
                }
            }
        }
    }

    private fun updateAnimateComponent2(entity: Entity, componentProperty: AnimateComponentType, value: Any, change: Any = Unit, duration: Float? = null, easing: Easing? = null) {
        entity.configure { animatedEntity ->
            animatedEntity.getOrAdd(componentProperty.type) { AnimateComponent(componentProperty) }.also {
                it.change = change
                it.value = value
                it.duration = duration ?: 0f
                it.timeProgress = 0f
                it.easing = easing ?: Easing.LINEAR
            }
        }
    }
}
