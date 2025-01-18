package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*


class CameraSystem(
    worldToPixelRatio: Float
) : IteratingSystem(
    family = family { all(CameraFollowTag) },
    interval = EachFrame
) {
    private val worldToPixelRatioInv = 1f / worldToPixelRatio
    private val factor = 0.05f

    override fun onTickEntity(entity: Entity) {

        // Set camera position to entity with "CameraFollowTag" component
        val followPosition = entity[PositionComponent]

        val camera: Entity = world.getMainCamera()

        val cameraPosition = camera[PositionComponent]
        val xDiff = followPosition.x - cameraPosition.x
        val yDiff = followPosition.y - cameraPosition.y
        cameraPosition.x += xDiff * factor
        cameraPosition.y += yDiff * factor

        val parallaxFamily = world.family { all(ParallaxComponent, MotionComponent) }
        parallaxFamily.forEach { parallaxEntity ->
            val motion = parallaxEntity[MotionComponent]

            // Convert pixel distance of camera movement in the level to velocity for parallax layers
            val distanceInWorldUnits = (xDiff * factor) * worldToPixelRatioInv  // (distance in pixel) / (world to pixel ratio)
            motion.velocityX = -distanceInWorldUnits / deltaTime  // world units per delta-time

            // TODO: Move parallax layer also vertically
        }
    }
}
