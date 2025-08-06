package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.PlayerInput.Companion.PlayerInputComponent


class PlayerMoveSystem : IteratingSystem(
    family = World.family { all(PlayerInputComponent, MotionComponent) },
    interval = Fixed(1 / 30f)
) {

    override fun onTickEntity(entity: Entity) {
        val playerInputComponent = entity[PlayerInputComponent]
        val motionComponent = entity[MotionComponent]

        // TODO Check if max velocity for player movement is reached
        motionComponent.velocityX += playerInputComponent.speed * playerInputComponent.xMoveStrength
//        motionComponent.velocityY += playerInputComponent.speed * playerInputComponent.yMoveStrength

        // Player pressed down arrow key (s key)
        if (playerInputComponent.yMoveStrength > 0f) {
        }

        // Player pressed up arrow key (w key)
        if (playerInputComponent.yMoveStrength < 0f) {
            motionComponent.velocityY += playerInputComponent.jumpForce * playerInputComponent.yMoveStrength
            if (motionComponent.velocityY < -1f) {
                motionComponent.velocityY = -1f // Limit the jump force
            }
        }

/*
To simulate mass/inertia so that an entity starts moving slowly, introduce an acceleration variable and apply forces
gradually. The entity's velocity increases over time based on the applied force and its mass.

Example: Add mass and acceleration to your motion component, and update velocity using Newton's second law (F = m * a).

data class Motion(
    var velocityX: Float = 0f,
    var velocityY: Float = 0f,
    var accelerationX: Float = 0f,
    var accelerationY: Float = 0f,
    var mass: Float = 1f
)

fun applyForce(motion: Motion, forceX: Float, forceY: Float) {
    // F = m * a => a = F / m
    motion.accelerationX += forceX / motion.mass
    motion.accelerationY += forceY / motion.mass
}

fun updateMotion(motion: Motion, deltaTime: Float) {
    motion.velocityX += motion.accelerationX * deltaTime
    motion.velocityY += motion.accelerationY * deltaTime
    // Optionally apply friction here
    // Reset acceleration after applying
    motion.accelerationX = 0f
    motion.accelerationY = 0f
}

This way, heavier entities accelerate slower, simulating inertia.
*/

    }
}
