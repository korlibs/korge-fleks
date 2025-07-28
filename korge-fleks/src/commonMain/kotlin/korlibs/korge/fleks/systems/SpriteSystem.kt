package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.image.format.ImageAnimation.Direction.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.Sprite.Companion.SpriteComponent
import korlibs.korge.fleks.utils.deleteViaLifeCycle
import korlibs.math.*
import korlibs.time.*


class SpriteSystem : IteratingSystem(
    family = family { all(SpriteComponent) },
    interval = EachFrame
) {
    private val assetStore: AssetStore = world.inject(name = "AssetStore")

    override fun onTickEntity(entity: Entity) {
        val spriteComponent = entity[SpriteComponent]

        // Update sprite offsets
        // TODO
        //   1. move offset lists from OffsetByFrameIndexComponent to static config
        //   2. do instead:
        //        Update offsetX and offsetY in PositionComponent depending on frame index

        // Update sprite animation
        if (spriteComponent.running) {
//            comp.time += deltaTime
            spriteComponent.nextFrameIn -= deltaTime
            if (spriteComponent.nextFrameIn <= 0f) {
//                println("increment: ${comp.increment}, frameIndex: ${comp.frameIndex} was shown for: ${comp.time}")
                val imageAnimation = assetStore.getImageAnimation(spriteComponent.name, spriteComponent.animation)
                val numFrames = imageAnimation.frames.size
                spriteComponent.frameIndex = (spriteComponent.frameIndex + spriteComponent.increment) umod numFrames


                val frame = imageAnimation.frames[spriteComponent.frameIndex]  // frameIndex is within list bounds
                spriteComponent.nextFrameIn = frame.duration.seconds.toFloat()
                spriteComponent.increment = when (spriteComponent.direction) {
                    FORWARD -> +1
                    REVERSE -> -1
                    PING_PONG -> if (spriteComponent.frameIndex + spriteComponent.increment !in 0 until numFrames)
                        -spriteComponent.increment else spriteComponent.increment
                    ONCE_FORWARD -> if (spriteComponent.frameIndex < numFrames - 1) +1 else 0
                    ONCE_REVERSE -> if (spriteComponent.frameIndex == 0) 0 else -1
                    null -> {
                        println("WARNING -- SpriteAnimationSystem: direction in SpriteAnimationComponent shall not be null!")
                        0
                    }
                }

                // Check if animation should be played only once and if we need to delete the entity afterward
                if (spriteComponent.increment == 0) {
                    spriteComponent.running = false
                    if (spriteComponent.destroyOnAnimationFinished) {
                        world.deleteViaLifeCycle(entity)
//                        world -= entity
                    }
                }

            }
        }
    }
}
