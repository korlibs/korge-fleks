package korlibs.korge.fleks.assets.data

import korlibs.korge.fleks.assets.data.AssetConfig.ImageInfo.ImageFrame
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Asset configuration data class.
 *
 *  - info: version info
 *  - textures: list of texture atlas file names
 *  - images: map of image names to [ImageInfo]
 *  - ninePatches: map of nine-patch image names to [NinePatchInfo]
 */
@Serializable
data class AssetConfig(
    val info: List<Int> = emptyList(),
    val textures: List<String> = emptyList(),
    val images: Map<String, ImageInfo> = emptyMap(),
    val ninePatches: Map<String, NinePatchInfo> = emptyMap(),
    val pixelFonts: Map<String, PixelFontInfo> = emptyMap(),
    val parallaxLayers: Map<String, ParallaxImageInfo> = emptyMap(),
    val parallaxConfigs: Map<String, ParallaxConfigV2> = emptyMap()
) {
    /**
     * Image info data class.
     *
     *  - width, height: original image width and height (before cropping)
     *  - frames: list of image frames for animations
     */

    @Serializable
    data class ImageInfo(
        @SerialName("w") val width: Int = 0,
        @SerialName("h") val height: Int = 0,
        @SerialName("f") val frames: List<ImageFrame> = emptyList()
    ) {
        /**
         * Image frame info data class.
         *
         *  - frame: [textureIndex, x, y, width, height] - Position and size in texture atlas
         *  - xOffset, yOffset: frame offset position for cropped images
         *  - duration: frame duration in milliseconds (for animations)
         */
        @Serializable
        data class ImageFrame(
            @SerialName("f") val frame: List<Int> = emptyList(),
            @SerialName("x") val xOffset: Int = 0,
            @SerialName("y") val yOffset: Int = 0,
            @SerialName("d") val duration: Int = 0
        )
    }

    /**
     * Nine-patch image info data class.
     * Nine-patch textures are never cropped in the texture atlas
     *
     *  - frame: [textureIndex, x, y, width, height] - Position and size in texture atlas
     *  - centerX, centerY: nine-patch center position offset
     *  - centerWidth, centerHeight: nine-patch center size
     */
    @Serializable
    data class NinePatchInfo(
        @SerialName("f") val frame: List<Int> = emptyList(),
        @SerialName("x") val centerX: Int = 0,
        @SerialName("y") val centerY: Int = 0,
        @SerialName("w") val centerWidth: Int = 0,
        @SerialName("h") val centerHeight: Int = 0
    )

    /**
     * Pixel font info data class.
     *
     *  - type: font extension type (e.g. "fnt")
     *  - frame: [textureIndex, x, y, width, height] - Position and size in texture atlas
     */
    @Serializable
    data class PixelFontInfo(
        @SerialName("t") val type: String = "",
        @SerialName("f") val frame: List<Int> = emptyList()
    )

    /**
     * Parallax image info data class.
     *
     *  - width, height: virtual size of parallax background (used only for HORIZONTAL_PLANE and VERTICAL_PLANE modes)
     *  - frames: list of image frames for animations
     *  - parallaxLayerConfig: optional parallax layer configuration
     *  - ParallaxAttachedLayerConfig: optional parallax plane configuration
     *
     *  Normally a parallax image will have either a parallaxLayerConfig or a ParallaxAttachedLayerConfig defined.
     */
    @Serializable
    data class ParallaxImageInfo(
        @SerialName("w") val width: Int = 0,
        @SerialName("h") val height: Int = 0,
        @SerialName("f") val frames: List<ImageFrame> = emptyList(),
        @SerialName("l") val parallaxLayerConfig: ParallaxConfig.ParallaxLayerConfig? = null,
        @SerialName("p") val parallaxAttachedLayerConfig: ParallaxConfig.ParallaxAttachedLayerConfig? = null
    )
}
