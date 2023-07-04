package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.datastructure.iterators.fastForEach
import korlibs.datastructure.iterators.fastForEachWithIndex
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.assets.ConfigBase
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.utils.Identifier
import korlibs.korge.fleks.utils.KorgeViewCache
import korlibs.korge.parallax.ParallaxConfig
import korlibs.korge.parallax.ParallaxDataView


object ParallaxBackground {

    data class Config(
        val assetName: Identifier,
        val drawOnLayer: String
    ) : ConfigBase

    // Used in component properties to specify invokable function
    val configureParallaxLayers = Identifier(name = "configureParallaxLayers")
    val configureParallaxBackground = Identifier(name = "configureParallaxBackground")

    // Game object related functions
    fun create(world: World, config: Identifier) = configureParallaxBackgroundFct(world, world.entity(), config)
    fun getEntityByLayerName(world: World, entity: Entity, name: String): Entity = with (world) {
        return entity[SubEntities][name]
    }

    private val configureParallaxLayersFct = fun(world: World, entity: Entity, assetConfig: Identifier): Entity = with(world) {
        println("Re-configure attached parallax Layers")

        val korgeViewCache = inject<KorgeViewCache>("KorgeViewCache")

        val config = inject<AssetStore>("AssetStore").getBackground(assetConfig).config
        val isHorizontal = config.mode == ParallaxConfig.Mode.HORIZONTAL_PLANE
        val view = korgeViewCache[entity] as ParallaxDataView
        val layerMap = entity[SubEntities]

        config.parallaxPlane?.let { planeConf ->
            val offset = planeConf.offset
            val selfSpeedX = if (isHorizontal) planeConf.selfSpeed else 0.0f
            val selfSpeedY = if (!isHorizontal) planeConf.selfSpeed else 0.0f

            // Update only attached layers because they might change their speed depending on their position on the ground plane
            planeConf.attachedLayersFront?.fastForEach { conf ->
                val layer = korgeViewCache.getLayer(entity, conf.name)
                configureSubEntityForLayer(
                    world = world,
                    entity = layerMap[conf.name],
                    layerName = conf.name,
                    speedFactor = view.parallaxPlaneSpeedFactor[layer.y.toInt() - offset + (layer.height.toInt().takeIf { conf.attachBottomRight } ?: 0)],
                    selfSpeedX = selfSpeedX, selfSpeedY = selfSpeedY,
                    isHorizontal = isHorizontal
                )
            }
            planeConf.attachedLayersRear?.fastForEach { conf ->
                val layer = korgeViewCache.getLayer(entity, conf.name)
                configureSubEntityForLayer(
                    world = world,
                    entity = layerMap[conf.name],
                    layerName = conf.name,
                    speedFactor = view.parallaxPlaneSpeedFactor[layer.y.toInt() - offset + (layer.height.toInt().takeIf { conf.attachBottomRight } ?: 0)],
                    selfSpeedX = selfSpeedX, selfSpeedY = selfSpeedY,
                    isHorizontal = isHorizontal
                )
            }
        }
        entity
    }

