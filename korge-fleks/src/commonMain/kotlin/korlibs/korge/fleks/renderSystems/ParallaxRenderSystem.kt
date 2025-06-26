package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import korlibs.datastructure.iterators.*
import korlibs.image.color.*
import korlibs.image.format.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.Parallax.Companion.ParallaxComponent
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Rgba.Companion.RgbaComponent
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.render.*
import korlibs.korge.view.*
import korlibs.math.geom.*

/**
 * Creates a new [ParallaxRenderSystem], allowing to configure with [callback], and attaches the newly created view to the
 * receiver "this".
 *
 * @param world is the Fleks world where the parallax entities are created.
 * @param layerTag is a special Fleks component which tells the [ParallaxRenderSystem] which entities it has to render.
 * @param callback can be used to configure the [ParallaxRenderSystem] object.
 */
inline fun Container.parallaxRenderSystem(world: World, layerTag: RenderLayerTag, callback: @ViewDslMarker ParallaxRenderSystem.() -> Unit = {}) =
    ParallaxRenderSystem(world, layerTag).addTo(this, callback)

class ParallaxRenderSystem(
    private val world: World,
    layerTag: RenderLayerTag
) : View() {
    private val family: Family = world.family { all(layerTag, PositionComponent, ParallaxComponent) }
    private val assetStore: AssetStore = world.inject(name = "AssetStore")

    override fun renderInternal(ctx: RenderContext) {
        // Custom Render Code
        ctx.useBatcher { batch ->

            // Iterate over all entities which should be rendered in this view
            family.forEach { entity ->
                val globalPositionComponent = entity[PositionComponent]
                val parallaxComponent = entity[ParallaxComponent]
                val parallaxDataContainer = assetStore.getBackground(parallaxComponent.name)

                // Iterate over all parallax layers
                val parallaxMode = parallaxDataContainer.config.mode

                // Draw all background parallax layers
                parallaxDataContainer.backgroundLayers?.defaultAnimation?.firstFrame?.layerData?.fastForEachWithIndex { index, layer ->
                    // Check local position and wrap around the texture size
                    val LayerEntity = parallaxComponent.bgLayerEntities[index]
                    val localPositionComponent = LayerEntity[PositionComponent]
                    localPositionComponent.x = wrap(localPositionComponent.x, max = layer.width)
                    localPositionComponent.y = wrap(localPositionComponent.y, max = layer.height)
                    val localRgba = LayerEntity[RgbaComponent].rgba

                    drawLayer(
                        global = globalPositionComponent,
                        local = localPositionComponent,
                        config = parallaxDataContainer.config.backgroundLayers!![index],
                        layer, localRgba, batch, ctx
                    )
                }

                // Draw rear-attached layers
                parallaxDataContainer.attachedLayersRear?.defaultAnimation?.firstFrame?.layerData?.fastForEachWithIndex { index, layer ->
                    val layerConfig = parallaxDataContainer.config.parallaxPlane!!.attachedLayersRear!![index]
                    val layerSize = if (parallaxMode == ParallaxConfig.Mode.HORIZONTAL_PLANE) layer.width
                    else layer.height  // if (parallaxMode == ParallaxConfig.Mode.VERTICAL_PLANE)

                    // Check local position and wrap around the texture size
                    if (layerConfig.repeat) parallaxComponent.attachedLayersRearPositions[index] = wrap(parallaxComponent.attachedLayersRearPositions[index], max = layerSize)
                    val localPositionComponent = parallaxComponent.parallaxPlaneEntity[PositionComponent]
                    val localRgba = parallaxComponent.parallaxPlaneEntity[RgbaComponent].rgba

                    drawParallaxPlaneLayer(
                        global = globalPositionComponent,
                        local = localPositionComponent,
                        localScroll = parallaxComponent.attachedLayersRearPositions[index],
                        parallaxMode = parallaxMode,
                        repeat = layerConfig.repeat,
                        layer, localRgba, batch, ctx
                    )
                }

                // Draw parallax plane
                parallaxDataContainer.parallaxPlane?.imageDatas?.fastForEachWithIndex { index, line ->
                    // Check local position and wrap around the texture size
                    val layer: ImageFrameLayer = line.defaultAnimation.firstFrame.first ?: return@fastForEachWithIndex
                    val layerSize = if (parallaxMode == ParallaxConfig.Mode.HORIZONTAL_PLANE) layer.width
                    else layer.height  // if (parallaxMode == ParallaxConfig.Mode.VERTICAL_PLANE)

                    parallaxComponent.linePositions[index] = wrap(parallaxComponent.linePositions[index], max = layerSize)
                    val localPositionComponent = parallaxComponent.parallaxPlaneEntity[PositionComponent]
                    val localRgba = parallaxComponent.parallaxPlaneEntity[RgbaComponent].rgba

                    drawParallaxPlaneLayer(
                        global = globalPositionComponent,
                        local = localPositionComponent,
                        localScroll = parallaxComponent.linePositions[index],
                        parallaxMode = parallaxMode,
                        repeat = true, layer, localRgba, batch, ctx
                    )
                }

                // Draw front-attached layers
                parallaxDataContainer.attachedLayersFront?.defaultAnimation?.firstFrame?.layerData?.fastForEachWithIndex { index, layer ->
                    val layerConfig = parallaxDataContainer.config.parallaxPlane!!.attachedLayersFront!![index]
                    val layerSize = if (parallaxMode == ParallaxConfig.Mode.HORIZONTAL_PLANE) layer.width
                    else layer.height  // if (parallaxMode == ParallaxConfig.Mode.VERTICAL_PLANE)

                    // Check local position and wrap around the texture size
                    if (layerConfig.repeat) parallaxComponent.attachedLayersFrontPositions[index] = wrap(parallaxComponent.attachedLayersFrontPositions[index], max = layerSize)
                    val localPositionComponent = parallaxComponent.parallaxPlaneEntity[PositionComponent]
                    val localRgba = parallaxComponent.parallaxPlaneEntity[RgbaComponent].rgba

                    drawParallaxPlaneLayer(
                        global = globalPositionComponent,
                        local = localPositionComponent,
                        localScroll = parallaxComponent.attachedLayersFrontPositions[index],
                        parallaxMode = parallaxMode,
                        repeat = layerConfig.repeat,
                        layer, localRgba, batch, ctx
                    )
                }

                // Draw all foreground parallax layers
                parallaxDataContainer.foregroundLayers?.defaultAnimation?.firstFrame?.layerData?.fastForEachWithIndex { index, layer ->
                    // Check local position and wrap around the texture size
                    val layerEntity = parallaxComponent.fgLayerEntities[index]
                    val localPositionComponent = layerEntity[PositionComponent]
                    localPositionComponent.x = wrap(localPositionComponent.x, max = layer.width)
                    localPositionComponent.y = wrap(localPositionComponent.y, max = layer.height)
                    val localRgba = layerEntity[RgbaComponent].rgba

                    drawLayer(
                        global = globalPositionComponent,
                        local = localPositionComponent,
                        config = parallaxDataContainer.config.foregroundLayers!![index],
                        layer, localRgba, batch, ctx
                    )
                }
            }
        }
    }

    // Set size of render view to display size
    override fun getLocalBoundsInternal(): Rectangle = with (world) {
        return Rectangle(0, 0, AppConfig.VIEW_PORT_WIDTH, AppConfig.VIEW_PORT_HEIGHT)
    }

    private fun drawLayer(
        global: Position,
        local: Position,
        config: ParallaxLayerConfig,
        layer: ImageFrameLayer,
        rgba: RGBA,
        batch: BatchBuilder2D,
        ctx: RenderContext
    ) {
        val countH = if (config.repeatX) AppConfig.VIEW_PORT_WIDTH / layer.width else 0
        val countV = if (config.repeatY) AppConfig.VIEW_PORT_HEIGHT / layer.height else 0

        val x = if (countH != 0 && config.speedFactor != null) global.x else global.x + layer.targetX
        val y = if (countV != 0 && config.speedFactor != null) global.y else global.y + layer.targetY

        val xStart = if (countH > 0) -1 else 0
        val yStart = if (countV > 0) -1 else 0
        for(xIndex in xStart until countH + 1) {  // +1 <== for right side of the view port when scrolling to the left
            for(yIndex in yStart until countV + 1) {
                batch.drawQuad(
                    tex = ctx.getTex(layer.slice),
                    // global + target + (layer index) + local (used for scrolling the layer)
                    x = x + xIndex * layer.width + local.x + local.offsetX,
                    y = y + yIndex * layer.height + local.y + local.offsetY,
                    m = globalMatrix,
                    filtering = false,
                    colorMul = rgba
                )
            }
        }
    }

    private fun drawParallaxPlaneLayer(
        global: Position,
        local: Position,
        localScroll: Float,
        parallaxMode: ParallaxConfig.Mode,
        repeat: Boolean,
        layer: ImageFrameLayer,
        rgba: RGBA,
        batch: BatchBuilder2D,
        ctx: RenderContext
    ) {
        when (parallaxMode) {
            ParallaxConfig.Mode.HORIZONTAL_PLANE -> {
                val countH = if (repeat) AppConfig.VIEW_PORT_WIDTH / layer.width else 0
                val x = if (countH != 0) global.x else global.x + layer.targetX

                for(xIndex in -1 - countH until countH + 1) {
                    batch.drawQuad(
                        tex = ctx.getTex(layer.slice),
                        x = x + xIndex * layer.width + AppConfig.VIEW_PORT_WIDTH * 0.5f + localScroll,
                        y = global.y + layer.targetY + local.offsetY,
                        m = globalMatrix,
                        filtering = false,
                        colorMul = rgba
                    )
                }
            }
            ParallaxConfig.Mode.VERTICAL_PLANE -> {
                val countV = if (repeat) AppConfig.VIEW_PORT_HEIGHT / layer.height else 0
                val y = if (countV != 0) global.y else global.y + layer.targetY

                for(yIndex in -1 - countV until countV + 1) {
                    batch.drawQuad(
                        tex = ctx.getTex(layer.slice),
                        x = global.x + layer.targetX + local.offsetX,
                        y = y + yIndex * layer.height + AppConfig.VIEW_PORT_HEIGHT * 0.5f + localScroll,
                        m = globalMatrix,
                        filtering = false,
                        colorMul = rgba
                    )
                }
            }
            ParallaxConfig.Mode.NO_PLANE -> {}
        }
    }

    private fun wrap(value: Float, max: Int, min: Int = 0): Float =
        if (value >= max) value - max else if (value < min) value + max else value

    init {
        name = layerTag.toString()
    }
}
