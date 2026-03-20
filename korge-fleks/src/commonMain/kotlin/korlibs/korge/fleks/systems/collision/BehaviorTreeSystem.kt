package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.image.format.ImageAnimation.Direction.*
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.assets.data.gameObject.CollisionRect
import korlibs.korge.fleks.components.BehaviorTree.Companion.BehaviorTreeComponent
import korlibs.korge.fleks.components.Collision.Companion.CollisionComponent
import korlibs.korge.fleks.components.EntityRefsByName.Companion.EntityRefsByNameComponent
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.Sprite.Companion.SpriteComponent
import korlibs.korge.fleks.state.*
import korlibs.korge.fleks.utils.AppConfig
import korlibs.math.interpolation.interpolate
import korlibs.math.interpolation.toRatio
import kotlin.math.sign


class BehaviorTreeSystem : IteratingSystem(
    family = World.family { all(BehaviorTreeComponent) },
    interval = Fixed(1 / 60f)
) {
    private val assetStore = world.inject<AssetStore>("AssetStore")
    private val inputState = world.inject<PlayerInputState>("InputState")


    override fun onTickEntity(entity: Entity) {
        val bTreeComponent = entity[BehaviorTreeComponent]
        // Tick the behavior tree
//        val behaviorTree = behaviorTreeFactory.getBTree(bTreeComponent.characterConfig)
        val behaviorTree = PlayerBTree(assetStore, inputState).btree
        behaviorTree.run { world.tick(entity) }
    }
}

class PlayerBTree(assetStore: AssetStore, input: PlayerInputState) {

    private val gravity = -20f * AppConfig.WORLD_TO_PIXEL_RATIO              // defined m/s², converted to full-HD pixel / s²
    private val maxHorizontalVelocity = 6f * AppConfig.WORLD_TO_PIXEL_RATIO  // defined m/s
    private val maxVerticalVelocity = 0f * AppConfig.WORLD_TO_PIXEL_RATIO    // not used by player character controller
    private val maxJumpEnergy = 360f * AppConfig.WORLD_TO_PIXEL_RATIO        // Factor describing how high an object can jump up
    private val maxFallingVelocity = -15f * AppConfig.WORLD_TO_PIXEL_RATIO
    private val endJumpVelocity = 1f * AppConfig.WORLD_TO_PIXEL_RATIO
    private val horizontalProgress = 0.08f.toRatio()
    private val initJumpVelocityFactor = 0.1f

    private val collisionData =
        CollisionRect(
            -8, -28, 17f, 29f
            //-17, -28, 35f, 29f
            //0, 0, 17f, 29f
        )

    private val collisionDataSquat =
        CollisionRect(
            -8, -18, 17f, 19f  // TODO check if height is OK
        )


    val jumpStartAction = ActionNode { entity ->
        val collision = entity[CollisionComponent]
        val motion = entity[MotionComponent]
        val playerBodySpriteComponent = entity[EntityRefsByNameComponent].getSubEntity("player_body")[SpriteComponent]
        val playerLegsSpriteComponent = entity[EntityRefsByNameComponent].getSubEntity("player_legs")[SpriteComponent]

        // Check if we can start jumping: must be grounded and just started pressing up
        if (!(collision.isGrounded && input.justUp))
            return@ActionNode BTStatus.Failure

        // Enable jump by setting the jump velocity to the maximum value from motion config
        collision.jumpEnergy = maxJumpEnergy
        motion.velocityY =
            -collision.jumpEnergy * initJumpVelocityFactor  // store inverted again for grid system
//            println("jumpStartAction: jumpVel: ${collision.jumpVelocity} velocityY: ${motion.velocityY}")

        // Flip sprites according to horizontal input
        if (input.justLeft) {
            playerBodySpriteComponent.flipX = true
            playerLegsSpriteComponent.flipX = true
        } else if (input.justRight) {
            playerBodySpriteComponent.flipX = false
            playerLegsSpriteComponent.flipX = false
        }

        playerBodySpriteComponent.setAnimation("player_jobe_body_stand", assetStore = assetStore)
        playerLegsSpriteComponent.setAnimation("player_jobe_legs_jump", assetStore = assetStore)

        println("jumpStartAction")
        BTStatus.Success
    }

