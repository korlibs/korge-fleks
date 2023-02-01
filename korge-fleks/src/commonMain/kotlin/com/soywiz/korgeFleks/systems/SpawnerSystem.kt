package com.soywiz.korgeFleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.soywiz.korgeFleks.components.*
import com.soywiz.korgeFleks.entity.config.EntityConfigFunctions
import com.soywiz.korgeFleks.entity.config.isNullEntity
import com.soywiz.korgeFleks.utils.ImageAnimView
import com.soywiz.korgeFleks.utils.KorgeViewCache
import com.soywiz.korgeFleks.utils.random

/**
 * This system is responsible to spawn new entity objects. It shall be the only system which spawns new objects.
 * It can be configured to periodically spawn entities until a total number of spawned objects is reached, or
 * it can also spawn an unlimited number of entities (run forever until it dies).
 */
class SpawnerSystem(
    private val entityConfigFunctions: EntityConfigFunctions = inject(),
    private val korgeViewCache: KorgeViewCache = inject("normalViewCache")
) : IteratingSystem(
    family { all(Spawner) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val spawner = entity[Spawner]
        if (spawner.interval > 0) {
            if (spawner.nextSpawnIn <= 0) {
                var x = 0.0
                var y = 0.0
                entity.getOrNull(PositionShape)?.let {
                    x = it.x
                    y = it.y
                }
                entity.getOrNull(Offset)?.let {
                    x += it.x
                    y += it.y
                }
                entity.getOrNull(OffsetByFrameIndex)?.let {
                    // Get offset depending on current animation and frame index
                    val currentFrameIndex = (korgeViewCache[it.entity] as ImageAnimView).currentFrameIndex
                    val animationName = it.entity.getOrNull(Sprite)?.animationName ?: ""
                    val offset = it.list[animationName]?.get(currentFrameIndex) ?: error("SpawnerSystem: Cannot get offset by frame index (entity: ${entity.id}, animationName: '$animationName', currentFrameIndex: $currentFrameIndex)")
                    x += offset.x
                    y += offset.y
                }

                for (i in 0 until spawner.numberOfObjects) {
                    val newEntity = if (isNullEntity(spawner.newEntity)) world.entity()  // create new entity
                    else spawner.newEntity  // use given entity
                    // Call the configured spawner function for configuring new objects
                    if (spawner.positionVariation != 0.0) {
                        entityConfigFunctions[spawner.configureFunction].invoke(world, newEntity,
                            // TODO cleanup code below
                            x + (-spawner.positionVariation..spawner.positionVariation).random(),
                            y + (-spawner.positionVariation..spawner.positionVariation).random(),
                            spawner.config)
                    } else {
                        entityConfigFunctions[spawner.configureFunction].invoke(world, newEntity, x, y, spawner.config)
                    }
                }

                spawner.numberOfObjectsSpawned += spawner.numberOfObjects
                spawner.nextSpawnIn = spawner.interval
                if (spawner.timeVariation != 0) spawner.nextSpawnIn += (-spawner.timeVariation..spawner.timeVariation).random()
            } else {
                spawner.nextSpawnIn--
            }
        }
        if (spawner.totalNumberOfObjects > 0 && spawner.numberOfObjectsSpawned >= spawner.totalNumberOfObjects) entity.configure {
            entity -= Spawner
        }
    }
}
