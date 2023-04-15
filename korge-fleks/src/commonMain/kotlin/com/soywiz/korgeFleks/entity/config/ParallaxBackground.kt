package com.soywiz.korgeFleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.kds.iterators.fastForEachWithIndex
import com.soywiz.korge.view.size
import com.soywiz.korgeFleks.assets.AssetStore
import com.soywiz.korgeFleks.components.*
import com.soywiz.korgeFleks.korlibsAdaptation.ParallaxDataView
import com.soywiz.korgeFleks.utils.KorgeViewCache
import com.soywiz.korma.geom.SizeInt


class ParallaxBackground(private val world: World, assetName: String, drawOnLayer: String) {

    val entity: Entity

    // All sub-entity IDs are here for quick lookup by its layer name and for recycling of the overall background entity object
    private val layerEntityMap = mutableMapOf<String, Entity>()

    fun getEntityByLayerName(name: String) : Entity = layerEntityMap[name] ?: error("ParallaxBackground: Layer '$name' not found!")

    init {
        with(world) {
            val korgeViewCache = inject<KorgeViewCache>("normalViewCache")

            entity = entity {
                it += Parallax(assetName = assetName)
                it += PositionShape()
                it += Drawable(drawOnLayer = drawOnLayer)
                it += Appearance(alpha = 1.0)
            }

            // Once the base ParallaxDataView is created with above base entity we can access it from the cache
            val view = korgeViewCache[entity] as ParallaxDataView

            val config = inject<AssetStore>().getBackground(assetName).config
            val isHorizontal = config.mode == ParallaxConfig.Mode.HORIZONTAL_PLANE

            config.backgroundLayers?.fastForEach { conf ->
                layerEntityMap[conf.name] = createSubEntityForLayer(entity, conf.name,
                    speedFactor = conf.speedFactor, selfSpeedX = conf.selfSpeedX, selfSpeedY = conf.selfSpeedY,
                    isHorizontal = isHorizontal)
            }
            config.foregroundLayers?.fastForEach { conf ->
                layerEntityMap[conf.name] = createSubEntityForLayer(entity, conf.name,
                    speedFactor = conf.speedFactor, selfSpeedX = conf.selfSpeedX, selfSpeedY = conf.selfSpeedY,
                    isHorizontal = isHorizontal)
            }
            config.parallaxPlane?.let { planeConf ->
                val offset = planeConf.offset
                val selfSpeedX = if (isHorizontal) planeConf.selfSpeed else 0.0
                val selfSpeedY = if (!isHorizontal) planeConf.selfSpeed else 0.0
                planeConf.attachedLayersFront?.fastForEach { conf ->
                    val layer = korgeViewCache.getLayer(entity, conf.name)
                    layerEntityMap[conf.name] = createSubEntityForLayer(entity, conf.name,
                        speedFactor = view.parallaxPlaneSpeedFactor[layer.y.toInt() - offset + (layer.height.toInt().takeIf { conf.attachBottomRight } ?: 0)],
                        selfSpeedX = selfSpeedX, selfSpeedY = selfSpeedY,
                        isHorizontal = isHorizontal)
                }
                planeConf.attachedLayersRear?.fastForEach { conf ->
                    val layer = korgeViewCache.getLayer(entity, conf.name)
                    layerEntityMap[conf.name] = createSubEntityForLayer(entity, conf.name,
                        speedFactor = view.parallaxPlaneSpeedFactor[layer.y.toInt() - offset + (layer.height.toInt().takeIf { conf.attachBottomRight } ?: 0)],
                        selfSpeedX = selfSpeedX, selfSpeedY = selfSpeedY,
                        isHorizontal = isHorizontal)
                }

                view.parallaxLines.fastForEachWithIndex { index, it -> it?.let { line ->
                    // Here we need to take speedFactor per line into account
                    layerEntityMap[planeConf.name + index] =  createSubEntityForLayer(entity, layerLine = index,
                        speedFactor = view.parallaxPlaneSpeedFactor[line.y.toInt() - offset],
                        selfSpeedX = selfSpeedX, selfSpeedY = selfSpeedY,
                        isHorizontal = isHorizontal)
                }}
            }
        }
    }

    private fun createSubEntityForLayer(parentEntity: Entity, layerName: String? = null, layerLine: Int? = null, speedFactor: Double? = null,
                                        selfSpeedX: Double = 0.0, selfSpeedY: Double = 0.0, isHorizontal: Boolean = true) : Entity {
        return world.entity {
            it += SpecificLayer(parentEntity = parentEntity, spriteLayer = layerName, parallaxPlaneLine = layerLine)
            it += PositionShape()
            it += Appearance()
            speedFactor?.let { speedFactor ->
                it += ParallaxMotion(isScrollingHorizontally = isHorizontal, speedFactor = speedFactor,
                    selfSpeedX = selfSpeedX, selfSpeedY = selfSpeedY)
            }
        }
    }
}


