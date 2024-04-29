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
 * Creates a new [ParallaxRenderView], allowing to configure with [callback], and attaches the newly created view to the
 * receiver "this".
 *
 * @param viewPortSize is the virtual width and height of the game display/screen.
 * @param world is the Fleks world where the parallax entities are created.
 * @param layerTag is a special Fleks component which tells the [ParallaxRenderView] which entities it has to render.
 * @param callback can be used to configure the [ParallaxRenderView] object.
 */
inline fun Container.parallaxRenderView(viewPortSize: SizeInt, world: World, layerTag: RenderLayerTag, callback: @ViewDslMarker ParallaxRenderView.() -> Unit = {}) =
    ParallaxRenderView(viewPortSize, world, layerTag).addTo(this, callback)

class ParallaxRenderView(
    private val viewPortSize: SizeInt,
    world: World,
    layerTag: RenderLayerTag
) : View() {
    private val family: Family

    override fun renderInternal(ctx: RenderContext) {
        // Custom Render Code
        ctx.useBatcher { batch ->

            // Iterate over all entities which should be rendered in this view
            family.forEach { entity ->
                val (globalX, globalY) = entity[PositionComponent]
                val (config, backgroundLayers, attachedLayersRear, parallaxPlane, attachedLayersFront, foregroundLayers) = entity[ParallaxComponent]
                val (rgba) = entity[RgbaComponent]
                val parallaxDataContainer = AssetStore.getBackground(config.name)

                // Iterate over all parallax layers
                val parallaxMode = parallaxDataContainer.config.mode

                // Draw all background parallax layers
                parallaxDataContainer.backgroundLayers?.defaultAnimation?.firstFrame?.layerData?.fastForEachWithIndex { index, layer ->
                    // Check local position and wrap around the texture size
                    backgroundLayers[index].position.x = wrap(backgroundLayers[index].position.x, max = layer.width)
                    backgroundLayers[index].position.y = wrap(backgroundLayers[index].position.y, max = layer.height)

                    drawLayer(
                        globalX, globalY,
                        localX = backgroundLayers[index].position.x,
                        localY = backgroundLayers[index].position.y,
                        config = parallaxDataContainer.config.backgroundLayers!![index],
                        layer, rgba, batch, ctx
                    )
                }

                // Draw rear-attached layers
                parallaxDataContainer.attachedLayersRear?.defaultAnimation?.firstFrame?.layerData?.fastForEachWithIndex { index, layer ->
                    val layerConfig = parallaxDataContainer.config.parallaxPlane!!.attachedLayersRear!![index]
                    // Check local position and wrap around the texture size
                    if (layerConfig.repeat) {
                        attachedLayersRear[index].position.x = wrap(attachedLayersRear[index].position.x, max = layer.width)
                        attachedLayersRear[index].position.y = wrap(attachedLayersRear[index].position.y, max = layer.height)
                    }

                    drawAttachedLayer(
                        globalX, globalY,
                        localX = attachedLayersRear[index].position.x,
                        localY = attachedLayersRear[index].position.y,
                        parallaxMode = parallaxMode,
                        config = layerConfig,
                        layer, rgba, batch, ctx
                    )
                }

                // Draw parallax plane
                parallaxDataContainer.parallaxPlane?.imageDatas?.fastForEachWithIndex { index, line ->
                    // Check local position and wrap around the texture size
                    val layer: ImageFrameLayer = line.defaultAnimation.firstFrame.first ?: return@fastForEachWithIndex
                    if (parallaxMode == ParallaxConfig.Mode.HORIZONTAL_PLANE) parallaxPlane[index].position.x = wrap(parallaxPlane[index].position.x, max = layer.width)
                    else if (parallaxMode == ParallaxConfig.Mode.VERTICAL_PLANE) parallaxPlane[index].position.y = wrap(parallaxPlane[index].position.y, max = layer.height)

                    drawParallaxLine(
                        globalX, globalY,
                        localX = parallaxPlane[index].position.x,
                        localY = parallaxPlane[index].position.y,
                        parallaxMode = parallaxMode,
                        layer, rgba, batch, ctx
                    )
                }

                // Draw front-attached layers
                parallaxDataContainer.attachedLayersFront?.defaultAnimation?.firstFrame?.layerData?.fastForEachWithIndex { index, layer ->
                    val layerConfig = parallaxDataContainer.config.parallaxPlane!!.attachedLayersFront!![index]
                    // Check local position and wrap around the texture size
                    if (layerConfig.repeat) {
                        attachedLayersFront[index].position.x = wrap(attachedLayersFront[index].position.x, max = layer.width)
                        attachedLayersFront[index].position.y = wrap(attachedLayersFront[index].position.y, max = layer.height)
                    }

                    drawAttachedLayer(
                        globalX, globalY,
                        localX = attachedLayersFront[index].position.x,
                        localY = attachedLayersFront[index].position.y,
                        parallaxMode = parallaxMode,
                        config = layerConfig,
                        layer, rgba, batch, ctx
                    )
                }

                // Draw all foreground parallax layers
                parallaxDataContainer.foregroundLayers?.defaultAnimation?.firstFrame?.layerData?.fastForEachWithIndex { index, layer ->
                    // Check local position and wrap around the texture size
                    foregroundLayers[index].position.x = wrap(foregroundLayers[index].position.x, max = layer.width)
                    foregroundLayers[index].position.y = wrap(foregroundLayers[index].position.y, max = layer.height)

                    drawLayer(
                        globalX, globalY,
                        localX = foregroundLayers[index].position.x,
                        localY = foregroundLayers[index].position.y,
                        config = parallaxDataContainer.config.foregroundLayers!![index],
                        layer, rgba, batch, ctx
                    )
                }
            }
        }
    }

    // Set size of render view to display size
    override fun getLocalBoundsInternal(): Rectangle =
        Rectangle(0, 0, viewPortSize.width, viewPortSize.height)

    private fun drawLayer(
        globalX: Float,
        globalY: Float,
        localX: Float,
        localY: Float,

        config: ParallaxLayerConfig,
        layer: ImageFrameLayer,
        rgba: RGBA,
        batch: BatchBuilder2D,
        ctx: RenderContext
    ) {
        val countH = if (config.repeatX) viewPortSize.width / layer.width else 0
        val countV = if (config.repeatY) viewPortSize.height / layer.height else 0

        val x = if (countH != 0 && config.speedFactor != null) globalX else globalX + layer.targetX
        val y = if (countV != 0 && config.speedFactor != null) globalY else globalY + layer.targetY

        val xStart = if (countH > 0) -1 else 0
        val yStart = if (countV > 0) -1 else 0
        for(xIndex in xStart until countH + 1) {
            for(yIndex in yStart until countV + 1) {
                batch.drawQuad(
                    tex = ctx.getTex(layer.slice),
                    x = x + xIndex * layer.width + localX,
                    y = y + yIndex * layer.height + localY,
                    m = globalMatrix,
                    filtering = false,
                    colorMul = rgba
                )
            }
        }
    }

    private fun drawParallaxLine(
        globalX: Float,
        globalY: Float,
        localX: Float,
        localY: Float,
        parallaxMode: ParallaxConfig.Mode,
        line: ImageFrameLayer,
        rgba: RGBA,
        batch: BatchBuilder2D,
        ctx: RenderContext
    ) {
        when (parallaxMode) {
            HORIZONTAL_PLANE -> {
                val countH = viewPortSize.width / line.width
                for(xIndex in -1 - countH until countH + 1) {
                    batch.drawQuad(
                        tex = ctx.getTex(line.slice),
                        x = globalX + xIndex * line.width + viewPortSize.width * 0.5f + localX,
                        y = globalY + line.targetY,
                        m = globalMatrix,
                        filtering = false,
                        colorMul = rgba
                    )
                }
            }
            VERTICAL_PLANE -> {
                val countV = viewPortSize.height / line.height
                for(yIndex in -1 - countV until countV + 1) {
                    batch.drawQuad(
                        tex = ctx.getTex(line.slice),
                        x = globalX + line.targetX,
                        y = globalY + yIndex * line.height + viewPortSize.height * 0.5f + localY,
                        m = globalMatrix,
                        filtering = false,
                        colorMul = rgba
                    )
                }
            }
            NO_PLANE -> {}
        }
    }

    private fun drawAttachedLayer(
        globalX: Float,
        globalY: Float,
        localX: Float,
        localY: Float,

        parallaxMode: ParallaxConfig.Mode,
        config: ParallaxAttachedLayerConfig,
        layer: ImageFrameLayer,
        rgba: RGBA,
        batch: BatchBuilder2D,
        ctx: RenderContext
    ) {
        when (parallaxMode) {
            HORIZONTAL_PLANE -> {
                val countH = if (config.repeat) viewPortSize.width / layer.width else 0
                val x = if (countH != 0) globalX else globalX + layer.targetX

                for(xIndex in -countH - 1 until countH + 1) {
                    batch.drawQuad(
                        tex = ctx.getTex(layer.slice),
                        x = x + xIndex * layer.width + viewPortSize.width * 0.5f + localX,
                        y = globalY + layer.targetY,
                        filtering = false,
                        colorMul = rgba
                    )
                }
            }
            VERTICAL_PLANE -> {
                val countV = if (config.repeat) viewPortSize.height / layer.height else 0
                val y = if (countV != 0) globalY else globalY + layer.targetY

                for(yIndex in -countV until countV) {
                    batch.drawQuad(
                        tex = ctx.getTex(layer.slice),
                        x = globalX + layer.targetX,
                        y = y + yIndex * layer.height + viewPortSize.height * 0.5f + localY,
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
        family = world.family { all(layerTag, PositionComponent, ParallaxComponent, RgbaComponent)}
    }
}
