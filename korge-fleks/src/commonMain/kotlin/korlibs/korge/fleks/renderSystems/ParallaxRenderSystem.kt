package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import korlibs.image.color.*
import korlibs.image.format.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.assets.data.ParallaxConfig
import korlibs.korge.fleks.assets.data.ParallaxConfigNew.ParallaxLayerConfigNew
import korlibs.korge.fleks.assets.data.ParallaxPlaneTextures
import korlibs.korge.fleks.components.Parallax.Companion.ParallaxComponent
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Rgba.Companion.RgbaComponent
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.render.*


/**
 * Creates a new [ParallaxRenderSystem], allowing to configure with [callback], and attaches the newly created view to the
 * receiver "this".
 *
 * @param world is the Fleks world where the parallax entities are created.
 * @param layerTag is a special Fleks component which tells the [ParallaxRenderSystem] which entities it has to render.
 * @param callback can be used to configure the [ParallaxRenderSystem] object.
 */
class ParallaxRenderSystem(
    private val world: World,
    layerTag: RenderLayerTag
) : RenderSystem {
    private val family: Family = world.family { all(layerTag, PositionComponent, ParallaxComponent) }
    private val assetStore: AssetStore = world.inject(name = "AssetStore")

    override fun render(ctx: RenderContext) {
        // Custom Render Code
        ctx.useBatcher { batch ->

            // Iterate over all entities which should be rendered in this view
            family.forEach { entity ->
                val globalPositionComponent = entity[PositionComponent]
                val parallaxComponent = entity[ParallaxComponent]

                // Draw all background parallax layers
                parallaxComponent.bgLayerEntities.forEach { (layerName, layerEntity) ->
                    val layerTexture = assetStore.getParallaxTexture(layerName)
                    val localPositionComponent = layerEntity[PositionComponent]
                    val localRgba = layerEntity[RgbaComponent].rgba

//                    if (layerName == "parallax_sky") {
//                        println()
//                    }

                    drawLayer(
                        global = globalPositionComponent,
                        local = localPositionComponent,
                        parallaxTexture = layerTexture,
                        localRgba, batch, ctx
                    )
                }

                // Draw parallax plane
                if (parallaxComponent.name != "") {
                    val parallaxPlane = assetStore.getParallaxPlane(parallaxComponent.name)
                    val localPositionComponent = parallaxComponent.parallaxPlaneEntity[PositionComponent]
                    val localRgba = parallaxComponent.parallaxPlaneEntity[RgbaComponent].rgba

                    parallaxPlane.lineTextures.forEach { lineTexture ->
                        drawParallaxPlaneLayer(
                            global = globalPositionComponent,
                            local = localPositionComponent,
                            localScroll = 0f,
                            parallaxMode = ParallaxConfig.Mode.HORIZONTAL_PLANE, // parallax.mode,
                            repeat = true,
                            lineTexture,
                            localRgba, batch, ctx
                        )
                    }
                }

                // Draw all foreground parallax layers
                parallaxComponent.fgLayerEntities.forEach { (layerName, layerEntity) ->
                    val layerTexture = assetStore.getParallaxTexture(layerName)
                    val localPositionComponent = layerEntity[PositionComponent]
                    val localRgba = layerEntity[RgbaComponent].rgba

                    drawLayer(
                        global = globalPositionComponent,
                        local = localPositionComponent,
                        parallaxTexture = layerTexture,
                        localRgba, batch, ctx
                    )
                }
/*
                // Iterate over all parallax layers
                val parallaxMode = parallaxConfig.mode

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
*/
            }
        }
    }

    private fun drawLayer(
        global: Position,
        local: Position,
        parallaxTexture: ParallaxLayerConfigNew,
        rgba: RGBA,
        batch: BatchBuilder2D,
        ctx: RenderContext
    ) {
        val countH = if (parallaxTexture.repeatX) AppConfig.VIEW_PORT_WIDTH / parallaxTexture.layerBmpSlice.width else 0
        val countV = if (parallaxTexture.repeatY) AppConfig.VIEW_PORT_HEIGHT / parallaxTexture.layerBmpSlice.height else 0

        val x = if (countH != 0 && parallaxTexture.speedFactor != null) global.x else global.x + parallaxTexture.targetX
        val y = if (countV != 0 && parallaxTexture.speedFactor != null) global.y else global.y + parallaxTexture.targetY

        val xStart = if (countH > 0) -1 else 0
        val yStart = if (countV > 0) -1 else 0
        for(xIndex in xStart until countH + 1) {  // +1 <== for right side of the view port when scrolling to the left
            for(yIndex in yStart until countV + 1) {
                batch.drawQuad(
                    tex = ctx.getTex(parallaxTexture.layerBmpSlice),
                    // global + target + (layer index) + local (used for scrolling the layer)
                    x = x + xIndex * parallaxTexture.layerBmpSlice.width + local.x + local.offsetX,
                    y = y + yIndex * parallaxTexture.layerBmpSlice.height + local.y + local.offsetY,
                    filtering = false,
                    colorMul = rgba
                )

                //println("x: ${x + xIndex * parallaxTexture.firstFrame.bmpSlice.width + local.x + local.offsetX}")
            }
        }
    }

    private fun drawParallaxPlaneLayer(
        global: Position,
        local: Position,
        localScroll: Float,
        parallaxMode: ParallaxConfig.Mode,
        repeat: Boolean,
        lineTexture: ParallaxPlaneTextures.LineTexture,
        rgba: RGBA,
        batch: BatchBuilder2D,
        ctx: RenderContext
    ) {
        when (parallaxMode) {
            ParallaxConfig.Mode.HORIZONTAL_PLANE -> {
                val targetX = 0  // TODO check if needed
                val targetY = lineTexture.index
                val countH = if (repeat) AppConfig.VIEW_PORT_WIDTH / lineTexture.bmpSlice.width else 0
                val x = if (countH != 0) global.x else global.x + targetX

                for(xIndex in -1 - countH until countH + 1) {
                    batch.drawQuad(
                        tex = ctx.getTex(lineTexture.bmpSlice),
                        x = x + xIndex * lineTexture.bmpSlice.width + AppConfig.VIEW_PORT_WIDTH * 0.5f + localScroll,
                        y = global.y + targetY + local.offsetY,
                        filtering = false,
                        colorMul = rgba
                    )
                }
            }
            ParallaxConfig.Mode.VERTICAL_PLANE -> {
                val targetX = lineTexture.index
                val targetY = 0  // TODO check if needed
                val countV = if (repeat) AppConfig.VIEW_PORT_HEIGHT / lineTexture.bmpSlice.height else 0
                val y = if (countV != 0) global.y else global.y + targetY

                for(yIndex in -1 - countV until countV + 1) {
                    batch.drawQuad(
                        tex = ctx.getTex(lineTexture.bmpSlice),
                        x = global.x + targetX + local.offsetX,
                        y = y + yIndex * lineTexture.bmpSlice.height + AppConfig.VIEW_PORT_HEIGHT * 0.5f + localScroll,
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
}
