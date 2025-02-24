package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.*
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

    private val assetStore: AssetStore = inject<AssetStore>("AssetStore")

    // TODO how to get current world name?
    private val worldHeight: Float = assetStore.getWorldData("").height
    private val worldWidth: Float = assetStore.getWorldData("").width

    // These properties need to be set by the onAdd hook function of the ParallaxComponent
    var parallaxHeight: Float = 0f
    var parallaxOffset: Float = 0f

    override fun onTickEntity(entity: Entity) {

        // Set camera position to entity with "CameraFollowTag" component
        val followPosition = entity[PositionComponent]

        val camera: Entity = world.getMainCamera()

        val cameraPosition = camera[PositionComponent]
        val viewPortHalf = camera[SizeComponent]

        val lastCameraPosX = cameraPosition.x
        //val lastCameraPosY = cameraPosition.y

        // Calculate the difference between the camera and the entity to follow
        val xDiff = followPosition.x - cameraPosition.x
        val yDiff = followPosition.y - cameraPosition.y
        // Move the camera towards the entity to follow
        val newCameraPositionX = cameraPosition.x + xDiff * factor
        val newCameraPositionY = cameraPosition.y + yDiff * factor

        // Keep camera within world bounds (+1 tile in each direction as guard for shaking camera - via camera offset)
        val leftBound = viewPortHalf.width + worldToPixelRatio
        val rightBound = worldWidth - viewPortHalf.width - worldToPixelRatio
        val topBound = viewPortHalf.height + worldToPixelRatio
        val bottomBound = worldHeight - viewPortHalf.height - worldToPixelRatio
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

        val parallaxFamily = world.family { all(ParallaxComponent, MotionComponent) }
        parallaxFamily.forEach { parallaxEntity ->
            val motion = parallaxEntity[MotionComponent]
            val position = parallaxEntity[PositionComponent]
            val viewPortHeight = camera[SizeIntComponent].height

            // Convert pixel distance of camera movement in the level to horizontal velocity for parallax layers
            val distanceInWorldUnits = cameraDistX * worldToPixelRatioInv  // (distance in pixel) / (world to pixel ratio)
            motion.velocityX = -distanceInWorldUnits / deltaTime  // world units per delta-time

            // Calculate the ratio of the camera position in the world to the world height
            // Camera position is in world coordinates
            val ratio = (cameraPosition.y - viewPortHalf.height) / (worldHeight - viewPortHeight)   // range: [0...1]

            // Get the global position of the parallax layer in screen coordinates
            val parallaxVerticalLength = viewPortHeight - parallaxHeight
            val parallaxVerticalPosition = ratio * parallaxVerticalLength

            position.y = parallaxVerticalPosition - parallaxOffset
//            println("parallax y: $position.y")
        }
    }
}
