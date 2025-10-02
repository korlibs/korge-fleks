package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.Parallax.Companion.ParallaxComponent
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*


class CameraSystem(
    private val worldToPixelRatio: Float
) : IteratingSystem(
    family = family { all(CameraFollowTag) },
    interval = EachFrame
) {
    private val worldToPixelRatioInv = 1f / worldToPixelRatio
    private val factor = 0.05f

    private val parallaxFamily = world.family { all(ParallaxComponent, MotionComponent) }

    // These properties need to be set by the entityConfigure function of the level map config
    var worldHeight: Float = 0f
    var worldWidth: Float = 0f

    // This property needs to be set by the onAdd hook function of the ParallaxComponent
    var parallaxHeight: Float = 0f

    override fun onTickEntity(entity: Entity) {

        // Set camera position to entity with "CameraFollowTag" component
        val followPosition = entity[PositionComponent]

        val camera: Entity = world.getMainCamera()
        val cameraPosition = camera[PositionComponent]

        val lastCameraPosX = cameraPosition.x
        //val lastCameraPosY = cameraPosition.y

        // Calculate the difference between the camera and the entity to follow
        val xDiff = followPosition.x - cameraPosition.x
        val yDiff = followPosition.y - cameraPosition.y
        // Move the camera towards the entity to follow
        val newCameraPositionX = cameraPosition.x + xDiff * factor
        val newCameraPositionY = cameraPosition.y + yDiff * factor

        // Keep camera within world bounds (+1 tile in each direction as guard for shaking camera - via camera offset)
        val leftBound = AppConfig.VIEW_PORT_WIDTH_HALF + worldToPixelRatio
        val rightBound = worldWidth - AppConfig.VIEW_PORT_WIDTH_HALF - worldToPixelRatio
        val topBound = AppConfig.VIEW_PORT_HEIGHT_HALF + worldToPixelRatio
        val bottomBound = worldHeight - AppConfig.VIEW_PORT_HEIGHT_HALF - worldToPixelRatio
        cameraPosition.x =
            if (newCameraPositionX < leftBound) leftBound
            else if (newCameraPositionX > rightBound) rightBound
            else newCameraPositionX
        cameraPosition.y =
            if (newCameraPositionY < topBound) topBound
            else if (newCameraPositionY > bottomBound) bottomBound
            else newCameraPositionY

        // Move parallax layers if camera moves
        val cameraDistX = cameraPosition.x - lastCameraPosX
        //val cameraDistY = cameraPosition.y - lastCameraPosY

        parallaxFamily.forEach { parallaxEntity ->
            val motion = parallaxEntity[MotionComponent]
            val position = parallaxEntity[PositionComponent]

            // Debugging parallax position at the end of the intro
            //println("parallax y: ${position.y}")

            // Convert pixel distance of camera movement in the level to horizontal velocity for parallax layers
            val distanceInWorldUnits = cameraDistX * worldToPixelRatioInv  // (distance in pixel) / (world to pixel ratio)
            motion.velocityX = -distanceInWorldUnits / deltaTime  // world units per delta-time

            // Calculate the ratio of the camera position in the world to the world height
            // Camera position is in world coordinates
            val ratio = (cameraPosition.y - AppConfig.VIEW_PORT_HEIGHT_HALF) / (worldHeight - AppConfig.VIEW_PORT_HEIGHT)   // range: [0...1]

            // Get the global position of the parallax layer in screen coordinates
            val parallaxVerticalLength = AppConfig.VIEW_PORT_HEIGHT - parallaxHeight
            position.y = ratio * parallaxVerticalLength
//            println("parallax y: $position.y")
        }
    }
}