    val runStartAction = ActionNode { entity ->
        val collision = entity[CollisionComponent]
        val playerBodySpriteComponent = entity[EntityRefsByNameComponent].getSubEntity("player_body")[SpriteComponent]
        val playerLegsSpriteComponent = entity[EntityRefsByNameComponent].getSubEntity("player_legs")[SpriteComponent]
        
        // First check if we can start running from standstill: must be grounded, not pressing up or down, and just started pressing left or right
        if (!((collision.isGrounded && !input.up && !input.down && (input.justRight || input.justLeft))
            // OR if we just landed from falling and kept pressing up
            || (collision.isGrounded && !collision.wasGroundedLastFrame && input.up && (input.right || input.left))
            || (collision.isGrounded && input.up && (input.justRight || input.justLeft))
            // OR if we were squatting and just released down while kept pressing left or right
            || (collision.isGrounded && input.justReleasedDown && (input.right || input.left))
            // OR if we were falling and just got grounded while kept pressing left or right
            || (collision.isGrounded && !collision.wasGroundedLastFrame && (input.right || input.left))
        )) return@ActionNode BTStatus.Failure

        // Flip sprites according to horizontal input
        if (input.justLeft) {
            playerBodySpriteComponent.flipX = true
            playerLegsSpriteComponent.flipX = true
        } else if (input.justRight) {
            playerBodySpriteComponent.flipX = false
            playerLegsSpriteComponent.flipX = false
        }
        playerBodySpriteComponent.setAnimation("player_jobe_body_run", true, assetStore = assetStore)
        playerLegsSpriteComponent.setAnimation("player_jobe_legs_run", true, assetStore = assetStore)

        // DEBUG output traces
        val fromStandstill = (collision.isGrounded && !input.up && !input.down && (input.justRight || input.justLeft))
        val fromFallingWithUp = (collision.isGrounded && input.up && (input.right || input.left))
        val fromSquatting = (collision.isGrounded && input.justReleasedDown && (input.right || input.left))
        val fromFalling = (collision.isGrounded && !collision.wasGroundedLastFrame && (input.right || input.left))
        val fromStateString = when {
            fromStandstill -> "standstill"
            fromFallingWithUp -> "falling (up pressed)"
            fromSquatting -> "squatting"
            fromFalling -> "falling"
            else -> "unknown"
        }
        println("runStartAction <- $fromStateString")

        BTStatus.Success
    }

    val jumpingAction = ActionNode { entity ->
        val collision = entity[CollisionComponent]
        val motion = entity[MotionComponent]
        val playerBodySpriteComponent = entity[EntityRefsByNameComponent].getSubEntity("player_body")[SpriteComponent]
        val playerLegsSpriteComponent = entity[EntityRefsByNameComponent].getSubEntity("player_legs")[SpriteComponent]

        // Check if we can continue jumping: must not be grounded and still have jump energy left, pressing up and not colliding above
        if (!(!collision.isGrounded && collision.jumpEnergy > 0f))
            return@ActionNode BTStatus.Failure

        // Flip sprites according to horizontal input
        if (input.justLeft) {
            playerBodySpriteComponent.flipX = true
            playerLegsSpriteComponent.flipX = true
        } else if (input.justRight) {
            playerBodySpriteComponent.flipX = false
            playerLegsSpriteComponent.flipX = false
        }

        if (input.up && !collision.isCollidingAbove) {
            // Calculate jump velocity for this frame by using a fraction of the remaining jump energy and decrease jumpEnergy by that amount
            val velY = collision.jumpEnergy * initJumpVelocityFactor
            collision.jumpEnergy -= velY
            motion.velocityY = -velY
            //                println("jumpingAction: jumpVel: ${collision.jumpVelocity} velocityY: ${motion.velocityY}")

            BTStatus.Running
        } else {
            // Jumping is aborted or finished: reset jump velocity and vertical velocity
            collision.jumpEnergy = 0f
            motion.velocityY = 0f
            //                println("jumpingAction: jumpVel: ${collision.jumpVelocity} velocityY: ${motion.velocityY}")

            playerLegsSpriteComponent.setAnimation("player_jobe_legs_fall", assetStore = assetStore)

            BTStatus.Success
        }
    }

