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
    val backgrounds: Map<String, ParallaxConfig> = mapOf(),
    val images: Map<String, ImageDataConfig> = mapOf(),
    val fonts: Map<String, String> = mapOf(),
    val tileMaps: Map<String, TileMapConfig> = mapOf()
) {
    @Serializable @SerialName("ImageDataConfig")
    data class ImageDataConfig(
        val fileName: String = "",
        val layers: String? = null
    )

    @Serializable @SerialName("TileMapConfig")
    data class TileMapConfig(
        val fileName: String,
        val collisionLayerName: String = "",  // Default is empty string - no collision layer or specific layer name for a tile map

        // internally used
        val tileSetPaths: MutableList<String> = mutableListOf()
    )
}