    private val configureParallaxBackgroundFct = fun(world: World, entity: Entity, config: Identifier): Entity = with(world) {
        val korgeViewCache = inject<KorgeViewCache>("KorgeViewCache")
        val parallaxConfig = inject<AssetStore>( "AssetStore").getEntityConfig<Config>(config)

        entity.configure {
            it += Parallax(config = parallaxConfig.assetName)
            it += PositionShape()
            it += Drawable(drawOnLayer = parallaxConfig.drawOnLayer)
            it += Appearance(alpha = 1.0f)
            // All sub-entity IDs are here for quick lookup by its layer name and for recycling of the overall background entity object
            it += SubEntities(moveWithParent = false)
        }

        // Once the base ParallaxDataView is created with above base entity we can access it from the cache
        val view = korgeViewCache[entity] as ParallaxDataView
        val layerMap = entity[SubEntities]

        val config = inject<AssetStore>("AssetStore").getBackground(parallaxConfig.assetName).config
        val isHorizontal = config.mode == ParallaxConfig.Mode.HORIZONTAL_PLANE

        config.backgroundLayers?.fastForEach { conf ->
            layerMap.entities[conf.name] = createSubEntityForLayer(world, entity, conf.name,
                speedFactor = conf.speedFactor, selfSpeedX = conf.selfSpeedX, selfSpeedY = conf.selfSpeedY,
                isHorizontal = isHorizontal)
        }
        config.foregroundLayers?.fastForEach { conf ->
            layerMap.entities[conf.name] = createSubEntityForLayer(world, entity, conf.name,
                speedFactor = conf.speedFactor, selfSpeedX = conf.selfSpeedX, selfSpeedY = conf.selfSpeedY,
                isHorizontal = isHorizontal)
        }
        config.parallaxPlane?.let { planeConf ->
            val offset = planeConf.offset
            val selfSpeedX = if (isHorizontal) planeConf.selfSpeed else 0.0f
            val selfSpeedY = if (!isHorizontal) planeConf.selfSpeed else 0.0f
            planeConf.attachedLayersFront?.fastForEach { conf ->
                val layer = korgeViewCache.getLayer(entity, conf.name)
                layerMap.entities[conf.name] = createSubEntityForLayer(world, entity, conf.name,
                    speedFactor = view.parallaxPlaneSpeedFactor[layer.y.toInt() - offset + (layer.height.toInt().takeIf { conf.attachBottomRight } ?: 0)],
                    selfSpeedX = selfSpeedX, selfSpeedY = selfSpeedY,
                    isHorizontal = isHorizontal)
            }
            planeConf.attachedLayersRear?.fastForEach { conf ->
                val layer = korgeViewCache.getLayer(entity, conf.name)
                layerMap.entities[conf.name] = createSubEntityForLayer(world, entity, conf.name,
                    speedFactor = view.parallaxPlaneSpeedFactor[layer.y.toInt() - offset + (layer.height.toInt().takeIf { conf.attachBottomRight } ?: 0)],
                    selfSpeedX = selfSpeedX, selfSpeedY = selfSpeedY,
                    isHorizontal = isHorizontal)
            }

            view.parallaxLines.fastForEachWithIndex { index, it -> it?.let { line ->
                // Here we need to take speedFactor per line into account
                layerMap.entities[planeConf.name + index] = createSubEntityForLayer(world, entity, layerLine = index,
                    speedFactor = view.parallaxPlaneSpeedFactor[line.y.toInt() - offset],
                    selfSpeedX = selfSpeedX, selfSpeedY = selfSpeedY,
                    isHorizontal = isHorizontal)
            }}
        }
        entity
    }

    init {
        Invokables.register(configureParallaxLayers, configureParallaxLayersFct)
        Invokables.register(configureParallaxBackground, configureParallaxBackgroundFct)
    }

    private fun createSubEntityForLayer(world: World, parentEntity: Entity, layerName: String? = null, layerLine: Int? = null, speedFactor: Float? = null,
                                        selfSpeedX: Float = 0.0f, selfSpeedY: Float = 0.0f, isHorizontal: Boolean = true) : Entity {
        return configureSubEntityForLayer(world, world.entity(), parentEntity, layerName, layerLine, speedFactor, selfSpeedX, selfSpeedY, isHorizontal)
    }

    private fun configureSubEntityForLayer(world: World, entity: Entity, parentEntity: Entity? = null, layerName: String?, layerLine: Int? = null, speedFactor: Float? = null,
                                           selfSpeedX: Float = 0.0f, selfSpeedY: Float = 0.0f, isHorizontal: Boolean = true) : Entity  = with(world) {
        entity.configure { entity ->
            entity.getOrAdd(SpecificLayer) { SpecificLayer() }.also {
                parentEntity?.let { parentEntity -> it.parentEntity = parentEntity }
                it.spriteLayer = layerName
                it.parallaxPlaneLine = layerLine
            }
            entity.getOrAdd(PositionShape) { PositionShape() }
            entity.getOrAdd(Appearance) { Appearance() }
            speedFactor?.let { speedFactor ->
                entity.getOrAdd(ParallaxMotion) { ParallaxMotion() }.also {
                    it.isScrollingHorizontally = isHorizontal
                    it.speedFactor = speedFactor
                    it.selfSpeedX = selfSpeedX
                    it.selfSpeedY = selfSpeedY
                }
            }
        }
        entity
    }
}
