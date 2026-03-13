package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.assets.data.gameObject.MotionConfig
import korlibs.korge.fleks.components.Collision
import korlibs.korge.fleks.components.Collision.Companion.CollisionComponent
import korlibs.korge.fleks.components.Motion
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.State
import korlibs.korge.fleks.components.State.Companion.StateComponent
import korlibs.korge.fleks.components.data.StateType
import korlibs.korge.fleks.state.PlayerInputState
import korlibs.korge.fleks.utils.Geometry
import korlibs.math.interpolation.interpolate


// ---------- Lightweight Behavior Tree primitives (kept local to this file) ----------

enum class BTStatus { Success, Failure, Running }

interface BTNode {
    fun tick(bb: Blackboard): BTStatus
}

class Sequence(private val children: List<BTNode>) : BTNode {
    override fun tick(bb: Blackboard): BTStatus {
        for (child in children) {
            when (child.tick(bb)) {
                BTStatus.Success -> continue
                BTStatus.Failure -> return BTStatus.Failure
                BTStatus.Running -> return BTStatus.Running
            }
        }
        return BTStatus.Success
    }
}

class Selector(private val children: List<BTNode>) : BTNode {
    override fun tick(bb: Blackboard): BTStatus {
        for (child in children) {
            when (child.tick(bb)) {
                BTStatus.Success -> return BTStatus.Success
                BTStatus.Running -> return BTStatus.Running
                BTStatus.Failure -> continue
            }
        }
        return BTStatus.Failure
    }
}

class ConditionNode(private val cond: (Blackboard) -> Boolean) : BTNode {
    override fun tick(bb: Blackboard): BTStatus = if (cond(bb)) BTStatus.Success else BTStatus.Failure
}

class ActionNode(private val action: (Blackboard) -> BTStatus) : BTNode {
    override fun tick(bb: Blackboard): BTStatus = action(bb)
}

// ---------- Blackboard holding references for the BT ----------

class Blackboard(
    val entity: Entity,
    val deltaTime: Float,
    val collision: Collision,
    val motion: Motion,
    val state: State,
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

class PlayerMoveSystem : IteratingSystem(
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

        // Prepare blackboard
        val motionConfig = assetStore.getGameObjectStateConfig(stateComponent.name).getMotionConfig()
        val bb = Blackboard(
            entity = entity,
            deltaTime = deltaTime, // provided by IteratingSystem
            collision = collisionComponent,
            motion = motionComponent,
            state = stateComponent,
            input = inputState,
            motionConfig = motionConfig
        )

        // initialize temporary values similar to previous implementation
        bb.lastHorizontalVelocity = motionComponent.velocityX
        bb.lastVerticalVelocity = -motionComponent.velocityY // invert Y axis as in original code

        // Save last state
        stateComponent.last = stateComponent.current

        // Update collision helper flags used by original logic
        collisionComponent.wasInFrontOfWall = collisionComponent.isInFrontOfWall()

        // Tick the behavior tree
        playerTree.tick(bb)

        // After BT run, write computed velocities back (BT nodes may have set them)
        motionComponent.lastHorizontalVelocity = motionComponent.velocityX
        motionComponent.velocityX = bb.computedVelocityX
        motionComponent.velocityY = -bb.computedVelocityY // store inverted again for grid system
    }

    private fun buildPlayerTree(): BTNode {
        // Sequence of high-level actions that roughly correspond to the original algorithm

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
            if (collision.isGrounded && input.justUp) {
                collision.jumpVelocity = bb.motionConfig.maxJumpVelocity
            }

            // Reset animation frame counter when appropriate (as originally)
            state.resetAnimFrameCounter = (!collision.squatDown && (input.justLeft || input.justRight || input.justDown))

            BTStatus.Success
        }

        // 2) Squat handling (if down and grounded)
        val squatCondition = ConditionNode { bb -> bb.input.down && bb.collision.isGrounded }
        val squatAction = ActionNode { bb ->
            bb.collision.squatDown = true
            bb.state.current = StateType.SQUAT
            // Slow down horizontal movement by interpolating towards 0
            val lastH = bb.lastHorizontalVelocity
            bb.computedVelocityX = bb.motionConfig.horizontalProgress.interpolate(lastH, 0f)
            BTStatus.Success
        }

        val squatSeq = Sequence(listOf(squatCondition, squatAction))

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
                    velX = setHorizontalVelocity(lastH, mc, lastH < 0f, Geometry.RIGHT_DIRECTION)
                    bb.state.current = StateType.RUN
                } else if (input.left) {
                    velX = setHorizontalVelocity(lastH, mc, lastH > 0f, Geometry.LEFT_DIRECTION)
                    bb.state.current = StateType.RUN
                } else if (collision.isGrounded) {
                    bb.state.current = StateType.STAND
                }
            } else if (!collision.isGrounded) {
                // allow some horizontal control in air
                if (input.right) {
                    velX = setHorizontalVelocity(lastH, mc, lastH < 0f, Geometry.RIGHT_DIRECTION)
                } else if (input.left) {
                    velX = setHorizontalVelocity(lastH, mc, lastH > 0f, Geometry.LEFT_DIRECTION)
                }
                // reset animation timer when falling finished and grounded again
                bb.state.resetAnimFrameCounter = (bb.lastVerticalVelocity < -0.1f) == true
            }

            bb.computedVelocityX = velX
            BTStatus.Success
        }

        // 4) Jump handling
        val jumpAction = ActionNode { bb ->
            val input = bb.input
            val collision = bb.collision
            val mc = bb.motionConfig

            var velY = bb.lastVerticalVelocity

            if (input.up && !collision.isCollidingAbove) {
                if (!(bb.lastVerticalVelocity < -0.1f)) {
                    // use only a fraction of the initial jump velocity in every frame
                    velY = collision.jumpVelocity * mc.initJumpVelocityFactor
                    collision.jumpVelocity -= velY
                    if (velY < mc.endJumpVelocity) {
                        collision.jumpVelocity = 0f
                        velY = 0f
                    }
                }
            } else {
                // Abort jumping only if moving up or collision above
                if (bb.lastVerticalVelocity > 0f || collision.isCollidingAbove) {
                    collision.jumpVelocity = 0f
                    velY = 0f
                }
            }

            bb.computedVelocityY = velY
            BTStatus.Success
        }

        // 5) Gravity + clamp vertical velocity and final adjustments including attack state
        val finalizeAction = ActionNode { bb ->
            val mc = bb.motionConfig
            val input = bb.input
            val state = bb.state

            // apply gravity
            var vy = bb.computedVelocityY + mc.gravity * bb.deltaTime

            // truncate vertical velocity when falling
            if (vy < mc.maxFallingVelocity) vy = mc.maxFallingVelocity

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
                jumpAction,
                finalizeAction
            )
        )
    }

    private fun setHorizontalVelocity(lastHorizontalVelocity: Float, motionConfig: MotionConfig, wasRunningInOppositeDirection: Boolean, direction: Int): Float {
        return if (wasRunningInOppositeDirection) {
            // immediate turnaround
            direction * motionConfig.maxHorizontalVelocity
        } else {
            motionConfig.horizontalProgress.interpolate(lastHorizontalVelocity, direction * motionConfig.maxHorizontalVelocity)
        }
    }
}
