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

        // To simulate mass/inertia so that an entity starts moving slowly, we use an acceleration variable and apply forces
        // gradually. The entity's velocity increases over time based on the applied force and its mass.

        // TODO Check if max velocity for player movement is reached


        // a = F / m
        // Apply acceleration based on player input and mass
        motionComponent.accelerationX += playerInputComponent.forceX * playerInputComponent.xMoveStrength
        motionComponent.velocityX += motionComponent.accelerationX * deltaTime

//        motionComponent.velocityX += playerInputComponent.forceX * playerInputComponent.xMoveStrength
//        motionComponent.velocityY += playerInputComponent.speed * playerInputComponent.yMoveStrength

        // Player pressed down arrow key (s key)
        if (playerInputComponent.yMoveStrength > 0f) {
        }

        // Player pressed up arrow key (w key)
        if (playerInputComponent.yMoveStrength < 0f) {
            motionComponent.velocityY += playerInputComponent.forceY * playerInputComponent.yMoveStrength
            if (motionComponent.velocityY < -5f) {
                motionComponent.velocityY = -5f // Limit the jump force
            }
        }

/*

Example: Add mass and acceleration to your motion component, and update velocity using Newton's second law (F = m * a).



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
