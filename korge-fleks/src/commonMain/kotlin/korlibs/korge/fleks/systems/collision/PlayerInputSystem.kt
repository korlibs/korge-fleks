package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.PlayerInput.Companion.PlayerInputComponent

class PlayerInputSystem() : IteratingSystem(
    family = World.family { all(PlayerInputComponent, MotionComponent) },
    interval = Fixed(1 / 60f)
) {

    var isPressedKeyA: Boolean = false
    var isPressedKeyS: Boolean = false
    var isPressedKeyD: Boolean = false
    var isPressedKeyW: Boolean = false

    override fun onTickEntity(entity: Entity) {
        val playerInputComponent = entity[PlayerInputComponent]
        val motionComponent = entity[MotionComponent]

        playerInputComponent.xMoveStrength = 0f
        playerInputComponent.yMoveStrength = 0f

        if (isPressedKeyA) {
            playerInputComponent.xMoveStrength = -1f
        }
        if (isPressedKeyD) {
            playerInputComponent.xMoveStrength = 1f
        }
        if (isPressedKeyW) {
            playerInputComponent.yMoveStrength = -1f
        }
        if (isPressedKeyS) {
            playerInputComponent.yMoveStrength = 1f
        }
    }
}