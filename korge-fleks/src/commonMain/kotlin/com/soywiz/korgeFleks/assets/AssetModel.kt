package com.soywiz.korgeFleks.assets

import com.soywiz.korgeFleks.entity.config.ParallaxConfig


/**
 * Asset model contains run time configuration for loading assets for the game.
 * This config will be loaded later from YAML file.
 *
 * Hint: Make sure to use only basic types (Integer, String, Boolean).
 */
data class AssetModel(
    val assetFolderName: String = "none",
    var reloading: Boolean = false,
    val sounds: Map<String, String> = mapOf(),
    val backgrounds: Map<String, ParallaxConfig> = mapOf(),
    val images: Map<String, ImageDataConfig> = mapOf(),
    val fonts: Map<String, String> = mapOf(),
    val tiledMaps: Map<String, String> = mapOf()
) {
    data class ImageDataConfig(
        val fileName: String = "",
        val layers: String? = null
    )
}
