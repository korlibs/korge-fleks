package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.EachFrame
import com.github.quillraven.fleks.IntervalSystem


/**
 * A system which spawns entities from the level chunks depending on the current camera position.
 * This system needs to be invoked with the same interval as the [PositionSystem] which moves
 * the entities.
 */
class ChunkEntitySpawnerSystem(

) : IntervalSystem(
    // Same interval as the game object move/position system
    interval = EachFrame
) {
    override fun onTick() {
        TODO("Not yet implemented")
    }
}