package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.EachFrame
import com.github.quillraven.fleks.IntervalSystem


class ChunkEntitySpawnerSystem(

) : IntervalSystem(
    // same interval as the game object move/position system
    interval = EachFrame
) {
    override fun onTick() {
        TODO("Not yet implemented")
    }
}