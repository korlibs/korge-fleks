package samples.fleks.systems

import com.github.quillraven.fleks.EachFrame
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import samples.fleks.components.*
import samples.fleks.entities.createExplosionArtefact

/**
 * This system controls the "destruction" of an entity (game object).
 *
 */
class DestructSystem : IteratingSystem(
    family { all(Position, Destruct) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val destruct = entity[Destruct]
        if (destruct.triggerDestruction) {
            val position = entity[Position]
            // The spawning of explosion objects is hardcoded here to 40 objects - TODO that should be put into some component config later
            for (i in 0 until 40) {
                world.createExplosionArtefact(position, destruct)
            }
            // now destroy entity
            world -= entity
        }
    }
}
