package korlibs.korge.fleks.assets

import korlibs.datastructure.*
import korlibs.datastructure.iterators.*
import korlibs.image.atlas.*
import korlibs.image.format.*
import korlibs.io.file.*
import kotlinx.serialization.*
import kotlinx.serialization.EncodeDefault.Mode.NEVER


suspend fun VfsFile.readParallaxDataContainer(
    config: ParallaxConfig,
    format: ImageFormat = ASE,
    atlas: MutableAtlas<Unit>? = null,
): ParallaxDataContainer {
    val props = ImageDecodingProps(this.baseName, extra = ExtraTypeCreate())
    val backgroundLayers = if (config.backgroundLayers != null) {
        props.setExtra("layers", config.backgroundLayers.joinToString(separator = ",") { it.name })
        props.setExtra("disableSlicing", true)
        val out = format.readImage(this.readAsSyncStream(), props)
        if (atlas != null) out.packInMutableAtlas(atlas) else out
    } else null
    val foregroundLayers = if (config.foregroundLayers != null) {
        props.setExtra("layers", config.foregroundLayers.joinToString(separator = ",") { it.name })
        props.setExtra("disableSlicing", true)
        val out = format.readImage(this.readAsSyncStream(), props)
        if (atlas != null) out.packInMutableAtlas(atlas) else out
    } else null
    val attachedLayersFront = if (config.parallaxPlane?.attachedLayersFront != null) {
        props.setExtra("layers", config.parallaxPlane.attachedLayersFront.joinToString(separator = ",") { it.name })
        props.setExtra("disableSlicing", true)
        val out = format.readImage(this.readAsSyncStream(), props)
        if (atlas != null) out.packInMutableAtlas(atlas) else out
    } else null
    val attachedLayersRear = if (config.parallaxPlane?.attachedLayersRear != null) {
        props.setExtra("layers", config.parallaxPlane.attachedLayersRear.joinToString(separator = ",") { it.name })
        props.setExtra("disableSlicing", true)
        val out = format.readImage(this.readAsSyncStream(), props)
        if (atlas != null) out.packInMutableAtlas(atlas) else out
    } else null
    val parallaxPlane = if (config.parallaxPlane != null) {
        props.setExtra("layers", config.parallaxPlane.name)
        props.setExtra("disableSlicing", false)
        props.setExtra("useSlicePosition", true)
        val out = format.readImageContainer(this.readAsSyncStream(), props)
        if (atlas != null) out.packInMutableAtlas(atlas) else out
    } else null

    // Re-build the layer arrays to match the order of layers in the config
    // and the image data which was loaded from Aseprite

    // Create a map of layers associated by its name
    val backgroundLayerMap = config.backgroundLayers?.associateBy { it.name }
    backgroundLayers?.defaultAnimation?.layers?.fastForEachWithIndex { index, layerData ->
        // Sanity check, should always be true
        if (config.backgroundLayers != null) {
            // Sanity check, name should always be a valid string otherwise the layer would not have been loaded from Aseprite
            val layerName: String = layerData.name ?: error("readParallaxDataContainer: Background layer does not have a name in Aseprite file!")
            // Sanity check, layer name should exist in map
            config.backgroundLayers[index] = backgroundLayerMap?.get(layerName) ?: error("readParallaxDataContainer: Background layer name '${layerData.name}' not consistent between config and Aseprite file!")
        } else error("readParallaxDataContainer: Background layer empty for layer '${layerData.name}'!")
    }
    val foregroundLayerMap = config.foregroundLayers?.associateBy { it.name }
    foregroundLayers?.defaultAnimation?.layers?.fastForEachWithIndex { index, layerData ->
        if (config.foregroundLayers != null) {
            val layerName: String = layerData.name ?: error("readParallaxDataContainer: Foreground layer does not have a name in Aseprite file!")
            config.foregroundLayers[index] = foregroundLayerMap?.get(layerName) ?: error("readParallaxDataContainer: Foreground layer name '${layerData.name}' not consistent between config and Aseprite file!")
        } else error("readParallaxDataContainer: Foreground layer empty for layer '${layerData.name}'!")
    }
    val attachedLayerFrontMap = config.parallaxPlane?.attachedLayersFront?.associateBy { it.name }
    attachedLayersFront?.defaultAnimation?.layers?.fastForEachWithIndex { index, layerData ->
        if (config.parallaxPlane?.attachedLayersFront != null) {
            val layerName: String = layerData.name ?: error("readParallaxDataContainer: Attached front layer does not have a name in Aseprite file!")
            config.parallaxPlane.attachedLayersFront[index] = attachedLayerFrontMap?.get(layerName) ?: error("readParallaxDataContainer: Attached front layer name '${layerData.name}' not consistent between config and Aseprite file!")
        } else error("readParallaxDataContainer: Attached front layer empty for layer '${layerData.name}'!")
    }
    val attachedLayerRearMap = config.parallaxPlane?.attachedLayersRear?.associateBy { it.name }
    attachedLayersRear?.defaultAnimation?.layers?.fastForEachWithIndex { index, layerData ->
        if (config.parallaxPlane?.attachedLayersRear != null) {
            val layerName: String = layerData.name ?: error("readParallaxDataContainer: Attached rear layer does not have a name in Aseprite file!")
            config.parallaxPlane.attachedLayersRear[index] = attachedLayerRearMap?.get(layerName) ?: error("readParallaxDataContainer: Attached rear layer name '${layerData.name}' not consistent between config and Aseprite file!")
        } else error("readParallaxDataContainer: Attached rear layer empty for layer '${layerData.name}'!")
    }

    // Precalculate
    val parallaxLayerSize: Int =
        when (config.mode) {
            ParallaxConfig.Mode.HORIZONTAL_PLANE -> {
                (backgroundLayers?.height ?: foregroundLayers?.height ?: attachedLayersFront?.height
                ?: attachedLayersRear?.height ?: 0) - (config.parallaxPlane?.offset ?: 0)
            }
            ParallaxConfig.Mode.VERTICAL_PLANE -> {
                (backgroundLayers?.width ?: foregroundLayers?.width ?: attachedLayersFront?.width
                ?: attachedLayersRear?.height ?: 0) - (config.parallaxPlane?.offset ?: 0)
            }
            ParallaxConfig.Mode.NO_PLANE -> 0  // not used without parallax plane setup
        }

    // Calculate array of speed factors for each line in the parallax plane.
    // The array will contain numbers starting from 1.0 -> 0.0 and then from 0.0 -> 1.0
    // The first part of the array is used as speed factor for the upper / left side of the parallax plane.
    // The second part is used for the lower / right side of the parallax plane.
    config.parallaxPlane?.parallaxPlaneSpeedFactors =
        FloatArray(parallaxLayerSize) { i ->
            val midPoint: Float = parallaxLayerSize * 0.5f
            (config.parallaxPlane!!.speedFactor) * (
                // The pixel in the point of view must not stand still, they need to move with the lowest possible speed (= 1 / midpoint)
                // Otherwise the midpoint is "running" away over time
                if (i < midPoint)
                    1f - (i / midPoint)
                else
                    (i - midPoint + 1f) / midPoint
                )
        }

    return ParallaxDataContainer(config, backgroundLayers, foregroundLayers, attachedLayersFront, attachedLayersRear, parallaxPlane)
}

