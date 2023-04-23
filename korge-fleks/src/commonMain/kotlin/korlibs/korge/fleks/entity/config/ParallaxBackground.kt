package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.datastructure.iterators.fastForEach
import korlibs.datastructure.iterators.fastForEachWithIndex
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.utils.KorgeViewCache
import korlibs.korge.parallax.ParallaxConfig
import korlibs.korge.parallax.ParallaxDataView


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
                it += Appearance(alpha = 1.0f)
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
                val selfSpeedX = if (isHorizontal) planeConf.selfSpeed else 0.0f
                val selfSpeedY = if (!isHorizontal) planeConf.selfSpeed else 0.0f
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

    private fun createSubEntityForLayer(parentEntity: Entity, layerName: String? = null, layerLine: Int? = null, speedFactor: Float? = null,
                                        selfSpeedX: Float = 0.0f, selfSpeedY: Float = 0.0f, isHorizontal: Boolean = true) : Entity {
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
