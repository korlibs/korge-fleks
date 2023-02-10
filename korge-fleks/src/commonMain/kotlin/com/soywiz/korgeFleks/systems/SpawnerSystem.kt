package com.soywiz.korgeFleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.soywiz.korgeFleks.components.*
import com.soywiz.korgeFleks.entity.config.isNullEntity
import com.soywiz.korgeFleks.korlibsAdaptation.ImageAnimView
import com.soywiz.korgeFleks.utils.KorgeViewCache
import com.soywiz.korgeFleks.utils.random

/**
 * This system is responsible to spawn new entity objects. It shall be the only system which spawns new objects.
 * It can be configured to periodically spawn entities until a total number of spawned objects is reached, or
 * it can also spawn an unlimited number of entities (run forever until it dies).
 */
class SpawnerSystem(
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
                var setPosition = false
                entity.getOrNull(PositionShape)?.let {
                    x = it.x
                    y = it.y
                    setPosition = true
                }
                entity.getOrNull(Offset)?.let {
                    x += it.x
                    y += it.y
                    setPosition = true
                }
                entity.getOrNull(OffsetByFrameIndex)?.let {
                    // Get offset depending on current animation and frame index
                    val currentFrameIndex = (korgeViewCache[it.entity] as ImageAnimView).currentFrameIndex
                    val animationName = it.entity.getOrNull(Sprite)?.animationName ?: ""
                    val offset = it.list[animationName]?.get(currentFrameIndex)
                        ?: error("SpawnerSystem: Cannot get offset by frame index (entity: ${entity.id}, animationName: '$animationName', currentFrameIndex: $currentFrameIndex)")
                    x += offset.x
                    y += offset.y
                    setPosition = true
                }

                for (i in 0 until spawner.numberOfObjects) {
                    var xx = x
                    var yy = y
                    val newEntity =
                        if (isNullEntity(spawner.newEntity)) world.entity()  // create new entity
                        else spawner.newEntity  // use given entity
                    if (spawner.positionVariation != 0.0) {
                        xx = x + (-spawner.positionVariation..spawner.positionVariation).random()
                        yy = y + (-spawner.positionVariation..spawner.positionVariation).random()
                    }
                    // Directly set position
                    if (setPosition) newEntity.configure {
                        it += PositionShape(x = xx, y = yy)
                    }

                    // Call the configured spawner function for configuring new objects
                    spawner.configureFunction.invoke(world, newEntity)
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
