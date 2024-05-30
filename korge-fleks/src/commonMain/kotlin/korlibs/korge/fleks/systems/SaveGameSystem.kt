package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.utils.SnapshotSerializer

class SaveGameSystem : IteratingSystem(
    family { all(PositionComponent) },  // TODO change this
    interval = Fixed(step = 0.5f)
) {
    override fun onTickEntity(entity: Entity) {

    }
}
