package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.SoundComponent
import korlibs.korge.fleks.utils.SnapshotSerializer

class SaveGameSystem(
    private val serializer: SnapshotSerializer = World.inject<SnapshotSerializer>("SnapshotSerializer")
) : IteratingSystem(
    family { all(SoundComponent) },
    interval = Fixed(step = 0.5f)
) {
    override fun onTickEntity(entity: Entity) {

    }
}


