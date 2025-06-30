package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import korlibs.korge.fleks.components.Info.Companion.InfoComponent


/**
 * Sanity check system which checks the world for empty entities.
 * This is most likely a bug in the game code, but it can also happen that entities are created
 * without any components or tags. In that case please check the game config files for errors.
 *
 * This system is not needed in production, but it is useful for debugging and testing.
 */
class SanityCheckSystem(
    timesPerSecond: Int = 1
) : IntervalSystem(
    interval = Fixed(step = 1f / timesPerSecond.toFloat())
) {
    override fun onTick() {
        // Do some sanity checks periodically every second or so
        world.forEach { entity ->
            val snapshot = world.snapshotOf(entity)
            if (snapshot.tags.isEmpty() && (snapshot.components.isEmpty() || snapshot.components.size == 1 && entity has InfoComponent )) {
                if (entity has InfoComponent)
                    println("ERROR: Sanity check - found empty entity '${entity.id}' (v${entity.version}) with name '${entity[InfoComponent].name}'.")
                else
                    println("ERROR: Sanity check - found empty entity '${entity.id}' (v${entity.version}) with no tags and no components.")
                // Remove empty entity
                world -= entity
            }
        }
    }
}
