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
        val spawnerComponent = entity[SpawnerComponent]
        if (spawnerComponent.interval > 0) {
            if (spawnerComponent.nextSpawnIn <= 0) {
                var x = 0f
                var y = 0f
// DEBUGGING
//                var spx = 0f
//                var spy = 0f
                entity.getOrNull(PositionComponent)?.let { component ->
                    x = component.x + component.offsetX
                    y = component.y + component.offsetY
// DEBUGGING
//                    spx = it.x + it.offsetX
//                    spy = it.y + it.offsetY
                }

                entity.getOrNull(OffsetByFrameIndexComponent)?.let { component ->
                    // Get offset depending on current animation and frame index
                    val spriteComponent = component.entity.getOrNull(SpriteComponent)
                    val currentFrameIndex = spriteComponent?.frameIndex ?: 0
                    val animationName = spriteComponent?.animation ?: ""
                    val offset = component.mapOfOffsetLists[animationName]?.points[currentFrameIndex]
                        ?: error("SpawnerSystem: Cannot get offset by frame index (entity: ${entity.id}, animationName: '$animationName', currentFrameIndex: $currentFrameIndex)")
                    x += offset.x
                    y += offset.y
                }

// DEBUGGING
//                if (spawnerComponent.entityConfig.contains("wing_dust")) {
//                    if (entity hasNo OffsetByFrameIndexComponent) {
//                        println("SpawnerSystem: Entity '${entity.id}' with config '${spawnerComponent.entityConfig}' is missing OffsetByFrameIndexComponent. ")
//                    } else {
//                        val spaceshipEntity = entity[OffsetByFrameIndexComponent].entity
//                        if (spaceshipEntity == Entity.NONE) {
//                            println("SpawnerSystem: Entity '${entity.id}' with config '${spawnerComponent.entityConfig}' has no spaceship entity assigned in OffsetByFrameIndexComponent.")
//                        }
//                        val spriteComponent = spaceshipEntity.getOrNull(SpriteComponent)
//                        if (spaceshipEntity.hasNo(SpriteComponent)) {
//                            println("SpawnerSystem: Entity '${entity.id}' with config '${spawnerComponent.entityConfig}' has no SpriteComponent assigned to the spaceship entity '${spaceshipEntity.id}'.")
//                        } else {
//                            val spriteComponent = spaceshipEntity[SpriteComponent]
//                            val currentFrameIndex = spriteComponent.frameIndex
//                            val animationName = spriteComponent.animation
//                            val offset = entity[OffsetByFrameIndexComponent].mapOfOffsetLists[animationName]?.points[currentFrameIndex]
//                                ?: error("TESTING SpawnerSystem: Cannot get offset by frame index (entity: ${entity.id}, animationName: '$animationName', currentFrameIndex: $currentFrameIndex)")
//                            if (offset.x == 0f || offset.y == 0f) {
//                                println("SpawnerSystem: Entity '${entity.id}' with config '${spawnerComponent.entityConfig}' has no offset for animation '$animationName' and frame index $currentFrameIndex.")
//                            }
//                        }
//                    }
//
//                }

                for (i in 0 until spawnerComponent.numberOfObjects) {
                    var xx = x
                    var yy = y
                    val newEntity =
                        if (spawnerComponent.newEntity == Entity.NONE) world.createEntity("SpawnerSystem: ${spawnerComponent.entityConfig}") {}  // create new entity
                        else spawnerComponent.newEntity  // use given entity
                    if (spawnerComponent.positionVariation != 0f) {
                        xx += (-spawnerComponent.positionVariation..spawnerComponent.positionVariation).random()
                        yy += (-spawnerComponent.positionVariation..spawnerComponent.positionVariation).random()
                    }
                    // Directly set position
                    newEntity.configure {
                        it += positionComponent {
                            this.x = xx
                            this.y = yy
                        }
                    }

                    // Call the configured spawner function for configuring new objects
                    world.configureEntity(spawnerComponent.entityConfig, newEntity)

// DEBUGGING
//                    println("Spawned entity: ${newEntity.id} with config: '${spawnerComponent.entityConfig}'")
//                    world.traceEntitySnapshot(newEntity)
                }

                spawnerComponent.numberOfObjectsSpawned += spawnerComponent.numberOfObjects
                spawnerComponent.nextSpawnIn = spawnerComponent.interval
                if (spawnerComponent.timeVariation != 0) spawnerComponent.nextSpawnIn += (-spawnerComponent.timeVariation..spawnerComponent.timeVariation).random()
            } else {
                spawnerComponent.nextSpawnIn--
            }
        }
        if (spawnerComponent.totalNumberOfObjects > 0 && spawnerComponent.numberOfObjectsSpawned >= spawnerComponent.totalNumberOfObjects) entity.configure {
            entity -= SpawnerComponent
        }
    }
}
