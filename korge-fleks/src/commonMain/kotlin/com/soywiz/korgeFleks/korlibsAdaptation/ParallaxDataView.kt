package com.soywiz.korgeFleks.korlibsAdaptation

import com.soywiz.kds.ExtraTypeCreate
import com.soywiz.kds.iterators.fastForEachWithIndex
import com.soywiz.kds.setExtra
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.*
import com.soywiz.korge.view.tiles.SingleTile
import com.soywiz.korge.view.tiles.repeat
import com.soywiz.korgeFleks.entity.config.ParallaxAttachedLayerConfig
import com.soywiz.korgeFleks.entity.config.ParallaxConfig
import com.soywiz.korgeFleks.entity.config.ParallaxLayerConfig
import com.soywiz.korgeFleks.entity.config.ParallaxPlaneConfig
import com.soywiz.korgeFleks.utils.KorgeViewBase
import com.soywiz.korim.atlas.MutableAtlas
import com.soywiz.korim.format.*
import com.soywiz.korio.file.VfsFile
import com.soywiz.korio.file.baseName

inline fun Container.parallaxDataView(
    data: ParallaxDataContainer,
    scale: Double = 1.0,
    smoothing: Boolean = false,
    callback: @ViewDslMarker ParallaxDataView.() -> Unit = {}
): ParallaxDataView = ParallaxDataView(data, scale, smoothing).addTo(this, callback)

/**
 * The ParallaxDataView is a special view object which can be used to show some background layers behind the play
 * field of a game level, menu, intro, extro, etc. It is configured through a data object [ParallaxDataContainer] which
 * stores model data for the view (cf. MVC pattern).
 *
 * The definition of a parallax layer is an image which is (in most cases) repeating in X and/or Y direction.
 * Thus scrolling of the parallax layer means that the parallax layer is moving relative to the scene camera. In the
 * camera view it looks like that the image is moving repeatedly over the screen. By constructing multiple layers which
 * are sitting one over another and which are moving with different speed factors a nice depth effect can be created.
 * That simulates the parallax effect of early 1990 video games.
 *
 * The parallax layers can be configured through a set of data classes which control how the parallax layers are
 * constructed, presented and moved over the screen. The actual image data for a layer is taken from an Aseprite file
 * which follows specific rules. This file must contain at least one frame with a couple of layers with defined names.
 * Also, for the so-called parallax plane the Aseprite file has to contain specific slice objects. An example Aseprite
 * template file is included in Korge's example "parallax-scrolling-aseprite".
 *
 * Types of parallax layers:
 * 1. Background layers
 * 2. parallax plane with attached layers
 * 3. Foreground layers
 *
 * Background layers (1) are drawn first and per definition are shown behind the parallax plane.
 * The parallax plane (2) is a special layer in the Aseprite image which is sliced into stripes of different lengths.
 * These stripes are moving with different speed which increases with more distance to the central vanishing point on
 * the screen. This results in a pseudo 3D plane effect which can be seen in early 1990 video games like Street Fighter 2
 * or Lionheart (Amiga). The parallax plane can have attached layers which are by themselves layers like background
 * layers. The difference is that they are moving depending on their position (top or bottom border) on the parallax
 * plane.
 * Finally, the Foreground layers (3) are drawn and thus are positioned in front of the parallax plane with its attached
 * layers. Any of these layer types can be also kept empty. With a combination of all layer types it is possible to
 * achieve different parallax layer effects.
 *
 * Please see the description of [ParallaxDataContainer] and its sub-data classes [ParallaxConfig],
 * [ParallaxLayerConfig], [ParallaxPlaneConfig] and [ParallaxAttachedLayerConfig] to set up a valid and meaningful
 * configuration for the parallax view.
 *
 * Scrolling of the parallax background can be done by setting [deltaX], [deltaY] and [diagonal] properties.
 * When the mode in [ParallaxConfig] is set to [ParallaxConfig.Mode.HORIZONTAL_PLANE] then setting [deltaX] is
 * moving the parallax view in horizontal direction. Additional to that the view can be moved vertically by setting
 * [diagonal] property. Its range goes from [0..1] which means it scrolls within its vertical boundaries.
 * This applies analogously to [deltaY] and [diagonal] when mode is set to [ParallaxConfig.Mode.VERTICAL_PLANE].
 * Setting mode to [ParallaxConfig.Mode.NO_PLANE] means that the parallax plane is disabled and only "normal" layers are used.
 * Then the parallax background can be scrolled endlessly in any direction by setting [deltaX] and [deltaY].
 *
 * Sometimes the parallax view should not scroll automatically but the position should be set directly by altering [x] and [y]
 * of the ParallaxDataView. E.g. when using parallax backgrounds in an intro or similar where it is needed to have more
 * "control" over the movement of the background. Thus the automatic scrolling can be disabled with [disableScrollingX] and
 * [disableScrollingY] parameters.
 */
