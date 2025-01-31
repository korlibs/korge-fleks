package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import korlibs.korge.fleks.assets.*
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

    private val assetStore: AssetStore = inject("AssetStore")

    private val worldHeight: Float = assetStore.getWorldHeight()
    private val worldWidth: Float = assetStore.getWorldWidth()

    var parallaxHeight: Float = 0f

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
            val position = parallaxEntity[PositionComponent]
            val viewPortHeight = camera[SizeIntComponent].height

            // Convert pixel distance of camera movement in the level to velocity for parallax layers
            val distanceInWorldUnits = (xDiff * factor) * worldToPixelRatioInv  // (distance in pixel) / (world to pixel ratio)
            motion.velocityX = -distanceInWorldUnits / deltaTime  // world units per delta-time

            // TODO: Move parallax layer also vertically
//            position.y =

            // We need the camera position in world coordinates
            val cameraVerticalPosition = cameraPosition.y  // TODO: add the position of the level in the world
            val worldActiveHeight = worldHeight //- viewPortHeight  ???
            val ratio = cameraVerticalPosition / worldActiveHeight   // [0...1]

            // Get the global position of the parallax layer in screen coordinates
            val parallaxVerticalMax = viewPortHeight - parallaxHeight
            val parallaxVerticalPosition = ratio * parallaxVerticalMax

            position.y = parallaxVerticalPosition
//            println("parallax y: $position.y")

            // parallax layer image height: 375


        }
    }
}