/**
 * This is the main parallax configuration.
 * The [aseName] is the name of the aseprite file which is used for reading the image data.
 * (Currently it is not used. It will be used when reading the config from YAML/JSON file.)
 *
 * The parallax [mode] has to be one of the following enum values:
 * - [ParallaxConfig.Mode.NO_PLANE]
 *   This type is used to set up a parallax background which will scroll repeatedly in X and Y direction. For this
 *   type of parallax effect it makes most sense to repeat the layers in X and Y direction (see [ParallaxLayerConfig]).
 *   The [parallaxPlane] object will not be used in this mode.
 * - [ParallaxConfig.Mode.HORIZONTAL_PLANE]
 *   This is the default parallax mode. It is used to create an endless scrolling horizontal parallax background.
 *   Therefore, it makes sense to repeat the parallax layers in X direction in [ParallaxLayerConfig]. Also in this
 *   mode the [parallaxPlane] object is active which also can contain attached layers. If the virtual height of [size]
 *   is greater than the visible height on the screen then the view can be scrolled up and down with the diagonal
 *   property of [ParallaxDataView].
 * - [ParallaxConfig.Mode.VERTICAL_PLANE]
 *   This mode is the same as [ParallaxConfig.Mode.HORIZONTAL_PLANE] but in vertical direction.
 *
 * [backgroundLayers] and [foregroundLayers] contain the configuration for independent layers. They can be used with
 * all three parallax [mode]s. [parallaxPlane] is the configuration for the special parallax plane with attached
 * layers. Please look at [ParallaxLayerConfig] and [ParallaxPlaneConfig] data classes for more details.
 */
data class ParallaxConfig(
    val aseName: String,
    val mode: Mode = Mode.HORIZONTAL_PLANE,
    val backgroundLayers: List<ParallaxLayerConfig>? = null,
    val parallaxPlane: ParallaxPlaneConfig? = null,
    val foregroundLayers: List<ParallaxLayerConfig>? = null
) {
    enum class Mode {
        HORIZONTAL_PLANE, VERTICAL_PLANE, NO_PLANE
    }
}

/**
 * This is the configuration of the parallax plane which can be used in [ParallaxConfig.Mode.HORIZONTAL_PLANE] and
 * [ParallaxConfig.Mode.VERTICAL_PLANE] modes. The parallax plane itself consists of a top and a bottom part. The top part
 * can be used to represent a ceiling (e.g. of a cave, building or sky). The bottom part is usually showing some ground.
 * The top part is the upper half of the Aseprite image. The bottom part is the bottom part. This is used to simulate
 * a central vanishing point in the resulting parallax effect.
 *
 * [size] contains the virtual size of the parallax background which describes the resolution in pixels which is
 * displayed on the screen.
 * [offset] TODO add description
 *
 * [name] has to be set to the name of the layer in the Aseprite which contains the image for the sliced stripes
 * of the parallax plane.
 * [speed] is the factor for scrolling the parallax plane relative to the game play field (which usually contains the
 * level map).
 * [selfSpeed] is the factor for scrolling the parallax plane continuously in a direction independently of the player
 * input.
 * [attachedLayersFront] contains the config for further layers which are "attached" on top of the parallax plane.
 * [attachedLayersRear] contains the config for further layers which are "attached" below the parallax plane.
 * Both attached layer types will scroll depending on their position on the parallax plane.
 */
data class ParallaxPlaneConfig(
    val size: SizeInt,
    val offset: Int = 0,
    val name: String,
    val speedFactor: Double = 1.0,
    val selfSpeed: Double = 0.0,
    val attachedLayersFront: List<ParallaxAttachedLayerConfig>? = null,
    val attachedLayersRear: List<ParallaxAttachedLayerConfig>? = null
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
 * When mode is set to [ParallaxConfig.Mode.HORIZONTAL_PLANE] and [attachBottomRight] is set to false then the top
 * border of the layer is attached to the parallax plane. If [attachBottomRight] is set to true than the bottom
 * border is attached.
 * When mode is set to [ParallaxConfig.Mode.VERTICAL_PLANE] and [attachBottomRight] is set to false then the left border of the
 * layer is attached to the parallax plane. If [attachBottomRight] is set to true than the right border is attached.
 */
data class ParallaxAttachedLayerConfig(
    val name: String,
    val repeat: Boolean = false,
    val attachBottomRight: Boolean = false
)

/**
 * This is the configuration for an independent parallax layer. Independent means that these layers are not attached
 * to the parallax plane. Their speed in X and Y direction can be configured independently by [speedFactorX] and [speedFactorY].
 * Also, their self-Speed [selfSpeedX] and [selfSpeedY] can be configured independently.
 *
 * [name] has to be set to the name of the layer in the used Aseprite file. The image on this layer will be taken for
 * the layer object.
 * [repeatX] and [repeatY] describes if the image of the layer object should be repeated in X and Y direction.
 * [speedFactor] is the factors for scrolling the parallax layer in X and Y direction relative to the game
 * play field.
 * [selfSpeedX] and [selfSpeedY] are the factors for scrolling the parallax layer in X and Y direction continuously
 * and independently of the player input.
 */
data class ParallaxLayerConfig(
    val name: String,
    val repeatX: Boolean = false,
    val repeatY: Boolean = false,
    val speedFactor: Double? = null,  // It this is null than no movement is applied to the layer
    val selfSpeedX: Double = 0.0,
    val selfSpeedY: Double = 0.0
)
