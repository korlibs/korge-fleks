package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.assets.data.gameObject.MotionConfig
import korlibs.korge.fleks.components.Collision.Companion.CollisionComponent
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.State
import korlibs.korge.fleks.components.State.Companion.StateComponent
import korlibs.korge.fleks.components.data.StateType
import korlibs.korge.fleks.utils.Geometry
import kotlin.math.abs
import korlibs.math.interpolation.interpolate


class PlayerMoveSystem(
    private val inputSystem: PlayerInputSystem
) : IteratingSystem(
    family = World.family { all(MotionComponent, CollisionComponent, StateComponent) },
    interval = Fixed(1 / 60f)  // TODO check if we use here 30 or 60 Hz/FPS
) {
    val assetStore = world.inject<AssetStore>("AssetStore")

    override fun onTickEntity(entity: Entity) {
        val collisionComponent = entity[CollisionComponent]
        val motionComponent = entity[MotionComponent]
        val stateComponent = entity[StateComponent]

        var velocityY = (-motionComponent.velocityY)  // invert Y velocity because the Y axis is inverted in the grid system
        var velocityX = 0f // reset horizontal velocity

        val motionConfig = assetStore.getGameObjectStateConfig(stateComponent.name).getMotionConfig()

        // put here code which updates the player game objects
        motionComponent.lastHorizontalVelocity = motionComponent.velocityX
        val lastVerticalVelocity = velocityY

        val wasRunningLeft = motionComponent.lastHorizontalVelocity < 0f
        val wasRunningRight = motionComponent.lastHorizontalVelocity > 0f
        val wasMovingHorizontally = abs(motionComponent.lastHorizontalVelocity) > 0.1f
        val wasFalling = lastVerticalVelocity < -0.1f
        val wasMovingUp = lastVerticalVelocity > 0f
        collisionComponent.wasInFrontOfWall = collisionComponent.isInFrontOfWall()

        // Save last state
        stateComponent.last = stateComponent.current

        if (!collisionComponent.isGrounded) {
           // check if falling speed exceeds a certain limit from which we show the falling anim
            stateComponent.current = if (wasFalling) {
                StateType.FALL
            } else {
                // wait for two frames until changing to jump anim in order to ignore when "isGrounded" gets "false"
                // for one frame only
                if (collisionComponent.wasGroundedLastFrame) {
                    StateType.STAND
                } else {
                    StateType.JUMP
                }
            }
        }

        // Enable next jump when player is grounded and joystick is pressed up again.
        // This prevents that the player sprite jumps around like crazy when joystick "Up" is keeping
        // pressed.
        if (collisionComponent.isGrounded && inputSystem.justUp) {
            collisionComponent.jumpVelocity = motionConfig.maxJumpVelocity
        }

        // Reset animation timer if the keys for walking right or left or squat down were just pressed in
        // order to play the animation from the beginning. Do this before squatDown is overwritten below.
        // So that the last squat state is taken into account here!
        // If the player is squat down then ignore here new left and right presses.
        // If the player is running left or right and squats down the squat down animation should start from
        // the beginning
        stateComponent.resetAnimFrameCounter = (!collisionComponent.squatDown && (inputSystem.justLeft || inputSystem.justRight || inputSystem.justDown))

        // Check if player should squat down
        if (inputSystem.down && collisionComponent.isGrounded) {
            collisionComponent.squatDown = true
            stateComponent.current = StateType.SQUAT
            // Slow down any horizontal movement
            velocityX = motionConfig.horizontalProgress.interpolate(motionComponent.lastHorizontalVelocity, 0f)
        } else {
            // Save last squat state
            collisionComponent.squatDown = false
        }
        // Check if player is not squat down and add velocity to player
        if (!inputSystem.down && collisionComponent.isGrounded) {
            // Process horizontal sprite movement
            if (inputSystem.right) {
                // Sprite moves to right direction
                velocityX = setHorizontalVelocity(motionComponent.lastHorizontalVelocity, motionConfig, wasRunningLeft, Geometry.RIGHT_DIRECTION)
                stateComponent.current = StateType.RUN
            } else if (inputSystem.left) {
                // Sprite moves to left direction
                velocityX = setHorizontalVelocity(motionComponent.lastHorizontalVelocity, motionConfig, wasRunningRight, Geometry.LEFT_DIRECTION)
                stateComponent.current = StateType.RUN
            } else if (collisionComponent.isGrounded) {
                // Sprite does not run
                stateComponent.current = StateType.STAND
            }
        } else if (!collisionComponent.isGrounded) {
            // This handles pressing joystick left or right while player is not grounded (that means player is jumping or falling)
            if (inputSystem.right) {
                // Sprite moves to right direction
                velocityX = setHorizontalVelocity(motionComponent.lastHorizontalVelocity, motionConfig, wasRunningLeft, Geometry.RIGHT_DIRECTION)
            } else if (inputSystem.left) {
                // Sprite moves to left direction
                velocityX = setHorizontalVelocity(motionComponent.lastHorizontalVelocity, motionConfig, wasRunningRight, Geometry.LEFT_DIRECTION)
            }
            // Reset animation timer when falling finished and player is grounded again
            stateComponent.resetAnimFrameCounter = wasFalling == true
        }
        // check if player is jumping
        if (inputSystem.up && !collisionComponent.isCollidingAbove) {
            // during jumping
            if (!wasFalling) {
                // use only a fraction of the initial jump velocity in every frame
                velocityY = collisionComponent.jumpVelocity * motionConfig.initJumpVelocityFactor
                // save the "rest" of the jump velocity for the next frame and truncate (end jump) it at some
                // arbitrary number
                collisionComponent.jumpVelocity -= velocityY
                // end jump when highest point is reached
                if (velocityY < motionConfig.endJumpVelocity) {
                    collisionComponent.jumpVelocity = 0f
                    velocityY = 0f
                }
            }
        } else {
            // Abort jumping only if Player Sprite is moving up
            // otherwise falling of the Player will be disrupted
            if (wasMovingUp || collisionComponent.isCollidingAbove) {
                collisionComponent.jumpVelocity = 0f
                velocityY = 0f
            }
        }

        // apply gravity before moving
        velocityY += motionConfig.gravity * deltaTime  // m/s² * s = m/s

        // truncate vertical velocity when falling
        if (velocityY < motionConfig.maxFallingVelocity) {
            velocityY = motionConfig.maxFallingVelocity
        }

        // Finally check the attack status and adapt the state accordingly
        if (inputSystem.attack) {
            // Check if player is attacking
            when (stateComponent.current) {
                StateType.STAND, StateType.IDLE -> stateComponent.current = StateType.STAND_ATTACK
                StateType.JUMP -> stateComponent.current = StateType.JUMP_ATTACK
                StateType.FALL -> stateComponent.current = StateType.FALL_ATTACK
                StateType.SQUAT -> {
                    stateComponent.current = if (inputSystem.attackIndex > Geometry.DIRECTION_DIAGONAL_RIGHT_DOWN) StateType.ON_FLOOR_ATTACK
                    else StateType.SQUAT_ATTACK
                }
                StateType.RUN -> stateComponent.current = StateType.RUN_ATTACK
                StateType.RUN_ATTACK, StateType.STAND_ATTACK, StateType.JUMP_ATTACK -> { /* accepted */
                }
                else -> println("ERROR: PlayerMoveSystem - Unknown state \"${stateComponent.current}\" on attack!")
            }
        } else if ((stateComponent.last == StateType.RUN_ATTACK || stateComponent.last == StateType.RUN_HIT)
            && stateComponent.current == StateType.RUN && !inputSystem.attack
// TODO            && !(app.animationHandler.isAnimationKeyframeInRunAttackState(gameObject) && gameObject.animData.directionIndex == Geometry.DIRECTION_RIGHT)
            ) {
            // Keep state run_attack after attack button release until the gun is in a position
            // where the animation switch over from still to running looks good
            // i.e. wait until direction is "right" and frame counter has reached a key frame
            stateComponent.current = StateType.RUN_ATTACK
        } else if (stateComponent.last == StateType.IDLE && stateComponent.current == StateType.STAND) {
            // Check if player was in idle mode and set that state again if he is still standing
            stateComponent.current = StateType.IDLE
        } else if (stateComponent.last != StateType.STAND_HIT && stateComponent.current == StateType.STAND) {
            // Switch to idle state after 50 cycles standing
// TODO            switchState(gameObject, 50, false, StateType.IDLE)
        }

        // Let the collision handler check and possibly truncate horizontal and vertical movement
        // of the player sprite according to collisions with walls, etc.
        motionComponent.velocityX = velocityX
        motionComponent.velocityY = -velocityY  // invert Y velocity because the Y axis is inverted in the grid system
    }

    private fun setHorizontalVelocity(lastHorizontalVelocity: Float, motionConfig: MotionConfig, wasRunningInOppositeDirection: Boolean, direction: Int): Float {
        if (wasRunningInOppositeDirection) {
            // Do immediate turnaround of the player
            return direction * motionConfig.maxHorizontalVelocity
        } else {
            // Apply smooth starting of movement from standstill
            return motionConfig.horizontalProgress.interpolate(lastHorizontalVelocity, direction * motionConfig.maxHorizontalVelocity)
//            return direction * motionConfig.maxHorizontalVelocity
        }
    }

    /*
        // Switch to specified state after given amount of cycles this game object was in a state in a row
        // and optional after the animation has finished.
        // Since there is no Animation Controller this function is in the base class.
        protected fun switchState(gameObject: AnimationObject, cycles: Int, checkAnimationFinished: Boolean, newState: StateType) {
            if (gameObject.data.lastCycleCounter == app.cycleCounter - 1) {
                // This function was called already last cycle
                if (app.cycleCounter - gameObject.data.startCycleCounter > cycles &&
                    // Take "animation has finished" into account if it should also be checked
                    (!checkAnimationFinished || app.animationHandler.isAnimationFinished(gameObject))) {
                    // Update the root state and mark the subState for do not use
                    gameObject.data.state = newState
                    gameObject.animData.animationFrameCounter = 0
                }
            } else {
                // First invoke of this function after some cycles, so save start cycle value
                gameObject.data.startCycleCounter = app.cycleCounter
            }
            // This function was called this state, so save last state cycle counter value for next time
            gameObject.data.lastCycleCounter = app.cycleCounter
        }
    */
}
