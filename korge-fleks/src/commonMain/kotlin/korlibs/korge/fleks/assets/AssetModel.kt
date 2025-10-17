package korlibs.korge.fleks.assets

import korlibs.korge.fleks.assets.data.ParallaxConfig
import kotlinx.serialization.*


/**
 * Asset model contains run time configuration for loading assets for the game.
 * This config can be also loaded from YAML files.
 *
 * Hint: Make sure to use only basic types (Integer, String, Boolean).
 */
@Serializable @SerialName("AssetModel")
data class AssetModel(
    val folder: String = "",
    val hotReloading: Boolean = false,

    val sounds: List<SoundConfig> = listOf(),
    val textureAtlas: List<TextureConfig> = listOf(),
    val tileMaps: List<TileMapConfig> = listOf()
) {

    @Serializable
    @SerialName("soundConfig")
    data class SoundConfig(
        val name: String,
        val fileName: String
    )

    @Serializable
    @SerialName("TileMapConfig")
    data class TileMapConfig(
        val name: String,
        val fileName: String,
        val collisionLayerName: String = "",  // Default is empty string - no collision layer or specific layer name for a tile map
    )

    @Serializable
    @SerialName("TextureConfig")
    data class TextureConfig(
        val fileName: String,
        val frameDurations: Map<String, FrameDurationConfig> = mapOf(),
        val nineSlices: Map<String, NineSlice> = mapOf(),
        val fonts: List<String> = listOf(),
        val parallaxBackgrounds: Map<String, ParallaxConfig> = mapOf(),
        val tilesets: List<TilesetConfig> = listOf()
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
        @SerialName("TilesetConfig")
        data class TilesetConfig(
            val name: String,
            val size: Int
        )
    }
}
