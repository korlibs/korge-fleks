package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.utils.random
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.entity.EntityFactory

/**
 * This system is responsible to spawn new entity objects. It shall be the only system which spawns new objects.
 * It can be configured to periodically spawn entities until a total number of spawned objects is reached, or
 * it can also spawn an unlimited number of entities (run forever until it dies).
 */
class SpawnerSystem : IteratingSystem(
    family { all(SpawnerComponent) },
    // Make this fixed to not generate too much objects
    interval = Fixed(1f / 60f)
) {
    override fun onTickEntity(entity: Entity) {
        val spawner = entity[SpawnerComponent]
        if (spawner.interval > 0) {
            if (spawner.nextSpawnIn <= 0) {
                var x = 0f
                var y = 0f
                var setPosition = false
                entity.getOrNull(PositionComponent)?.let {
                    x = it.x + it.offsetX
                    y = it.y + it.offsetY
                    setPosition = true
                }
                entity.getOrNull(OffsetByFrameIndexComponent)?.let {
                    // Get offset depending on current animation and frame index
// TODO refactor because this component will be deleted - offsets are part of static entity config
                    val currentFrameIndex = 0  //(KorgeViewCache[it.entity] as ImageDataViewEx).currentFrameIndex
                    val animationName = it.entity.getOrNull(SpriteComponent)?.animation ?: ""
                    val offset = it.mapOfOffsetLists[animationName]?.get(currentFrameIndex)
                        ?: error("SpawnerSystem: Cannot get offset by frame index (entity: ${entity.id}, animationName: '$animationName', currentFrameIndex: $currentFrameIndex)")
                    x += offset.x
                    y += offset.y
                    setPosition = true
                }

                for (i in 0 until spawner.numberOfObjects) {
                    var xx = x
                    var yy = y
                    val newEntity =
                        if (spawner.newEntity == Entity.NONE) world.entity {}  // create new entity
                        else spawner.newEntity  // use given entity
                    if (spawner.positionVariation != 0f) {
                        xx = x + (-spawner.positionVariation..spawner.positionVariation).random()
                        yy = y + (-spawner.positionVariation..spawner.positionVariation).random()
                    }
                    // Directly set position
                    if (setPosition) newEntity.configure {
                        it += PositionComponent(x = xx, y = yy)
                    }

                    // Call the configured spawner function for configuring new objects
                    EntityFactory.createEntity(spawner.entityConfig, world, newEntity)
                }

                spawner.numberOfObjectsSpawned += spawner.numberOfObjects
                spawner.nextSpawnIn = spawner.interval
                if (spawner.timeVariation != 0) spawner.nextSpawnIn += (-spawner.timeVariation..spawner.timeVariation).random()
            } else {
                spawner.nextSpawnIn--
            }
        }
        if (spawner.totalNumberOfObjects > 0 && spawner.numberOfObjectsSpawned >= spawner.totalNumberOfObjects) entity.configure {
            entity -= SpawnerComponent
        }
    }
}
