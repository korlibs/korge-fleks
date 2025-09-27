package korlibs.korge.fleks.assets.data

import korlibs.datastructure.size
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This is the main parallax configuration.
 *
 * [offset] is the amount of pixels from the top of the image where the upper part of the parallax plane starts.
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
data class ParallaxConfigNew(
    val offset: Int = 0,
    val mode: Mode = Mode.HORIZONTAL_PLANE,
    val backgroundLayers: List<ParallaxLayerConfig> = listOf(),
//    val parallaxPlane: ParallaxPlaneConfig? = null,
    val foregroundLayers: List<ParallaxLayerConfig> = listOf()
) {
    enum class Mode {
        HORIZONTAL_PLANE, VERTICAL_PLANE, NO_PLANE
    }

    /**
     * This is the configuration for an independent parallax layer. Independent means that these layers are not attached
     * to the parallax plane. Their speed in X and Y direction can be configured by [speedFactor].
     * Their self-Speed [selfSpeedX] and [selfSpeedY] can be configured independently.
     *
     * [name] has to be set to the name of the layer in the texture atlas file. The image on this layer will be taken for
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


}
