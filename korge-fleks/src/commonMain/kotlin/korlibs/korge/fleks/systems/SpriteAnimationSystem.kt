package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.image.format.ImageAnimation.Direction.*
import korlibs.korge.assetmanager.*
import korlibs.korge.fleks.components.*
import korlibs.math.*
import korlibs.time.*

class SpriteAnimationSystem : IteratingSystem(
    family = family { all(SpriteComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val spriteComponent = entity[SpriteComponent]

        val imageAnimation = AssetStore.getImageAnimation(spriteComponent.name, spriteComponent.animation)

        if (spriteComponent.running) {
//            comp.time += deltaTime
            spriteComponent.nextFrameIn -= deltaTime
            if (spriteComponent.nextFrameIn <= 0f) {
//                println("increment: ${comp.increment}, frameIndex: ${comp.frameIndex} was shown for: ${comp.time}")
                val numframes = imageAnimation.frames.size
                spriteComponent.frameIndex = (spriteComponent.frameIndex + spriteComponent.increment) umod numframes

                // Check if animation should be played only once
                if (spriteComponent.increment == 0) {
                    spriteComponent.running = false
                    if (spriteComponent.destroyOnAnimationFinished) { world -= entity }
//                    println("delete at time: ${comp.time}")
                }

                val frame = imageAnimation.frames[spriteComponent.frameIndex]  // frameIndex is within list bounds
                spriteComponent.nextFrameIn = frame.duration.seconds.toFloat()
                spriteComponent.increment = when (spriteComponent.direction) {
                    FORWARD -> +1
                    REVERSE -> -1
                    PING_PONG -> if (spriteComponent.frameIndex + spriteComponent.increment !in 0 until numframes)
                        -spriteComponent.increment else spriteComponent.increment
                    ONCE_FORWARD -> if (spriteComponent.frameIndex < numframes - 1) +1 else 0
                    ONCE_REVERSE -> if (spriteComponent.frameIndex == 0) 0 else -1
                    null -> {
                        println("WARNING -- SpriteAnimationSystem: direction in SpriteAnimationComponent shall not be null!")
                        0
                    }
                }
            }
        }
    }
}
