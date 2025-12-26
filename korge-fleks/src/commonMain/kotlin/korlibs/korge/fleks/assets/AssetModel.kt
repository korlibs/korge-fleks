package korlibs.korge.fleks.assets

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
        val tilesets: List<TilesetConfig> = listOf()
    ) {
        @Serializable
        @SerialName("TilesetConfig")
        data class TilesetConfig(
            val name: String,
            val size: Int
        )
    }
}
