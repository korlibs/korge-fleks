package korlibs.korge.fleks.assets

import korlibs.korge.fleks.assets.data.ParallaxConfig
import kotlinx.serialization.*


/**
 * Asset model contains run time configuration for loading assets for the game.
 * This config could be also loaded later from YAML files.
 *
 * Hint: Make sure to use only basic types (Integer, String, Boolean).
 */
@Serializable @SerialName("AssetModel")
data class AssetModel(
    val folder: String = "",
    val hotReloading: Boolean = false,
    val sounds: Map<String, String> = mapOf(),
    val textureAtlas: List<TextureConfig> = listOf()
) {

    @Serializable
    @SerialName("TextureConfig")
    data class TextureConfig(
        val fileName: String,
        val frameDurations: Map<String, FrameDurationConfig> = mapOf(),
        val nineSlices: Map<String, NineSlice> = mapOf(),
        val fonts: List<String> = listOf(),
        val parallaxBackgrounds: Map<String, ParallaxConfig> = mapOf(),
        val tileMaps: Map<String, TileMapConfig> = mapOf()
    ) {
        @Serializable
        @SerialName("FrameDurationConfig")
        data class FrameDurationConfig(
            val default: Int = 0,          // default duration in milliseconds for all frames of the animation
            val custom: List<Int>? = null  // [optional] custom frame duration in milliseconds for each frame of the animation
        )

        @Serializable
        @SerialName("NineSlice")
        data class NineSlice(
            val x: Int,
            val y: Int,
            val width: Int,
            val height: Int
        )

        @Serializable
        @SerialName("TileMapConfig")
        data class TileMapConfig(
            val fileName: String,
            val collisionLayerName: String = "",  // Default is empty string - no collision layer or specific layer name for a tile map
        )
    }
}