class ParallaxDataView(
    data: ParallaxDataContainer,
    scale: Double = 1.0,
    smoothing: Boolean = false
) : Container(), KorgeViewBase {

    // Delta movement in X or Y direction of the parallax background depending on the scrolling direction
    var deltaX: Double = 0.0
    var deltaY: Double = 0.0

    // Percentage of the position diagonally to the scrolling direction (only used with parallax plane setup)
    var diagonal: Double = 0.0  // Range: [0...1]

    // Accessing properties of layer objects
    private val layerMap: HashMap<String, View> = HashMap()

    // The middle point of the parallax plane (central vanishing point on the screen)
    private val parallaxPlaneMiddlePoint: Double =
        when (data.config.mode) {
            ParallaxConfig.Mode.HORIZONTAL_PLANE -> (data.config.parallaxPlane?.size?.width?.toDouble() ?: 0.0) * 0.5
            ParallaxConfig.Mode.VERTICAL_PLANE -> (data.config.parallaxPlane?.size?.height?.toDouble() ?: 0.0) * 0.5
            ParallaxConfig.Mode.NO_PLANE -> 0.0  // not used without parallax plane setup
        }

    val parallaxLayerSize: Int =
        when (data.config.mode) {
            ParallaxConfig.Mode.HORIZONTAL_PLANE ->
                (data.backgroundLayers?.height ?: data.foregroundLayers?.height?: data.attachedLayersFront?.height ?: data.attachedLayersRear?.height ?: 0) - (data.config.parallaxPlane?.offset ?: 0)
            ParallaxConfig.Mode.VERTICAL_PLANE ->
                (data.backgroundLayers?.width ?: data.foregroundLayers?.width ?: data.attachedLayersFront?.width ?: data.attachedLayersRear?.height ?: 0) - (data.config.parallaxPlane?.offset ?: 0)
            ParallaxConfig.Mode.NO_PLANE -> 0  // not used without parallax plane setup
        }

    // Calculate array of speed factors for each line in the parallax plane.
    // The array will contain numbers starting from 1.0 -> 0.0 and then from 0.0 -> 1.0
    // The first part of the array is used as speed factor for the upper / left side of the parallax plane.
    // The second part is used for the lower / right side of the parallax plane.
    val parallaxPlaneSpeedFactor = DoubleArray(
        parallaxLayerSize
    ) { i ->
        val midPoint: Double = parallaxLayerSize * 0.5
        (data.config.parallaxPlane?.speedFactor ?: 1.0) * (
                // The pixel in the point of view must not stand still, they need to move with the lowest possible speed (= 1 / midpoint)
                // Otherwise the midpoint is "running" away over time
                if (i < midPoint)
                    1 - (i / midPoint)
                else
                    (i - midPoint + 1) / midPoint
                )
    }

    val parallaxLines: Array<View?> = Array(parallaxLayerSize) { null }

    override fun getLayer(name: String): View? = layerMap[name]

    private fun constructParallaxPlane(
        parallaxPlane: ImageDataContainer?,
        attachedLayersFront: ImageData?,
        attachedLayersRear: ImageData?,
        config: ParallaxPlaneConfig?,
        isScrollingHorizontally: Boolean,
        smoothing: Boolean
    ) {
        if (parallaxPlane == null || config == null) return
        if (parallaxPlane.imageDatas[0].frames.isEmpty()) error("Parallax plane not found. Check that name of parallax plane layer in Aseprite matches the name in the parallax config.")
        if (parallaxPlaneSpeedFactor.size < parallaxPlane.imageDatas.size) error("Parallax data must at least contain one layer in backgroundLayers, foregroundLayers or attachedLayers!")

        // Add attached layers which will be below parallax plane
        if (attachedLayersRear != null && config.attachedLayersRear != null) {
            constructAttachedLayers(attachedLayersRear, config.attachedLayersRear, smoothing, isScrollingHorizontally)
        }

        layerMap[config.name] = container {
            parallaxPlane.imageDatas.fastForEachWithIndex { i, data ->
                imageAnimView(data, playing = false, smoothing = smoothing, repeating = true) {
                    val layer = getLayer(config.name)
                    if (layer == null) {
                        println("WARNING: Could not find parallax plane '${config.name}' in ImageData. Check that name of parallax plane in Aseprite matches the name in the parallax config.")
                    } else parallaxLines[i] = (layer as SingleTile).apply {
                        if (isScrollingHorizontally) {
                            layer.repeat(repeatX = true)
                            x = parallaxPlaneMiddlePoint
                        } else {
                            layer.repeat(repeatY = true)
                            y = parallaxPlaneMiddlePoint
                        }
                    }
                }
            }
        }

        // Add attached layers which will be on top of parallax plane
        if (attachedLayersFront != null && config.attachedLayersFront != null) {
            constructAttachedLayers(attachedLayersFront, config.attachedLayersFront, smoothing, isScrollingHorizontally)
        }
    }

    private fun constructAttachedLayers(attachedLayers: ImageData, attachedLayersConfig: List<ParallaxAttachedLayerConfig>, smoothing: Boolean, isScrollingHorizontally: Boolean) {
        if (attachedLayers.frames.isEmpty()) error("No attached layers not found. Check that name of attached layers in Aseprite matches the name in the parallax config.")
        val imageData = imageAnimView(attachedLayers, playing = false, smoothing = smoothing, repeating = true)

        for (conf in attachedLayersConfig) {
            val layer = imageData.getLayer(conf.name)

            if (layer == null) {
                println("WARNING: Could not find layer '${conf.name}' in ImageData. Check that name of attached layer in Aseprite matches the name in the parallax config.")
            } else layerMap[conf.name] = (layer as SingleTile).apply {
                repeat(repeatX = isScrollingHorizontally && conf.repeat, repeatY = !isScrollingHorizontally && conf.repeat)
            }
        }
    }

    private fun constructLayer(
        layers: ImageData?,
        config: List<ParallaxLayerConfig>?,
        smoothing: Boolean,
    ) {
        if (layers == null || config == null || layers.frames.isEmpty()) return

        val imageData = imageAnimView(layers, playing = false, smoothing = smoothing, repeating = true)

        for (conf in config) {
            val layer = imageData.getLayer(conf.name)

            if (layer == null) {
                println("Could not find layer '${conf.name}' in ImageData. Check that name of layer in Aseprite matches the name in the parallax config.")
            } else layerMap[conf.name] = (layer as SingleTile).apply {
                repeat(repeatX = conf.repeatX, repeatY = conf.repeatY)
            }
        }
    }

    fun update(time: TimeSpan) {
// Maybe too expensive if no animation is played
// Better call update of layer separately if it has an animation to play
//        layerMap.forEach {
//            when (val view = it.value) {
//                is ImageAnimView -> view.update(time)
//                is Container -> view.children.forEach { line ->
//                    line as ImageAnimView
//                    line.update(time) }
//            }
//        }
    }

    init {
        // Only the base container for all view objects needs to be scaled
        this.scale = scale

        // First create background layers in the back
        constructLayer(data.backgroundLayers, data.config.backgroundLayers, smoothing)

        // Then construct the two parallax planes with their attached layers
        if (data.config.mode != ParallaxConfig.Mode.NO_PLANE) {

// TODO add check for size + offset
//            if (data.config.mode == ParallaxConfig.Mode.HORIZONTAL_PLANE) {
//                data.config.size.height
//            }
//            if (parallaxPlaneSpeedFactor.size < (data.backgroundLayers?.height ?: data.foregroundLayers?.height?: data.attachedLayers?.height ?: 0))


            constructParallaxPlane(
                data.parallaxPlane,
                data.attachedLayersFront,
                data.attachedLayersRear,
                data.config.parallaxPlane,
                data.config.mode == ParallaxConfig.Mode.HORIZONTAL_PLANE,
                smoothing
            )
//            // TODO move this into PositionSystem
//            val parallaxSize = parallaxLayerSize - (data.config.parallaxPlane?.offset ?: 0)
//            // Do horizontal or vertical movement depending on parallax scrolling direction
//            if (!disableScrollingY && data.config.mode == ParallaxConfig.Mode.HORIZONTAL_PLANE) {
//                val height = data.config.parallaxPlane?.size?.height?.toDouble() ?: 0.0
//                // Move parallax plane inside borders
//                addUpdater {
//                    // Sanity check of diagonal movement - it has to be between 0.0 and 1.0
//                    diagonal = diagonal.clamp(0.0, 1.0)
//                    y = -(diagonal * (parallaxLayerSize - height))
//                }
//            } else if (!disableScrollingX && data.config.mode == ParallaxConfig.Mode.VERTICAL_PLANE) {
//                val width = data.config.parallaxPlane?.size?.width?.toDouble() ?: 0.0
//                addUpdater {
//                    diagonal = diagonal.clamp(0.0, 1.0)
//                    x = -(diagonal * (parallaxLayerSize - width))
//                }
//            }
        }

        // Last construct the foreground layers on top
        constructLayer(data.foregroundLayers, data.config.foregroundLayers, smoothing)
    }
}

