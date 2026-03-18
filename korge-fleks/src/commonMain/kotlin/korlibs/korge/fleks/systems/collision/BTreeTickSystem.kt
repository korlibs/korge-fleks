package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.image.format.ImageAnimation.Direction.*
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.assets.data.gameObject.MotionConfig
import korlibs.korge.fleks.components.Collision
import korlibs.korge.fleks.components.Collision.Companion.CollisionComponent
import korlibs.korge.fleks.components.EntityRefsByName.Companion.EntityRefsByNameComponent
import korlibs.korge.fleks.components.Motion
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.Sprite
import korlibs.korge.fleks.components.Sprite.Companion.SpriteComponent
import korlibs.korge.fleks.components.State
import korlibs.korge.fleks.components.State.Companion.StateComponent
import korlibs.korge.fleks.components.data.StateType
import korlibs.korge.fleks.state.*
import korlibs.korge.fleks.utils.Geometry
import korlibs.math.interpolation.interpolate
import kotlin.math.sign


// ---------- Blackboard holding references for the BT ----------

class Blackboard(
    val entity: Entity,
    val deltaTime: Float,
    val collision: Collision,
    val motion: Motion,
    val state: State,
    val playerBodySpriteComponent: Sprite,
    val playerLegsSpriteComponent: Sprite,
    val input: PlayerInputState,
    val motionConfig: MotionConfig
) {
    // temporary working values computed during the tree
    var computedVelocityX: Float = 0f
    var computedVelocityY: Float = 0f
    var lastVerticalVelocity: Float = 0f
    var lastHorizontalVelocity: Float = 0f
}


// ---------- PlayerMoveSystem rewritten to use a small Hybrid BT ----------

