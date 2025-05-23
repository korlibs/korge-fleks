package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.Spawner.Companion.SpawnerComponent
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


/**
 * This object prototype can be used to create objects which are moving and spawn a trail.
 *
 */
@Serializable @SerialName("MovedSpawnerObjectConfig")
data class MovedSpawnerObjectConfig(
    override val name: String,

    // SpawnerComponent for creating the fire trail
    var numberOfObjects: Int = 0,           // The spawner will generate this number of object when triggered after interval time
    var interval: Int = 0,                  // 0 - disabled, 1 - every frame, 2 - every second frame, 3 - every third frame,...
    var timeVariation: Int = 0,             // 0 - no variation, 1 - one frame variation, 2 - two frames variation, ...
    var positionVariation: Double = 0.0,    // variation radius where objects will be spawned - 0.0 = no variation
    var entityConfig: String = "",          // Name of entity config which configures the spawned entity object
    var totalNumberOfObjects: Int = -1,     // -1 - unlimited number of objects spawned, x = x-number of objects spawned in total

    // SpriteComponent
    // TODO give the meteorite an image

    // OffsetComponent
    val offsetX: Float = 0f,              // offset to pivot point of meteorite
    val offsetY: Float = 0f,

    // MotionComponent
    val velocityX: Float = 0f,
    val velocityY: Float = 0f,
    val velocityVariationX: Float = 0f,
    val velocityVariationY: Float = 0f
) : EntityConfig {

    // Configure function which applies the config to the entity's components
    override fun World.entityConfigure(entity: Entity) : Entity {
        // PositionComponent might already be set by SpawnerSystem and it already contains the position

        entity.configure {
//            entity.getOrAdd(OffsetComponent) { OffsetComponent() }.also {
//                it.x = spawnerConfig.offsetX
//                it.y = spawnerConfig.offsetY
//            }
            it.getOrAdd(MotionComponent) { MotionComponent {} }.also { motion ->
                var velocityXX = velocityX
                var velocityYY = velocityY
                if (velocityVariationX != 0f) {
                    velocityXX += (-velocityVariationX..velocityVariationX).random()
                }
                if (velocityVariationY != 0f) {
                    velocityYY += (-velocityVariationY..velocityVariationY).random()
                }
                motion.velocityX = velocityXX
                motion.velocityY = velocityYY
            }
            it.getOrAdd(SpawnerComponent) { SpawnerComponent {} }.also {

            }
//            entity.getOrAdd(SpriteComponent) { SpriteComponent() }.also {
//                it.assetName = spawnerConfig.assetName
//                it.animationName = spawnerConfig.animationName
//                it.isPlaying = true
//            }
//            entity.getOrAdd(DrawableComponent) { DrawableComponent() }.also {
//                it.drawOnLayer = spawnerConfig.drawOnLayer
//            }
//            entity.getOrAdd(AppearanceComponent) { AppearanceComponent() }
//            entity.getOrAdd(LifeCycleComponent) { LifeCycleComponent() }
        }
        return entity
/*
            // Spawner details for spawned objects (spawned objects do also spawn objects itself)
            spawnerNumberOfObjects = 5, // Enable spawning feature for spawned object
            spawnerInterval = 1,
            spawnerPositionVariationX = 5.0,
            spawnerPositionVariationY = 5.0,
            spawnerPositionAccelerationX = -30.0,
            spawnerPositionAccelerationY = -100.0,
            spawnerPositionAccelerationVariation = 15.0,
            spawnerSpriteImageData = "meteorite",  // "" - Disable sprite graphic for spawned object
            spawnerSpriteAnimation = "FireTrail",  // "FireTrail" - "TestNum"
            spawnerSpriteIsPlaying = true,
            // Set position details for spawned objects
            positionVariationX = 100.0,
            positionVariationY = 0.0,
            positionAccelerationX = 90.0,
            positionAccelerationY = 200.0,
            positionAccelerationVariation = 10.0,
            // Destruct info for spawned objects
            destruct = true

        var xx = position.x + spawner.positionX
        if (spawner.positionVariationX != 0.0) xx += (-spawner.positionVariationX..spawner.positionVariationX).random()
         var yy = position.y + spawner.positionY
        if (spawner.positionVariationY != 0.0) yy += (-spawner.positionVariationY..spawner.positionVariationY).random()
        var xAccel = spawner.positionAccelerationX
        var yAccel = spawner.positionAccelerationY
        if (spawner.positionAccelerationVariation != 0.0) {
            val variation = (-spawner.positionAccelerationVariation..spawner.positionAccelerationVariation).random()
            xAccel += variation
            yAccel += variation
        }

        it += Position(  // Position of spawner
            x = xx,
            y = yy,
            xAcceleration = xAccel,
            yAcceleration = yAccel
        )
        // Add spawner feature
        if (spawner.spawnerNumberOfObjects != 0) {
            it += Spawner(
                numberOfObjects = spawner.spawnerNumberOfObjects,
                interval = spawner.spawnerInterval,
                timeVariation = spawner.spawnerTimeVariation,
                // Position details for spawned objects
                positionX = spawner.spawnerPositionX,
                positionY = spawner.spawnerPositionY,
                positionVariationX = spawner.spawnerPositionVariationX,
                positionVariationY = spawner.spawnerPositionVariationY,
                positionAccelerationX = spawner.spawnerPositionAccelerationX,
                positionAccelerationY = spawner.spawnerPositionAccelerationY,
                positionAccelerationVariation = spawner.spawnerPositionAccelerationVariation,
                // Sprite animation details for spawned objects
                spriteImageData = spawner.spawnerSpriteImageData,
                spriteAnimation = spawner.spawnerSpriteAnimation,
                spriteIsPlaying = spawner.spawnerSpriteIsPlaying,
                spriteForwardDirection = spawner.spawnerSpriteForwardDirection,
                spriteLoop = spawner.spawnerSpriteLoop
            )
        }
        // Add sprite animations
        if (spawner.spriteImageData.isNotEmpty()) {
            it += Sprite(  // Config for spawned object
                imageData = spawner.spriteImageData,
                animation = spawner.spriteAnimation,
                isPlaying = spawner.spriteIsPlaying,
                forwardDirection = spawner.spriteForwardDirection,
                loop = spawner.spriteLoop
            )
        }
        // Add destruct details
        if (spawner.destruct) {
            it += Destruct(
                spawnExplosion = true,
                explosionParticleRange = 15.0,
                explosionParticleAcceleration = 300.0
            )
        }
*/
    }

    init {
        EntityFactory.register(this)
    }
}
