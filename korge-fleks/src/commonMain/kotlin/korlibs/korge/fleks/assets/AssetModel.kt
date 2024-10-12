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
    val sounds: Map<String, String> = mapOf(),
    val backgrounds: Map<String, ParallaxConfig> = mapOf(),
    val images: Map<String, ImageDataConfig> = mapOf(),
    val fonts: Map<String, String> = mapOf(),
    val tileMaps: List<TileMapConfig> = listOf()
) {
    @Serializable @SerialName("ImageDataConfig")
    data class ImageDataConfig(
        val fileName: String = "",
        val layers: String? = null
    )

    @Serializable @SerialName("TileMapConfig")
    data class TileMapConfig(
        val fileName: String,
        val levels: String? = null
    )
}

enum class AssetType { COMMON, WORLD, LEVEL, SPECIAL }
enum class TileMapType { LDTK, TILED }