suspend fun VfsFile.readParallaxDataContainer(
    config: ParallaxConfig,
    format: ImageFormat = ASE,
    atlas: MutableAtlas<Unit>? = null,
): ParallaxDataContainer {
    val props = ImageDecodingProps(this.baseName, extra = ExtraTypeCreate())
    return ParallaxDataContainer(
        config = config,
        backgroundLayers = if (config.backgroundLayers != null) {
            props.setExtra("layers", config.backgroundLayers.joinToString(separator = ",") { it.name })
            props.setExtra("disableSlicing", true)
            val out = format.readImage(this.readAsSyncStream(), props)
            if (atlas != null) out.packInMutableAtlas(atlas) else out
        } else null,
        foregroundLayers = if (config.foregroundLayers != null) {
            props.setExtra("layers", config.foregroundLayers.joinToString(separator = ",") { it.name })
            props.setExtra("disableSlicing", true)
            val out = format.readImage(this.readAsSyncStream(), props)
            if (atlas != null) out.packInMutableAtlas(atlas) else out
        } else null,
        attachedLayersFront = if (config.parallaxPlane?.attachedLayersFront != null) {
            props.setExtra("layers", config.parallaxPlane.attachedLayersFront.joinToString(separator = ",") { it.name })
            props.setExtra("disableSlicing", true)
            val out = format.readImage(this.readAsSyncStream(), props)
            if (atlas != null) out.packInMutableAtlas(atlas) else out
        } else null,
        attachedLayersRear = if (config.parallaxPlane?.attachedLayersRear != null) {
            props.setExtra("layers", config.parallaxPlane.attachedLayersRear.joinToString(separator = ",") { it.name })
            props.setExtra("disableSlicing", true)
            val out = format.readImage(this.readAsSyncStream(), props)
            if (atlas != null) out.packInMutableAtlas(atlas) else out
        } else null,
        parallaxPlane = if (config.parallaxPlane != null) {
            props.setExtra("layers", config.parallaxPlane.name)
            props.setExtra("disableSlicing", false)
            props.setExtra("useSlicePosition", true)
            val out = format.readImageContainer(this.readAsSyncStream(), props)
            if (atlas != null) out.packInMutableAtlas(atlas) else out
        } else null
    )
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
