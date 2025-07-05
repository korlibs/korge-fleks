package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import korlibs.korge.fleks.components.Info.Companion.InfoComponent
import korlibs.korge.fleks.utils.Pool


/**
 * Sanity check system which checks the world for empty entities.
 * This is most likely a bug in the game code, but it can also happen that entities are created
 * without any components or tags. In that case please check the game config files for errors.
 *
 * This system is not needed in production, but it is useful for debugging and testing.
 */
class HealthMonitorSystem(
    timesPerSecond: Int = 1
) : IntervalSystem(
    // Do some sanity checks periodically every second
    interval = Fixed(step = 1f / timesPerSecond.toFloat())
) {
    override fun onTick() {
        // Health check for empty entities
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

        // Health check component and object pool usage
        Pool.listOfAllPools.forEach { (name, pool) ->
            if (pool.itemsInPool > pool.totalGeneratedItems) {
                println("ERROR: Sanity check - pool '$name' has more items in pool (${pool.itemsInPool}) than total generated items (${pool.totalGeneratedItems}).")
            }

            // Consistency check for total items in use
            if (pool.totalItemsInUse != pool.totalGeneratedItemsInUse) {
                println("ERROR: Consistency check - pool '$name' has total items in use (${pool.totalItemsInUse}) different from total generated items in use (${pool.totalGeneratedItemsInUse}).")
            }
        }
    }
}
