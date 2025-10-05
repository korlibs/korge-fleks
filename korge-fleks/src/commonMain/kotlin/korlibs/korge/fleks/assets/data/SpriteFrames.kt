package korlibs.korge.fleks.assets.data

import korlibs.image.bitmap.BmpSlice


data class SpriteFrame(
    val bmpSlice: BmpSlice,
    val targetX: Int = 0,  // offset from the top-left corner of the original sprite if cropped
    val targetY: Int = 0,
    // Duration in seconds will be set later after all frames have been loaded from texture atlas
    var duration: Float = 0f
)

class SpriteFrames(
    private val frames: MutableList<SpriteFrame> = mutableListOf(),
    val width: Int = 0,  // virtual size of the sprite (can be different from bmpSlice.width if cropped)
    val height: Int = 0
) : MutableList<SpriteFrame> by frames {
    val numberOfFrames: Int get() = frames.size
    val firstFrame: BmpSlice get() = frames.first().bmpSlice

    override fun get(index: Int): SpriteFrame =
        if (index in 0..<size) frames[index]
        else {
            println("ERROR: SpriteAnimFrames.get($index) - index out of range [0..${size - 1}]")
            frames[0]
        }

    fun getTexture(index: Int): BmpSlice =
        if (index in 0..<size) frames[index].bmpSlice
        else {
            println("ERROR: SpriteAnimFrames.getSpriteTexture($index) - index out of range [0..${size - 1}]")
            frames[0].bmpSlice
        }
    fun getDuration(index: Int): Float =
        if (index in 0..<size) frames[index].duration
        else {
            println("ERROR: SpriteAnimFrames.getDuration($index) - index out of range [0..${size - 1}]")
            frames[0].duration
        }

}
