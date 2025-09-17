package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.Collision.Companion.CollisionComponent
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.State.Companion.StateComponent
import korlibs.korge.fleks.components.data.StateType
import korlibs.korge.fleks.utils.Geometry


class PlayerMoveAfterCollisionSystem(
    private val inputSystem: PlayerInputSystem
) : IteratingSystem(
    family = World.family { all(CollisionComponent, MotionComponent, StateComponent) },
    interval = Fixed(1 / 60f)
) {
    override fun onTickEntity(entity: Entity) {
        val collisionComponent = entity[CollisionComponent]
        val motionComponent = entity[MotionComponent]
        val stateComponent = entity[StateComponent]

        // WARNING! WARNING! WARNING! WARNING! WARNING! WARNING! WARNING! WARNING! WARNING! WARNING!
        // Do not change game object state any more after moving
        // Otherwise the collider might change and lead to unexpected behaviour
        // WARNING! WARNING! WARNING! WARNING! WARNING! WARNING! WARNING! WARNING! WARNING! WARNING!

        // Update animation state after moving if the player is in front of wall
        if (collisionComponent.isGrounded
            && collisionComponent.isInFrontOfWall()
            && !collisionComponent.squatDown) {
            stateComponent.current = if (!inputSystem.attack) StateType.STAND else StateType.STAND_ATTACK
            // Reset animation timer first time when player is in front of wall - otherwise
            // the breath animation will not play
            if (!collisionComponent.wasInFrontOfWall) {
//                gameObject.animData.animationFrameCounter = 0
            }
        }
        // flip sprite as needed
        if (inputSystem.right) {
            stateComponent.direction = Geometry.RIGHT_DIRECTION
        } else if (inputSystem.left) {
            stateComponent.direction = Geometry.LEFT_DIRECTION
        }

        collisionComponent.isFalling = -(motionComponent.velocityY) < 0f  // invert Y velocity because the Y axis is inverted in the grid system
    }
}