class BTreeTickSystem : IteratingSystem(
    family = World.family { all(MotionComponent, CollisionComponent, StateComponent) },
    interval = Fixed(1 / 60f)
) {
    private val assetStore = world.inject<AssetStore>("AssetStore")
    private val inputState = world.inject<PlayerInputState>("InputState")

    // The behavior tree prototype (shared) - created once
    private val playerTree: BTNode = buildPlayerTree()

    override fun onTickEntity(entity: Entity) {
        val collisionComponent = entity[CollisionComponent]
        val motionComponent = entity[MotionComponent]
        val stateComponent = entity[StateComponent]
        val entityRefsByName = entity[EntityRefsByNameComponent]

        // Prepare blackboard
        val motionConfig = assetStore.getGameObjectStateConfig(stateComponent.name).getMotionConfig()
        val bb = Blackboard(
            entity = entity,
            deltaTime = deltaTime, // provided by IteratingSystem
            collision = collisionComponent,
            motion = motionComponent,
            state = stateComponent,
            playerBodySpriteComponent = entityRefsByName.getSubEntity("player_body")[SpriteComponent],
            playerLegsSpriteComponent = entityRefsByName.getSubEntity("player_legs")[SpriteComponent],
            input = inputState,
            motionConfig = motionConfig
        )

        // initialize temporary values similar to previous implementation
//        bb.lastHorizontalVelocity = motionComponent.velocityX
//        bb.lastVerticalVelocity = -motionComponent.velocityY // invert Y axis as in original code

        // Save last state
//        stateComponent.last = stateComponent.current

        // Update collision helper flags used by original logic
//        collisionComponent.wasInFrontOfWall = collisionComponent.isInFrontOfWall()

        // Tick the behavior tree
        playerTree.tick(bb)

        // After BT run, write computed velocities back (BT nodes may have set them)
//        motionComponent.lastHorizontalVelocity = motionComponent.velocityX
//        motionComponent.velocityX = bb.computedVelocityX
//        motionComponent.velocityY = -bb.computedVelocityY // store inverted again for grid system
    }

    private fun buildPlayerTree(): BTNode {
        // Sequence of high-level actions that roughly correspond to the original algorithm

        // 1) Damage

        // 2) isGrounded
        val isGrounded = ConditionNode { bb -> bb.collision.isGrounded }
        val inputJustUp = ConditionNode { bb -> bb.input.justUp }
        val isJumping = ConditionNode { bb -> bb.collision.jumpVelocity > 0f }

        val runStartAction = ActionNode { bb ->
            if (!(bb.collision.isGrounded && bb.input.justRight || bb.input.justLeft)) return@ActionNode BTStatus.Failure

            bb.playerBodySpriteComponent.setAnimation("player_jobe_body_run", true, assetStore = assetStore)
            bb.playerLegsSpriteComponent.setAnimation("player_jobe_legs_run", true, assetStore = assetStore)

            BTStatus.Success
        }

        val horizontalMoveAction = ActionNode { bb ->
            if (!(bb.input.right || bb.input.left)) return@ActionNode BTStatus.Failure
            val wasRunningInOppositeDirection = (bb.input.right && bb.motion.velocityX < 0f) || (bb.input.left && bb.motion.velocityX > 0f)
            bb.motion.velocityX = setHorizontalVelocity(bb.motion.velocityX, bb.motionConfig, wasRunningInOppositeDirection)

//            bb.lastHorizontalVelocity = motionComponent.velocityX
//            if (input.right) {
//                velX = setHorizontalVelocity(lastH, mc, lastH < 0f, Geometry.RIGHT_DIRECTION)
//                bb.state.current = StateType.RUN
//            } else if (input.left) {
//                velX = setHorizontalVelocity(lastH, mc, lastH > 0f, Geometry.LEFT_DIRECTION)

            BTStatus.Running
        }

        val jumpStartAction = ActionNode { bb ->
            if (!bb.collision.isGrounded || !bb.input.justUp) return@ActionNode BTStatus.Failure

            // Enable jump by setting the jump velocity to the maximum value from motion config
            bb.collision.jumpVelocity = bb.motionConfig.maxJumpVelocity
            bb.motion.velocityY = -bb.collision.jumpVelocity * bb.motionConfig.initJumpVelocityFactor  // store inverted again for grid system
            println("jumpStartAction: jumpVel: ${bb.collision.jumpVelocity} velocityY: ${bb.motion.velocityY}")

            bb.playerBodySpriteComponent.setAnimation("player_jobe_body_stand", assetStore = assetStore)
            bb.playerLegsSpriteComponent.setAnimation("player_jobe_legs_jump", assetStore = assetStore)
            BTStatus.Success
        }

        val jumpingAction = ActionNode { bb ->
            if (bb.collision.isGrounded || bb.collision.jumpVelocity == 0f) return@ActionNode BTStatus.Failure

            if (bb.input.up && !bb.collision.isCollidingAbove) {
                val velY = bb.collision.jumpVelocity * bb.motionConfig.initJumpVelocityFactor
                bb.collision.jumpVelocity -= velY
                bb.motion.velocityY = -velY
                println("jumpingAction: jumpVel: ${bb.collision.jumpVelocity} velocityY: ${bb.motion.velocityY}")

                BTStatus.Running
            } else {
                bb.collision.jumpVelocity = 0f
                bb.motion.velocityY = 0f
                println("jumpingAction: jumpVel: ${bb.collision.jumpVelocity} velocityY: ${bb.motion.velocityY}")

                BTStatus.Success
            }
        }

        val fallingAction = ActionNode { bb ->
            if (bb.collision.isGrounded) return@ActionNode BTStatus.Failure

            bb.playerBodySpriteComponent.setAnimation("player_jobe_body_stand", assetStore = assetStore)
            bb.playerLegsSpriteComponent.setAnimation("player_jobe_legs_fall", assetStore = assetStore)
            BTStatus.Success
        }

        val squatAction = ActionNode { bb ->
            if (bb.input.justDown) {
                // Start squatting: trigger squat animation
                bb.playerBodySpriteComponent.setAnimation("player_jobe_body_stand", assetStore = assetStore)
                bb.playerLegsSpriteComponent.setAnimation("player_jobe_legs_squat", true, ONCE_FORWARD, assetStore = assetStore)

                BTStatus.Running
            } else if (bb.input.down) {
                // Continue squatting: Slow down horizontal movement by interpolating towards 0
                val lastH = bb.motion.velocityX
                bb.motion.velocityX = bb.motionConfig.horizontalProgress.interpolate(lastH, 0f)

                BTStatus.Running
            } else {
                // Stop squatting: reset flag and return failure to try other grounded actions
                BTStatus.Failure
            }
        }

        val idleAction = ActionNode { bb ->
            if (bb.input.right || bb.input.left) return@ActionNode BTStatus.Failure

            bb.playerBodySpriteComponent.setAnimation("player_jobe_body_stand", assetStore = assetStore)
            bb.playerLegsSpriteComponent.setAnimation("player_jobe_legs_stand", assetStore = assetStore)
            BTStatus.Success
        }

//        Selector {
//            Sequence {
//                Action {
//
//                }
//            }
//        }

        return Selector(listOf(
            runStartAction,
            horizontalMoveAction,
            jumpStartAction,
            jumpingAction,
            fallingAction,
            squatAction,
            idleAction
        ))

//        Selector(listOf(
//            Sequence(listOf(
//                isGrounded,
//                Selector(listOf(
//                    squatAction,
//                    jumpStartAction,
//                    // TODO runAction,
//                    idleAction
//                ))
//            )),
//            jumpAction,
//            fallingAction
//        ))

        // 1) Update grounded/falling/jump-availability and reset animation triggers
        val updateState = ActionNode { bb ->
            val collision = bb.collision
            val state = bb.state
            val input = bb.input

            // Determine vertical state
            val wasFalling = bb.lastVerticalVelocity < -0.1f
            if (!collision.isGrounded) {
                state.current = if (wasFalling) StateType.FALL
                else {
                    if (collision.wasGroundedLastFrame) StateType.STAND else StateType.JUMP
                }
            }

            // Enable next jump when grounded and joystick was just pressed up
//            if (collision.isGrounded && input.justUp) {
//                collision.jumpVelocity = bb.motionConfig.maxJumpVelocity
//            }

            // Reset animation frame counter when appropriate (as originally)
            state.resetAnimFrameCounter = (!collision.squatDown && (input.justLeft || input.justRight || input.justDown))

            BTStatus.Success
        }

        // 2) Squat handling (if down and grounded)
        val squatCondition = ConditionNode { bb -> bb.input.down && bb.collision.isGrounded }
        val squatAction2 = ActionNode { bb ->
//            bb.collision.squatDown = true
//            bb.state.current = StateType.SQUAT
            // Slow down horizontal movement by interpolating towards 0
//            val lastH = bb.lastHorizontalVelocity
//            bb.computedVelocityX = bb.motionConfig.horizontalProgress.interpolate(lastH, 0f)
            BTStatus.Success
        }

        val squatSeq = Sequence(listOf(squatCondition, squatAction2))

        // 3) Horizontal movement handling when not squatting
        val horizontalAction = ActionNode { bb ->
            val input = bb.input
            val collision = bb.collision
            val mc = bb.motionConfig
            val lastH = bb.lastHorizontalVelocity
            var velX = 0f

            // When grounded and not down, set state and velocities according to input
            if (!input.down && collision.isGrounded) {
                if (input.right) {
//                    velX = setHorizontalVelocity(lastH, mc, lastH < 0f, Geometry.RIGHT_DIRECTION)
                    bb.state.current = StateType.RUN
                } else if (input.left) {
//                    velX = setHorizontalVelocity(lastH, mc, lastH > 0f, Geometry.LEFT_DIRECTION)
                    bb.state.current = StateType.RUN
                } else if (collision.isGrounded) {
                    bb.state.current = StateType.STAND
                }
            } else if (!collision.isGrounded) {
                // allow some horizontal control in air
                if (input.right) {
//                    velX = setHorizontalVelocity(lastH, mc, lastH < 0f, Geometry.RIGHT_DIRECTION)
                } else if (input.left) {
//                    velX = setHorizontalVelocity(lastH, mc, lastH > 0f, Geometry.LEFT_DIRECTION)
                }
                // reset animation timer when falling finished and grounded again
                bb.state.resetAnimFrameCounter = (bb.lastVerticalVelocity < -0.1f) == true
            }

            bb.computedVelocityX = velX
            BTStatus.Success
        }

        // 4) Jump handling
        val jumpAction2 = ActionNode { bb ->
            val input = bb.input
            val collision = bb.collision
            val mc = bb.motionConfig

//            var velY = bb.lastVerticalVelocity
//
//            if (input.up && !collision.isCollidingAbove) {
//                if (!(bb.lastVerticalVelocity < -0.1f)) {
//                    // use only a fraction of the initial jump velocity in every frame
//                    velY = collision.jumpVelocity * mc.initJumpVelocityFactor
//                    collision.jumpVelocity -= velY
//                    if (velY < mc.endJumpVelocity) {
//                        collision.jumpVelocity = 0f
//                        velY = 0f
//                    }
//                }
//            } else {
//                // Abort jumping only if moving up or collision above
//                if (bb.lastVerticalVelocity > 0f || collision.isCollidingAbove) {
//                    collision.jumpVelocity = 0f
//                    velY = 0f
//                }
//            }
//
//            bb.computedVelocityY = velY
            BTStatus.Success
        }

        // 5) Gravity + clamp vertical velocity and final adjustments including attack state
        val finalizeAction = ActionNode { bb ->
            val mc = bb.motionConfig
            val input = bb.input
            val state = bb.state

            // apply gravity
            var vy = bb.computedVelocityY + mc.gravity * bb.deltaTime  // --> moved to GridMoveSystem

            // truncate vertical velocity when falling
            if (vy < mc.maxFallingVelocity) vy = mc.maxFallingVelocity  // --> moved to GridMoveSystem

            // handle attack states similar to original
            if (input.attack) {
                when (state.current) {
                    StateType.STAND, StateType.IDLE -> state.current = StateType.STAND_ATTACK
                    StateType.JUMP -> state.current = StateType.JUMP_ATTACK
                    StateType.FALL -> state.current = StateType.FALL_ATTACK
                    StateType.SQUAT -> {
                        state.current = if (input.attackIndex > Geometry.DIRECTION_DIAGONAL_RIGHT_DOWN) StateType.ON_FLOOR_ATTACK
                        else StateType.SQUAT_ATTACK
                    }
                    StateType.RUN -> state.current = StateType.RUN_ATTACK
                    StateType.RUN_ATTACK, StateType.STAND_ATTACK, StateType.JUMP_ATTACK -> { /* accepted */ }
                    else -> println("ERROR: PlayerMoveSystem(BT) - Unknown state \"${state.current}\" on attack!")
                }
            } else if ((state.last == StateType.RUN_ATTACK || state.last == StateType.RUN_HIT)
                && state.current == StateType.RUN && !input.attack) {
                state.current = StateType.RUN_ATTACK
            } else if (state.last == StateType.IDLE && state.current == StateType.STAND) {
                state.current = StateType.IDLE
            }

            // finalized computed velocities remain in bb.computedVelocityX and computedVelocityY
            bb.computedVelocityY = vy
            BTStatus.Success
        }

        // Compose the tree: updateState -> (squatSeq OR horizontalAction) -> jumpAction -> finalizeAction
        return Sequence(
            listOf(
                updateState,
                Selector(listOf(squatSeq, horizontalAction)),
                jumpAction2,
                finalizeAction
            )
        )
    }

    private fun setHorizontalVelocity(lastHorizontalVelocity: Float, motionConfig: MotionConfig, wasRunningInOppositeDirection: Boolean): Float {
        val direction = sign(lastHorizontalVelocity)
        return if (wasRunningInOppositeDirection) {
            // immediate turnaround
            direction * motionConfig.maxHorizontalVelocity
        } else {
            motionConfig.horizontalProgress.interpolate(lastHorizontalVelocity, direction * motionConfig.maxHorizontalVelocity)
        }
    }
}