/**
 * This class contains all data which is needed by the [ParallaxDataView] to display the parallax view on the screen.
 * It stores the [ParallaxConfig] and all [ImageData] objects for the background, foreground and attached Layers. The
 * parallax plane is a sliced Aseprite image and therefore consists of a [ImageDataContainer] object.
 *
 * All these image data objects are read from one Aseprite file. Function [readParallaxDataContainer]
 * uses special [ImageDecodingProps] to control which details of the Aseprite file are read into which image
 * data object.
 */
data class ParallaxDataContainer(
    val config: ParallaxConfig,
    val backgroundLayers: ImageData?,
    val foregroundLayers: ImageData?,
    val attachedLayersFront: ImageData?,
    val attachedLayersRear: ImageData?,
    val parallaxPlane: ImageDataContainer?
)
/**
 * This is the main parallax configuration.
 * The [aseName] is the name of the aseprite file which is used for reading the image data.
 * (Currently it is not used. It will be used when reading the config from YAML/JSON file.)
 *
 * The parallax [mode] has to be one of the following enum values:
 * - [NO_PLANE]
 *   This type is used to set up a parallax background which will scroll repeatedly in X and Y direction. For this
 *   type of parallax effect it makes most sense to repeat the layers in X and Y direction (see [ParallaxLayerConfig]).
 *   The [parallaxPlane] object will not be used in this mode.
 * - [HORIZONTAL_PLANE]
 *   This is the default parallax mode. It is used to create an endless scrolling horizontal parallax background.
 *   Therefore, it makes sense to repeat the parallax layers in X direction in [ParallaxLayerConfig]. Also in this
 *   mode the [parallaxPlane] object is active which also can contain attached layers. If the virtual height of [size]
 *   is greater than the visible height on the screen then the view can be scrolled up and down with the diagonal
 *   property of [ParallaxDataView].
 * - [VERTICAL_PLANE]
 *   This mode is the same as [HORIZONTAL_PLANE] but in vertical direction.
 *
 * [backgroundLayers] and [foregroundLayers] contain the configuration for independent layers. They can be used with
 * all three parallax [mode]s. [parallaxPlane] is the configuration for the special parallax plane with attached
 * layers. Please look at [ParallaxLayerConfig] and [ParallaxPlaneConfig] data classes for more details.
 */
