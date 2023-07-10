package korlibs.korge.fleks.assets

import korlibs.korge.parallax.ParallaxConfig


/**
 * Asset model contains run time configuration for loading assets for the game.
 * This config will be loaded later from YAML file.
 *
 * Hint: Make sure to use only basic types (Integer, String, Boolean).
 */
data class AssetModel(
    val assetFolderName: String = "none",
    val sounds: Map<String, String> = mapOf(),
    val backgrounds: Map<String, ParallaxConfig> = mapOf(),
    val images: Map<String, ImageDataConfig> = mapOf(),
    val fonts: Map<String, String> = mapOf(),
    val tiledMaps: Map<String, String> = mapOf(),
    val entityConfigs: Map<String, ConfigBase> = mapOf()
) {
    data class ImageDataConfig(
        val fileName: String = "",
        val layers: String? = null
    )
}
