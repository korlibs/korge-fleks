package korlibs.korge.fleks.assets

import korlibs.image.format.ImageAnimation
import korlibs.image.format.ImageFrame
import korlibs.time.seconds
import kotlin.collections.contains



/**
 * Get animation frame for [frameIndex] out of [AssetStore].
 */
fun AssetStore.getImageFrame(name: String, animation: String? = null, frameIndex: Int = 0) : ImageFrame {
    val animationFrames = if (animation != null) {
        val spriteAnimations = getImageData(name).animationsByName
        if (spriteAnimations.contains(animation)) {
            spriteAnimations[animation]!!.frames
        } else {
            println("WARNING -- AssetStore: Image animation '$animation' not found!")
            return ImageFrame(0)
        }
    } else {
        getImageData(name).defaultAnimation.frames
    }
    return if (animationFrames.size > frameIndex) {
        animationFrames[frameIndex]
    } else {
        println("WARNING -- AssetStore: Image animation frame '$frameIndex' out of bounds!")
        ImageFrame(0)
    }
}
