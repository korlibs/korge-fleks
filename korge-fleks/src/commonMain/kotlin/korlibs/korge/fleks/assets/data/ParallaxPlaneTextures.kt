package korlibs.korge.fleks.assets.data

import korlibs.image.bitmap.BmpSlice


data class ParallaxPlaneTextures(
    val selfSpeed: Float = 0f,
    val lineTextures: MutableList<LineTexture> = mutableListOf(),
    val topAttachedLayerTextures: MutableMap<String, LineTexture> = mutableMapOf(),
    val bottomAttachedLayerTextures: MutableMap<String, LineTexture> = mutableMapOf()
) {
    data class LineTexture(
        val index: Int,
        val bmpSlice: BmpSlice,
        val speedFactor: Float
    )
}
