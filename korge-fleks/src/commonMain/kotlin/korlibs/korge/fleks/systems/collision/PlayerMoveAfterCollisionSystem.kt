package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.Collision.Companion.CollisionComponent
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.state.PlayerInputState


class PlayerMoveAfterCollisionSystem : IteratingSystem(
    family = World.family { all(CollisionComponent, MotionComponent) },
    interval = Fixed(1 / 60f)
) {
    private val inputState by lazy { world.inject<PlayerInputState>("InputState") }

    override fun onTickEntity(entity: Entity) {
        val collisionComponent = entity[CollisionComponent]
        val motionComponent = entity[MotionComponent]
//        val stateComponent = entity[StateComponent]

        // WARNING! WARNING! WARNING! WARNING! WARNING! WARNING! WARNING! WARNING! WARNING! WARNING!
        // Do not change game object state any more after moving
        // Otherwise the collider might change and lead to unexpected behavior
        // WARNING! WARNING! WARNING! WARNING! WARNING! WARNING! WARNING! WARNING! WARNING! WARNING!

        // Update animation state after moving if the player is in front of wall
        if (collisionComponent.isGrounded
            && collisionComponent.isInFrontOfWall()
            && !collisionComponent.squatDown) {
//            stateComponent.current = if (!inputState.attack) StateType.STAND else StateType.STAND_ATTACK
            // Reset animation timer first time when player is in front of wall - otherwise
            // the breath animation will not play
            if (!collisionComponent.wasInFrontOfWall) {
//                gameObject.animData.animationFrameCounter = 0
            }
        }
        // flip sprite as needed
        if (inputState.right) {
//            stateComponent.direction = Geometry.RIGHT_DIRECTION
        } else if (inputState.left) {
//            stateComponent.direction = Geometry.LEFT_DIRECTION
        }

// TODO cleanup
//        if (inputState.justUp) {
//            collisionComponent.canJump = false
//        } else if (inputState.justReleasedUp) {
//            collisionComponent.canJump = true
//        }

        if (collisionComponent.isGrounded) {
            collisionComponent.isFalling = false
        } else {
            // Set isFalling to true if the player is moving downwards and not grounded
            collisionComponent.isFalling = motionComponent.velocityY > 0f  // Y velocity is positive when moving downwards in the grid system
            if (collisionComponent.isFalling) {
//                println("Player is falling with velocity ${motionComponent.velocityY}")
            }
        }

//        collisionComponent.isFalling = -(motionComponent.velocityY) < 0f  // invert Y velocity because the Y axis is inverted in the grid system
//        if (collisionComponent.isFalling) {
//            println("Player is falling with velocity ${motionComponent.velocityY}")
//        }

    }
}
