package korlibs.korge.fleks.assets.data

import korlibs.image.bitmap.BmpSlice


data class ParallaxPlaneTextures(
    val selfSpeed: Float = 0f,  // TODO not used yet
    val lineTextures: MutableList<LineTexture> = mutableListOf(),
    val topAttachedLayerTextures: MutableList<LineTexture> = mutableListOf(),
    val bottomAttachedLayerTextures: MutableList<LineTexture> = mutableListOf()
) {
    data class LineTexture(
        val index: Int,
        val bmpSlice: BmpSlice,
        val speedFactor: Float
    )
}
