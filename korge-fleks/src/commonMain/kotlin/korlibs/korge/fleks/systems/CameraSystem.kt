package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*
import korlibs.math.geom.*


class CameraSystem : IteratingSystem(
    family = family { all(CameraFollowTag) },
    interval = EachFrame
) {
    private var camera: Entity = Entity.NONE  // Needs to be set after the camera entity was created after configuring the fleks world
    private val viewPortSize: SizeInt = inject("ViewPortSize")

    override fun onTickEntity(entity: Entity) {

        // Set camera position to entity with "CameraFollowTag" component
        val followPosition = entity[PositionComponent]
        val cameraPosition = camera[PositionComponent]
        val xDiff = followPosition.x - cameraPosition.x
        val yDiff = followPosition.y - cameraPosition.y
        cameraPosition.x += xDiff * 0.05f
        cameraPosition.y += yDiff * 0.05f


    }

    fun setCamera(cameraName: String) {
        this.camera = EntityByName.getOrNull(cameraName) ?: throw Error("ERROR: Camera entity with name $cameraName does not exist and cannot be set in PositionSystem!")
    }
}
