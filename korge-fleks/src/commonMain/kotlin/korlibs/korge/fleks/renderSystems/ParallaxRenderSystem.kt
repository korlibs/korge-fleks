package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import korlibs.datastructure.iterators.*
import korlibs.image.color.*
import korlibs.image.format.*
import korlibs.korge.assetmanager.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.parallax.*
import korlibs.korge.parallax.ParallaxConfig.Mode.*
import korlibs.korge.render.*
import korlibs.korge.view.*
import korlibs.math.geom.*

/**
 * Creates a new [ParallaxRenderSystem], allowing to configure with [callback], and attaches the newly created view to the
 * receiver "this".
 *
 * @param viewPortSize is the virtual width and height of the game display/screen.
 * @param world is the Fleks world where the parallax entities are created.
 * @param layerTag is a special Fleks component which tells the [ParallaxRenderSystem] which entities it has to render.
 * @param callback can be used to configure the [ParallaxRenderSystem] object.
 */
inline fun Container.parallaxRenderSystem(viewPortSize: SizeInt, world: World, layerTag: RenderLayerTag, callback: @ViewDslMarker ParallaxRenderSystem.() -> Unit = {}) =
    ParallaxRenderSystem(viewPortSize, world, layerTag).addTo(this, callback)

class ParallaxRenderSystem(
    private val viewPortSize: SizeInt,
    world: World,
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
                val (configName, backgroundLayers, parallaxPlane, foregroundLayers) = entity[ParallaxComponent]
                val parallaxDataContainer = assetStore.getBackground(configName)

                // Iterate over all parallax layers
                val parallaxMode = parallaxDataContainer.config.mode

                // Draw all background parallax layers
                parallaxDataContainer.backgroundLayers?.defaultAnimation?.firstFrame?.layerData?.fastForEachWithIndex { index, layer ->
                    // Check local position and wrap around the texture size
                    backgroundLayers[index].position.x = wrap(backgroundLayers[index].position.x, max = layer.width)
                    backgroundLayers[index].position.y = wrap(backgroundLayers[index].position.y, max = layer.height)
                    val localRgba = backgroundLayers[index].rgba.rgba

                    drawLayer(
                        global = globalPositionComponent,
                        local = backgroundLayers[index].position,
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
                    if (layerConfig.repeat) parallaxPlane.attachedLayersRearPositions[index] = wrap(parallaxPlane.attachedLayersRearPositions[index], max = layerSize)
                    val localRgba = parallaxPlane.rgba.rgba

                    drawParallaxPlaneLayer(
                        global = globalPositionComponent,
                        local = parallaxPlane.position,
                        localScroll = parallaxPlane.attachedLayersRearPositions[index],
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

                    parallaxPlane.linePositions[index] = wrap(parallaxPlane.linePositions[index], max = layerSize)
                    val localRgba = parallaxPlane.rgba.rgba

                    drawParallaxPlaneLayer(
                        global = globalPositionComponent,
                        local = parallaxPlane.position,
                        localScroll = parallaxPlane.linePositions[index],
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
                    if (layerConfig.repeat) parallaxPlane.attachedLayersFrontPositions[index] = wrap(parallaxPlane.attachedLayersFrontPositions[index], max = layerSize)
                    val localRgba = parallaxPlane.rgba.rgba

                    drawParallaxPlaneLayer(
                        global = globalPositionComponent,
                        local = parallaxPlane.position,
                        localScroll = parallaxPlane.attachedLayersFrontPositions[index],
                        parallaxMode = parallaxMode,
                        repeat = layerConfig.repeat,
                        layer, localRgba, batch, ctx
                    )
                }

                // Draw all foreground parallax layers
                parallaxDataContainer.foregroundLayers?.defaultAnimation?.firstFrame?.layerData?.fastForEachWithIndex { index, layer ->
                    // Check local position and wrap around the texture size
                    foregroundLayers[index].position.x = wrap(foregroundLayers[index].position.x, max = layer.width)
                    foregroundLayers[index].position.y = wrap(foregroundLayers[index].position.y, max = layer.height)
                    val localRgba = foregroundLayers[index].rgba.rgba

                    drawLayer(
                        global = globalPositionComponent,
                        local = foregroundLayers[index].position,
                        config = parallaxDataContainer.config.foregroundLayers!![index],
                        layer, localRgba, batch, ctx
                    )
                }
            }
        }
    }

    // Set size of render view to display size
    override fun getLocalBoundsInternal(): Rectangle =
        Rectangle(0, 0, viewPortSize.width, viewPortSize.height)

    private fun drawLayer(
        global: PositionComponent,
        local: PositionComponent,
        config: ParallaxLayerConfig,
        layer: ImageFrameLayer,
        rgba: RGBA,
        batch: BatchBuilder2D,
        ctx: RenderContext
    ) {
        val countH = if (config.repeatX) viewPortSize.width / layer.width else 0
        val countV = if (config.repeatY) viewPortSize.height / layer.height else 0

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
        global: PositionComponent,
        local: PositionComponent,
        localScroll: Float,
        parallaxMode: ParallaxConfig.Mode,
        repeat: Boolean,
        layer: ImageFrameLayer,
        rgba: RGBA,
        batch: BatchBuilder2D,
        ctx: RenderContext
    ) {
        when (parallaxMode) {
            HORIZONTAL_PLANE -> {
                val countH = if (repeat) viewPortSize.width / layer.width else 0
                val x = if (countH != 0) global.x else global.x + layer.targetX

                for(xIndex in -1 - countH until countH + 1) {
                    batch.drawQuad(
                        tex = ctx.getTex(layer.slice),
                        x = x + xIndex * layer.width + viewPortSize.width * 0.5f + localScroll,
                        y = global.y + layer.targetY + local.offsetY,
                        m = globalMatrix,
                        filtering = false,
                        colorMul = rgba
                    )
                }
            }
            VERTICAL_PLANE -> {
                val countV = if (repeat) viewPortSize.height / layer.height else 0
                val y = if (countV != 0) global.y else global.y + layer.targetY

                for(yIndex in -1 - countV until countV + 1) {
                    batch.drawQuad(
                        tex = ctx.getTex(layer.slice),
                        x = global.x + layer.targetX + local.offsetX,
                        y = y + yIndex * layer.height + viewPortSize.height * 0.5f + localScroll,
                        m = globalMatrix,
                        filtering = false,
                        colorMul = rgba
                    )
                }
            }
            NO_PLANE -> {}
        }
    }

    private fun wrap(value: Float, max: Int, min: Int = 0): Float =
        if (value >= max) value - max else if (value < min) value + max else value

    init {
        name = layerTag.toString()
    }
}
