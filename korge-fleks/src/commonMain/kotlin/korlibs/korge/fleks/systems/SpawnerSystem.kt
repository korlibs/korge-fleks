package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.OffsetByFrameIndex.Companion.OffsetByFrameIndexComponent
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Position.Companion.positionComponent
import korlibs.korge.fleks.components.Spawner.Companion.SpawnerComponent
import korlibs.korge.fleks.components.Sprite.Companion.SpriteComponent
import korlibs.korge.fleks.utils.*


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
// DEBUGGING
//                var spx = 0f
//                var spy = 0f
                entity.getOrNull(PositionComponent)?.let {
                    x = it.x + it.offsetX
                    y = it.y + it.offsetY
// DEBUGGING
//                    spx = it.x + it.offsetX
//                    spy = it.y + it.offsetY
                }

                entity.getOrNull(OffsetByFrameIndexComponent)?.let {
                    // Get offset depending on current animation and frame index
                    val spriteComponent = it.entity.getOrNull(SpriteComponent)
                    val currentFrameIndex = spriteComponent?.frameIndex ?: 0
                    val animationName = spriteComponent?.animation ?: ""
                    val offset = it.mapOfOffsetLists[animationName]?.points[currentFrameIndex]
                        ?: error("SpawnerSystem: Cannot get offset by frame index (entity: ${entity.id}, animationName: '$animationName', currentFrameIndex: $currentFrameIndex)")
                    x += offset.x
                    y += offset.y
                }

                for (i in 0 until spawner.numberOfObjects) {
                    var xx = x
                    var yy = y
                    val newEntity =
                        if (spawner.newEntity == Entity.NONE) world.entity("spawned_$i") {}  // create new entity
                        else spawner.newEntity  // use given entity
                    if (spawner.positionVariation != 0f) {
                        xx += (-spawner.positionVariation..spawner.positionVariation).random()
                        yy += (-spawner.positionVariation..spawner.positionVariation).random()
                    }
                    // Directly set position
                    newEntity.configure {
                        it += positionComponent {
                            this.x = xx
                            this.y = yy
                        }
                    }

// DEBUGGING
//                    if (spawner.entityConfig == "introSpaceshipRightWingDust") println("Spawning: newEntity: $newEntity - spaceship pos: ($spx, $spy) - spawing pos: ($xx, $yy")
//                    if (spawner.entityConfig == "introSpaceshipLeftWingDust") println("Spawning: newEntity: $newEntity - spaceship pos: ($spx, $spy) - spawing pos: ($xx, $yy)")

                    // Call the configured spawner function for configuring new objects
                    world.configureEntity(spawner.entityConfig, newEntity)
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
