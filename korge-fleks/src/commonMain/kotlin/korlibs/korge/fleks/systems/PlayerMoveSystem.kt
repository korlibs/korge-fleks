package korlibs.korge.fleks.systems

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

        motionComponent.velocityX += playerInputComponent.speed * playerInputComponent.xMoveStrength
        motionComponent.velocityY += playerInputComponent.speed * playerInputComponent.yMoveStrength
    }
}
