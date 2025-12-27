package korlibs.korge.fleks.assets.data

import korlibs.image.bitmap.BmpSlice
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


/**
 * Asset configuration data class.
 *
 *  - version: version info (major, minor, build)
 *  - textures: list of texture atlas file names
 *  - images: map of image names to [ImageInfo]
 *  - ninePatches: map of nine-patch image names to [NinePatchInfo]
 *  - pixelFonts: map of pixel font names to [PixelFontInfo]
 *  - parallaxLayers: map of parallax layer config objects to [ParallaxLayersInfo]
 */
@Serializable
data class AssetConfig(
    val version: List<Int> = emptyList(),
    val textures: List<String> = emptyList(),
    val tilesets: List<String> = emptyList(),
    val images: Map<String, ImageInfo> = emptyMap(),
    val ninePatches: Map<String, NinePatchInfo> = emptyMap(),
    val pixelFonts: Map<String, PixelFontInfo> = emptyMap(),
    val parallaxLayers: Map<String, ParallaxLayersInfo> = emptyMap(),
    val tiles: TilesInfo = TilesInfo()
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
     * Parallax layers info data class.
     *
     *  - width, height: virtual size of parallax background (used only for HORIZONTAL_PLANE and VERTICAL_PLANE modes)
     */
    @Serializable
    data class ParallaxLayersInfo(
        @SerialName("w") val width: Int = 0,
        @SerialName("h") val height: Int = 0,
        @SerialName("m") val mode: Mode = Mode.NO_PLANE,
        @SerialName("b") val backgroundLayers: List<ParallaxLayer> = emptyList(),
        @SerialName("f") val foregroundLayers: List<ParallaxLayer> = emptyList(),
        @SerialName("p") val parallaxPlane: ParallaxPlane? = null
    ) {
        enum class Mode {
            HORIZONTAL_PLANE, VERTICAL_PLANE, NO_PLANE
        }

        @Serializable
        data class ParallaxLayer(
            @SerialName("n") val name: String = "",
            @SerialName("f") val frame: List<Int> = emptyList(),

            @SerialName("tx") val targetX: Int = 0,  // offset from the left corner of the parallax background image used in VERTICAL_PLANE mode
            @SerialName("ty") val targetY: Int = 0,  // offset from the top corner of the parallax background image used in HORIZONTAL_PLANE mode
            @SerialName("rx") val repeatX: Boolean = false,
            @SerialName("ry") val repeatY: Boolean = false,
            @SerialName("cx") val centerX: Boolean = false,  // Center the layer in the parallax background image
            @SerialName("cy") val centerY: Boolean = false,
            @SerialName("sf") val speedFactor: Float? = null,  // It this is null than no movement is applied to the layer
            @SerialName("sx") val selfSpeedX: Float = 0f,
            @SerialName("sy") val selfSpeedY: Float = 0f
        ) {
            @Transient  // This is set when loading the texture atlas
            lateinit var bmpSlice: BmpSlice
        }

        @Serializable
        data class ParallaxPlane(
            @SerialName("n") val name: String = "",
            @SerialName("s") val selfSpeed: Float = 0f,
            @SerialName("l") val lineTextures: MutableList<LineTexture> = mutableListOf(),
            @SerialName("t") val topAttachedLayers: MutableList<LineTexture> = mutableListOf(),
            @SerialName("b") val bottomAttachedLayers: MutableList<LineTexture> = mutableListOf()
        ) {
            @Serializable
            data class LineTexture(
                @SerialName("n") val name: String? = null,  // name only loaded for top and bottom attached layers
                @SerialName("f") val frame: List<Int>,
                @SerialName("i") val index: Int,
                @SerialName("s") val speedFactor: Float
            ) {
                @Transient  // This is set when loading the texture atlas
                lateinit var bmpSlice: BmpSlice
            }
        }
    }

    @Serializable
    data class TilesInfo(
        @SerialName("w") val tileWidth: Int = 0,
        @SerialName("h") val tileHeight: Int = 0,
        @SerialName("f") val frames: Map<String, List<Int>> = emptyMap()
    )
}
