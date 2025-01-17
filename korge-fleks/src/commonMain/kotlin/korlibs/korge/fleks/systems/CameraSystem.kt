package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*


class CameraSystem : IteratingSystem(
    family = family { all(CameraFollowTag) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {

        // Set camera position to entity with "CameraFollowTag" component
        val followPosition = entity[PositionComponent]

        val camera: Entity = world.getMainCamera()

        val cameraPosition = camera[PositionComponent]
        val xDiff = followPosition.x - cameraPosition.x
        val yDiff = followPosition.y - cameraPosition.y
        cameraPosition.x += xDiff * 0.05f
        cameraPosition.y += yDiff * 0.05f


    }
}
