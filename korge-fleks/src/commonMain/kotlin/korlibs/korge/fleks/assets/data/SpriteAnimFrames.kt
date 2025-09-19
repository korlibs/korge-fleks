package korlibs.korge.fleks.assets.data

import korlibs.image.bitmap.BmpSlice
import korlibs.math.umod
import korlibs.time.FastDuration


data class SpriteFrame(
    val bmpSlice: BmpSlice,
    val targetX: Int = 0,  // offset from the top-left corner of the original sprite if cropped
    val targetY: Int = 0,
    val spriteWidth: Int = 0,  // virtual size of the sprite (can be different from bmpSlice.width if cropped)
    val spriteHeight: Int = 0,
    // TODO get duration from aseprite or from some config file
    // Duration in milliseconds
    val duration: FastDuration = FastDuration(60.0)
)

class SpriteAnimFrames(
    val sprites: MutableList<SpriteFrame> = mutableListOf()
) : Collection<SpriteFrame> by sprites {
    val spriteStackSize: Int get() = sprites.size
    val firstSprite: BmpSlice get() = sprites[0].bmpSlice
    fun getSprite(index: Int): BmpSlice = sprites[index umod sprites.size].bmpSlice
    fun getDuration(index: Int): FastDuration = sprites[index umod sprites.size].duration
    operator fun get(index: Int) = getSprite(index)
}