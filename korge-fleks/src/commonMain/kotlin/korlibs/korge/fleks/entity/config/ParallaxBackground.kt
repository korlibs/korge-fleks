package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.assetmanager.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.entity.EntityFactory.EntityConfig


data class ParallaxBackground(
    override val name: String,
    
    private val assetName: String,
    private val layerTag: RenderLayerTag,
    private val x: Float = 0f,
    private val y: Float = 0f,
    private val tint: RgbaComponent.Rgb = RgbaComponent.Rgb.WHITE,
    private val alpha: Float = 1f
) : EntityConfig {

    private val numberBackgroundLayers = AssetStore.getBackground(assetName).config.backgroundLayers?.size ?: 0
    private val numberAttachedRearLayers = AssetStore.getBackground(assetName).config.parallaxPlane?.attachedLayersRear?.size ?: 0
    private val numberParallaxPlaneLines = AssetStore.getBackground(assetName).parallaxPlane?.imageDatas?.size ?: 0
    private val numberAttachedFrontLayers = AssetStore.getBackground(assetName).config.parallaxPlane?.attachedLayersFront?.size ?: 0
    private val numberForegroundLayers = AssetStore.getBackground(assetName).config.foregroundLayers?.size ?: 0

    // Game object related functions
//    fun create(world: World, config: Identifier) = configureParallaxBackgroundFct(world, world.entity(), config)
    fun getEntityByLayerName(world: World, entity: Entity, name: String): Entity = with (world) {
        return entity[SubEntitiesComponent][name]
    }
/*
    override val functionImpl = fun(world: World, entity: Entity, assetConfig: Identifier): Entity = with(world) {
        println("Re-configure attached parallax Layers")

        val config = AssetStore.getBackground(assetConfig.name).config
        val isHorizontal = config.mode == ParallaxConfig.Mode.HORIZONTAL_PLANE
        val view = KorgeViewCache[entity] as ParallaxDataView
        val layerMap = entity[SubEntitiesComponent]

        config.parallaxPlane?.let { planeConf ->
            val offset = planeConf.offset
            val selfSpeedX = if (isHorizontal) planeConf.selfSpeed else 0.0
            val selfSpeedY = if (!isHorizontal) planeConf.selfSpeed else 0.0

            // Update only attached layers because they might change their speed depending on their position on the ground plane
            planeConf.attachedLayersFront?.fastForEach { conf ->
                val layer = KorgeViewCache.getLayer(entity, conf.name)
                configureSubEntityForLayer(
                    world = world,
                    entity = layerMap[conf.name],
                    layerName = conf.name,
//                    speedFactor = view.parallaxPlaneSpeedFactor[layer.y.toInt() - offset + (layer.height.toInt().takeIf { conf.attachBottomRight } ?: 0)],
                    selfSpeedX = selfSpeedX, selfSpeedY = selfSpeedY,
                    isHorizontal = isHorizontal
                )
            }
            planeConf.attachedLayersRear?.fastForEach { conf ->
                val layer = KorgeViewCache.getLayer(entity, conf.name)
                configureSubEntityForLayer(
                    world = world,
                    entity = layerMap[conf.name],
                    layerName = conf.name,
//                    speedFactor = view.parallaxPlaneSpeedFactor[layer.y.toInt() - offset + (layer.height.toInt().takeIf { conf.attachBottomRight } ?: 0)],
                    selfSpeedX = selfSpeedX, selfSpeedY = selfSpeedY,
                    isHorizontal = isHorizontal
                )
            }
        }
        entity
    }
*/
    override val functionImpl = fun(world: World, entity: Entity): Entity = with(world) {
        entity.configure {
        it += PositionComponent(
            x = this@ParallaxBackground.x,
            y = this@ParallaxBackground.y
        )  // global position for the whole parallax background
        it += ParallaxComponent(
            config = assetName,
            backgroundLayers = List(numberBackgroundLayers) { ParallaxComponent.Layer() },
            attachedLayersRear = List(numberAttachedRearLayers) { ParallaxComponent.Layer() },
            parallaxPlane = List(numberParallaxPlaneLines) { ParallaxComponent.Layer() },
            attachedLayersFront = List(numberAttachedFrontLayers) { ParallaxComponent.Layer() },
            foregroundLayers = List(numberForegroundLayers) { ParallaxComponent.Layer() }
        )
        it += RgbaComponent().apply {
            tint = this@ParallaxBackground.tint
            alpha = this@ParallaxBackground.alpha
        }
        it += layerTag
        it += MotionComponent(
// TODO                velocityX = 5f  // world units per second
        )
        // All sub-entity IDs are here for quick lookup by its layer name and for recycling of the overall background entity object
// TODO            it += SubEntitiesComponent(moveWithParent = false)
        }
        entity
    }

/*
        // Once the base ParallaxDataView is created with above base entity we can access it from the cache
        val view = KorgeViewCache[entity] as ParallaxDataView
        val layerMap = entity[SubEntitiesComponent]

        val parallaxDataContainer = AssetStore.getBackground(parallaxConfig.assetName.name).config
        val isHorizontal = parallaxDataContainer.mode == ParallaxConfig.Mode.HORIZONTAL_PLANE

        parallaxDataContainer.backgroundLayers?.fastForEach { conf ->
            layerMap.entities[conf.name] = createSubEntityForLayer(world, entity, conf.name,
                speedFactor = conf.speedFactor, selfSpeedX = conf.selfSpeedX, selfSpeedY = conf.selfSpeedY,
                isHorizontal = isHorizontal)
        }
        parallaxDataContainer.foregroundLayers?.fastForEach { conf ->
            layerMap.entities[conf.name] = createSubEntityForLayer(world, entity, conf.name,
                speedFactor = conf.speedFactor, selfSpeedX = conf.selfSpeedX, selfSpeedY = conf.selfSpeedY,
                isHorizontal = isHorizontal)
        }
        parallaxDataContainer.parallaxPlane?.let { planeConf ->
            val offset = planeConf.offset
            val selfSpeedX = if (isHorizontal) planeConf.selfSpeed else 0.0
            val selfSpeedY = if (!isHorizontal) planeConf.selfSpeed else 0.0
            planeConf.attachedLayersFront?.fastForEach { conf ->
                val layer = KorgeViewCache.getLayer(entity, conf.name)
                layerMap.entities[conf.name] = createSubEntityForLayer(world, entity, conf.name,
//                    speedFactor = view.parallaxPlaneSpeedFactor[layer.y.toInt() - offset + (layer.height.toInt().takeIf { conf.attachBottomRight } ?: 0)],
                    selfSpeedX = selfSpeedX, selfSpeedY = selfSpeedY,
                    isHorizontal = isHorizontal)
            }
            planeConf.attachedLayersRear?.fastForEach { conf ->
                val layer = KorgeViewCache.getLayer(entity, conf.name)
                layerMap.entities[conf.name] = createSubEntityForLayer(world, entity, conf.name,
//                    speedFactor = view.parallaxPlaneSpeedFactor[layer.y.toInt() - offset + (layer.height.toInt().takeIf { conf.attachBottomRight } ?: 0)],
                    selfSpeedX = selfSpeedX, selfSpeedY = selfSpeedY,
                    isHorizontal = isHorizontal)
            }

            view.parallaxLines.fastForEachWithIndex { index, it -> it?.let { line ->
                // Here we need to take speedFactor per line into account
                layerMap.entities[planeConf.name + index] = createSubEntityForLayer(world, entity, layerLine = index,
//                    speedFactor = view.parallaxPlaneSpeedFactor[line.y.toInt() - offset],
                    selfSpeedX = selfSpeedX, selfSpeedY = selfSpeedY,
                    isHorizontal = isHorizontal)
            }}
        }
*/

    init {
        EntityFactory.register(this)
    }
/*
    private fun createSubEntityForLayer(world: World, parentEntity: Entity, layerName: String? = null, layerLine: Int? = null, speedFactor: Float? = null,
                                        selfSpeedX: Double = 0.0, selfSpeedY: Double = 0.0, isHorizontal: Boolean = true) : Entity {
        return configureSubEntityForLayer(world, world.entity(), parentEntity, layerName, layerLine, speedFactor, selfSpeedX, selfSpeedY, isHorizontal)
    }

    private fun configureSubEntityForLayer(world: World, entity: Entity, parentEntity: Entity? = null, layerName: String?, layerLine: Int? = null, speedFactor: Float? = null,
                                           selfSpeedX: Double = 0.0, selfSpeedY: Double = 0.0, isHorizontal: Boolean = true) : Entity  = with(world) {
        entity.configure { entity ->
            entity.getOrAdd(SpecificLayerComponent) { SpecificLayerComponent() }.also {
                parentEntity?.let { parentEntity -> it.parentEntity = parentEntity }
                it.spriteLayer = layerName
                it.parallaxPlaneLine = layerLine
            }
            entity.getOrAdd(PositionComponent) { PositionComponent() }
            speedFactor?.let { speedFactor ->
//                entity.getOrAdd(ParallaxMotionComponent) { ParallaxMotionComponent() }.also {
//                    it.isScrollingHorizontally = isHorizontal
//                    it.speedFactor = speedFactor
//                    it.selfSpeedX = selfSpeedX
//                    it.selfSpeedY = selfSpeedY
//                }
            }
        }
        entity
    }
*/
}