@Serializable @SerialName("ParallaxConfig")
data class ParallaxConfig(
    val aseName: String,
    val mode: Mode = Mode.HORIZONTAL_PLANE,
    val backgroundLayers: ArrayList<ParallaxLayerConfig>? = null,
    val parallaxPlane: ParallaxPlaneConfig? = null,
    val foregroundLayers: ArrayList<ParallaxLayerConfig>? = null
) {
    enum class Mode {
        HORIZONTAL_PLANE, VERTICAL_PLANE, NO_PLANE
    }
}

/**
 * This is the configuration of the parallax plane which can be used in [HORIZONTAL_PLANE] and
 * [VERTICAL_PLANE] modes. The parallax plane itself consists of a top and a bottom part. The top part
 * can be used to represent a ceiling (e.g. of a cave, building or sky). The bottom part is usually showing some ground.
 * The top part is the upper half of the Aseprite image. The bottom part is the bottom half of the image. This is used
 * to simulate a central vanishing point in the resulting parallax effect.
 *
 * [size] contains the virtual size (height or width) of the parallax plane background.
 * [offset] is the amount of pixels from the top of the image where the upper part of the parallax plane starts.
 * [name] has to be set to the name of the layer in the Aseprite which contains the image for the sliced stripes
 * of the parallax plane.
 * [speedFactor] is the factor for scrolling the parallax plane relative to the game play field (which usually contains the
 * level map).
 * [selfSpeed] is the amount of velocity for scrolling the parallax plane continuously in a direction independently of the player
 * input.
 * [attachedLayersFront] contains the config for further layers which are "attached" on top of the parallax plane.
 * [attachedLayersRear] contains the config for further layers which are "attached" below the parallax plane.
 * Both attached layer types will scroll depending on their position on the parallax plane.
 */
@Serializable @SerialName("ParallaxPlaneConfig")
data class ParallaxPlaneConfig @OptIn(ExperimentalSerializationApi::class) constructor(
    val offset: Int = 0,
    val name: String,
    val speedFactor: Float = 1f,
    // TODO move parallaxPlaneSpeedFactors to static Parallax entity config (which is always re-created after deserialization)
    @EncodeDefault(NEVER) var parallaxPlaneSpeedFactors: FloatArray = floatArrayOf(),  // create empty list, the real size will come from the texture size
//    val parallaxPlaneSpeedFactors: FloatArrayList = FloatArrayList(capacity = 0),  // create empty list, the real size will come from the texture size
    val selfSpeed: Float = 0f,
    val attachedLayersFront: ArrayList<ParallaxAttachedLayerConfig>? = null,
    val attachedLayersRear: ArrayList<ParallaxAttachedLayerConfig>? = null
)

/**
 * This is the configuration for layers which are attached to the parallax plane. These layers are moving depending
 * on its position on the parallax plane. They can be attached to the top or the bottom part of the parallax plane.
 *
 * [name] has to be set to the name of the layer in the used Aseprite file. The image on this layer will be taken for
 * the layer object.
 * [repeat] describes if the image of the layer object should be repeated in the scroll direction (horizontal or
 * vertical) of the parallax plane.
 *
 * When mode is set to [HORIZONTAL_PLANE] and [attachBottomRight] is set to false then the top
 * border of the layer is attached to the parallax plane. If [attachBottomRight] is set to true than the bottom
 * border is attached.
 * When [mode][ParallaxConfig.Mode] is [VERTICAL_PLANE] and [attachBottomRight] is false then the left border
 * of the layer will be attached to the parallax plane. If [attachBottomRight] is true then the right border
 * will be attached.
 */
@Serializable @SerialName("ParallaxAttachedLayerConfig")
data class ParallaxAttachedLayerConfig(
    val name: String,
    val repeat: Boolean = false,
    val attachBottomRight: Boolean = false
)

/**
 * This is the configuration for an independent parallax layer. Independent means that these layers are not attached
 * to the parallax plane. Their speed in X and Y direction can be configured by [speedFactor].
 * Their self-Speed [selfSpeedX] and [selfSpeedY] can be configured independently.
 *
 * [name] has to be set to the name of the layer in the used Aseprite file. The image on this layer will be taken for
 * the layer object.
 * [repeatX] and [repeatY] describes if the image of the layer object should be repeated in X and Y direction.
 * [speedFactor] is the factors for scrolling the parallax layer in X and Y direction relative to the game
 * play field.
 * [selfSpeedX] and [selfSpeedY] are the factors for scrolling the parallax layer in X and Y direction continuously
 * and independently of the player input.
 */
@Serializable @SerialName("ParallaxLayerConfig")
data class ParallaxLayerConfig(
    val name: String,
    val repeatX: Boolean = false,
    val repeatY: Boolean = false,
    val speedFactor: Float? = null,  // It this is null than no movement is applied to the layer
    val selfSpeedX: Float = 0f,
    val selfSpeedY: Float = 0f
)
