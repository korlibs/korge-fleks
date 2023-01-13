package samples.fleks.systems

import com.github.quillraven.fleks.*
import samples.fleks.components.*
import samples.fleks.entities.createMeteoriteObject

class SpawnerSystem : IteratingSystem(
    World.family { all(Spawner) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val spawner = entity[Spawner]
        if (spawner.interval > 0) {
            if (spawner.nextSpawnIn <= 0) {
                spawn(entity)
                spawner.nextSpawnIn = spawner.interval
                if (spawner.timeVariation != 0) spawner.nextSpawnIn += (-spawner.timeVariation..spawner.timeVariation).random()
            } else {
                spawner.nextSpawnIn--
            }
        }
    }

    private fun spawn(entity: Entity) {
        val spawnerPosition = entity[Position]
        val spawner = entity[Spawner]
        for (i in 0 until spawner.numberOfObjects) {
            world.createMeteoriteObject(spawnerPosition, spawner)
        }
    }
}
