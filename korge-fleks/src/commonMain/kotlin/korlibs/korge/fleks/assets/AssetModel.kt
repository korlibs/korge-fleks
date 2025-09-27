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
    val tileMaps: Map<String, TileMapConfig> = mapOf(),
    val textureAtlas: List<TextureConfig> = listOf()
) {

    @Serializable @SerialName("TileMapConfig")
    data class TileMapConfig(
        val fileName: String,
        val collisionLayerName: String = "",  // Default is empty string - no collision layer or specific layer name for a tile map

        // internally used
        val tileSetPaths: MutableList<String> = mutableListOf()
    )

    @Serializable @SerialName("TextureConfig")
    data class TextureConfig(
        val fileName: String,
        val frameDurations: Map<String, FrameDurationConfig> = mapOf(),
        val nineSlices: Map<String, NineSlice> = mapOf(),
        val fonts: List<String> = listOf(),
        val parallaxEffects: Map<String, ParallaxLayer> = mapOf()
    ) {
        @Serializable @SerialName("FrameDurationConfig")
        data class FrameDurationConfig(
            val default: Int = 0,          // default duration in milliseconds for all frames of the animation
            val custom: List<Int>? = null  // [optional] custom frame duration in milliseconds for each frame of the animation
        )

        @Serializable @SerialName("NineSlice")
        data class NineSlice(
            val x: Int,
            val y: Int,
            val width: Int,
            val height: Int
        )

        /**
         * This is the configuration for an independent parallax layer. Independent means that these layers are not attached
         * to the parallax plane. Their speed in X and Y direction can be configured by [speedFactor].
         * Their self-Speed [selfSpeedX] and [selfSpeedY] can be configured independently.
         *
         * [repeatX] and [repeatY] describes if the image of the layer object should be repeated in X and Y direction.
         * [speedFactor] is the factors for scrolling the parallax layer in X and Y direction relative to the game
         * play field.
         * [selfSpeedX] and [selfSpeedY] are the factors for scrolling the parallax layer in X and Y direction continuously
         * and independently of the player input.
         */
        @Serializable @SerialName("ParallaxLayer")
        data class ParallaxLayer(
            val repeatX: Boolean = false,
            val repeatY: Boolean = false,
            val speedFactor: Float? = null,  // It this is null than no movement is applied to the layer
            val selfSpeedX: Float = 0f,
            val selfSpeedY: Float = 0f
        )
    }
}