    val runningAction = ActionNode { entity ->
        val collision = entity[CollisionComponent]
        val motion = entity[MotionComponent]

        // Check if we can continue running: must be grounded, not pressing up or down, and pressing left or right
        if (!((collision.isGrounded && !(input.up || input.down) && (input.right || input.left))
                // OR if we are grounded, were jumping while kept pressing up and left or right
                || (collision.isGrounded && input.up && (input.right || input.left))
                )
        ) return@ActionNode BTStatus.Failure

        val direction = sign(motion.velocityX)
        motion.velocityX = horizontalProgress.interpolate(
            motion.velocityX,
            direction * maxHorizontalVelocity
        )

        // DEBUG output traces
        val fromStandstill = (collision.isGrounded && !(input.up || input.down) && (input.right || input.left))
        val fromFallingWithUp = (collision.isGrounded && input.up && (input.right || input.left))
        when {
            fromStandstill -> {} //println("runningAction <- standstill")
            fromFallingWithUp -> println("runningAction <- falling (up pressed)")
            else -> println("ERROR: runningAction <- unknown")
        }

        BTStatus.Success
    }

    val fallingAction = ActionNode { entity ->
        val collision = entity[CollisionComponent]
        val playerBodySpriteComponent = entity[EntityRefsByNameComponent].getSubEntity("player_body")[SpriteComponent]
        val playerLegsSpriteComponent = entity[EntityRefsByNameComponent].getSubEntity("player_legs")[SpriteComponent]

        // Check if we are falling: must not be grounded and have no jump energy left
        if (!(!collision.isGrounded && collision.jumpEnergy == 0f))
            return@ActionNode BTStatus.Failure

        // Flip sprites according to horizontal input
        if (input.justLeft) {
            playerBodySpriteComponent.flipX = true
            playerLegsSpriteComponent.flipX = true
        } else if (input.justRight) {
            playerBodySpriteComponent.flipX = false
            playerLegsSpriteComponent.flipX = false
        }

        playerBodySpriteComponent.setAnimation("player_jobe_body_stand", assetStore = assetStore)
        playerLegsSpriteComponent.setAnimation("player_jobe_legs_fall", assetStore = assetStore)

        BTStatus.Success
    }

    val squatAction = ActionNode { entity ->
        val collision = entity[CollisionComponent]
        val motion = entity[MotionComponent]
        val playerBodySpriteComponent = entity[EntityRefsByNameComponent].getSubEntity("player_body")[SpriteComponent]
        val playerLegsSpriteComponent = entity[EntityRefsByNameComponent].getSubEntity("player_legs")[SpriteComponent]

        // Check if we can squat: must be grounded and pressing down
        if (!(collision.isGrounded && input.down))
            return@ActionNode BTStatus.Failure

        collision.rect = collisionDataSquat

        // Flip sprites according to horizontal input
        if (input.justLeft) {
            playerBodySpriteComponent.flipX = true
            playerLegsSpriteComponent.flipX = true
        } else if (input.justRight) {
            playerBodySpriteComponent.flipX = false
            playerLegsSpriteComponent.flipX = false
        }

        if (input.justDown || !collision.wasGroundedLastFrame) {
            // Start squatting: trigger squat animation
            playerBodySpriteComponent.setAnimation("player_jobe_body_stand", assetStore = assetStore)
            playerLegsSpriteComponent.setAnimation("player_jobe_legs_squat", true, ONCE_FORWARD, assetStore = assetStore)
            BTStatus.Success
        } else {  // is input.down
            // Continue squatting: Slow down horizontal movement by interpolating towards 0
            val lastH = motion.velocityX
            motion.velocityX = horizontalProgress.interpolate(lastH, 0f)

            BTStatus.Running
        }
    }

    val idleAction = ActionNode { entity ->
        val collisionComponent = entity[CollisionComponent]
        val playerBodySpriteComponent = entity[EntityRefsByNameComponent].getSubEntity("player_body")[SpriteComponent]
        val playerLegsSpriteComponent = entity[EntityRefsByNameComponent].getSubEntity("player_legs")[SpriteComponent]

        collisionComponent.rect = collisionData
        playerBodySpriteComponent.setAnimation("player_jobe_body_stand", assetStore = assetStore)
        playerLegsSpriteComponent.setAnimation("player_jobe_legs_stand", assetStore = assetStore)

        BTStatus.Success
    }


    val btree = Selector(listOf(
        jumpStartAction,
        jumpingAction,
        fallingAction,
        runStartAction,
        runningAction,
        squatAction,
        idleAction
    ))
}
