package korlibs.korge.fleks.assets.data


data class ParallaxLayerTexture(
    val spriteFrames: SpriteFrames,

    val repeatX: Boolean = false,
    val repeatY: Boolean = false,
    val speedFactor: Float? = null,  // It this is null than no movement is applied to the layer
    val selfSpeedX: Float = 0f,
    val selfSpeedY: Float = 0f
) {

}
