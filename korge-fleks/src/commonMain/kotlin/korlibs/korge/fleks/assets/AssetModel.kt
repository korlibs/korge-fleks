package korlibs.korge.fleks.assets

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
    val sounds: MutableMap<String, String> = mutableMapOf(),
    val backgrounds: MutableMap<String, ParallaxConfig> = mutableMapOf(),
    val images: MutableMap<String, ImageDataConfig> = mutableMapOf(),
    val fonts: MutableMap<String, String> = mutableMapOf(),
    val tileMaps: MutableMap<String, TileMapConfig> = mutableMapOf(),
) {
    @Serializable @SerialName("ImageDataConfig")
    data class ImageDataConfig(
        val fileName: String = "",
        val layers: String? = null
    )

    @Serializable @SerialName("TileMapConfig")
    data class TileMapConfig(
        val fileName: String,
        val type: TileMapType
    )
}

enum class AssetType { COMMON, WORLD, LEVEL, SPECIAL }
enum class TileMapType { LDTK, TILED }
