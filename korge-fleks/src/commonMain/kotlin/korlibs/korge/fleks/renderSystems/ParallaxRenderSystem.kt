package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import korlibs.image.color.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.assets.data.AssetConfig.ParallaxLayersInfo.ParallaxLayer
import korlibs.korge.fleks.assets.data.AssetConfig.ParallaxLayersInfo.ParallaxPlane.LineTexture
import korlibs.korge.fleks.assets.data.AssetConfig.ParallaxLayersInfo.Mode
import korlibs.korge.fleks.assets.data.AssetConfig.ParallaxLayersInfo.Mode.*
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
                val parallaxConfig = assetStore.getParallaxLayers(parallaxComponent.name)

                // Draw all background parallax layers
                parallaxConfig.backgroundLayers.forEach { layer ->
                    val layerEntity = parallaxComponent.bgLayerEntities[layer.name]!!
                    val localPositionComponent = layerEntity[PositionComponent]
                    val localRgba = layerEntity[RgbaComponent].rgba

                    drawLayer(
                        global = globalPositionComponent,
                        local = localPositionComponent,
                        parallaxLayer = layer,
                        localRgba, batch, ctx
                    )
                }

                // Draw 2.5 D parallax plane and all attached layers
                parallaxConfig.parallaxPlane?.let { parallaxPlane ->
                    val localPositionComponent = parallaxComponent.parallaxPlane.entity[PositionComponent]
                    val localRgba = parallaxComponent.parallaxPlane.entity[RgbaComponent].rgba

                    // Draw bottom-attached layers
                    parallaxPlane.bottomAttachedLayers.forEachIndexed { index, attachedLayerTexture ->
                        val localScroll = parallaxComponent.parallaxPlane.bottomAttachedLayerPositions[index]
                        drawParallaxPlaneLayer(
                            global = globalPositionComponent,
                            local = localPositionComponent,
                            localScroll = localScroll,
                            parallaxMode = parallaxConfig.mode,
                            repeat = true,
                            attachedLayerTexture,
                            localRgba, batch, ctx
                        )
                    }

                    // Draw 2.5 D parallax plane lines
                    parallaxPlane.lineTextures.forEachIndexed { index, lineTexture ->
                        val localScroll = parallaxComponent.parallaxPlane.linePositions[index]
                        drawParallaxPlaneLayer(
                            global = globalPositionComponent,
                            local = localPositionComponent,
                            localScroll = localScroll,
                            parallaxMode = parallaxConfig.mode,
                            repeat = true,
                            lineTexture,
                            localRgba, batch, ctx
                        )
                    }

                    // Draw top-attached layers
                    parallaxPlane.topAttachedLayers.forEachIndexed { index, attachedLayerTexture ->
                        val localScroll = parallaxComponent.parallaxPlane.topAttachedLayerPositions[index]
                        drawParallaxPlaneLayer(
                            global = globalPositionComponent,
                            local = localPositionComponent,
                            localScroll = localScroll,
                            parallaxMode = parallaxConfig.mode,
                            repeat = true,
                            attachedLayerTexture,
                            localRgba, batch, ctx
                        )
                    }
                }


                // Draw all foreground parallax layers
                parallaxConfig.foregroundLayers.forEach { layer ->
                    val layerEntity = parallaxComponent.fgLayerEntities[layer.name]!!
                    val localPositionComponent = layerEntity[PositionComponent]
                    val localRgba = layerEntity[RgbaComponent].rgba

                    drawLayer(
                        global = globalPositionComponent,
                        local = localPositionComponent,
                        parallaxLayer = layer,
                        localRgba, batch, ctx
                    )
                }
            }
        }
    }
    private fun drawLayer(
        global: Position,
        local: Position,
        parallaxLayer: ParallaxLayer,
        rgba: RGBA,
        batch: BatchBuilder2D,
        ctx: RenderContext
    ) {
        val countH = if (parallaxLayer.repeatX) AppConfig.VIEW_PORT_WIDTH / parallaxLayer.bmpSlice.width else 0
        val countV = if (parallaxLayer.repeatY) AppConfig.VIEW_PORT_HEIGHT / parallaxLayer.bmpSlice.height else 0

        val x = if (countH != 0 && parallaxLayer.speedFactor != null) global.x else global.x + parallaxLayer.targetX
        val y = if (countV != 0 && parallaxLayer.speedFactor != null) global.y else global.y + parallaxLayer.targetY

        val xStart = if (countH > 0) -1 else 0
        val yStart = if (countV > 0) -1 else 0
        for(xIndex in xStart until countH + 1) {  // +1 <== for right side of the view port when scrolling to the left
            for(yIndex in yStart until countV + 1) {
                batch.drawQuad(
                    tex = ctx.getTex(parallaxLayer.bmpSlice),
                    // global + target + (layer index) + local (used for scrolling the layer)
                    x = x + xIndex * parallaxLayer.bmpSlice.width + local.x + local.offsetX,
                    y = y + yIndex * parallaxLayer.bmpSlice.height + local.y + local.offsetY,
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
        parallaxMode: Mode,
        repeat: Boolean,
        lineTexture: LineTexture,
        rgba: RGBA,
        batch: BatchBuilder2D,
        ctx: RenderContext
    ) {
        when (parallaxMode) {
            HORIZONTAL_PLANE -> {
                val targetX = 0  // TODO check if needed
                val targetY = lineTexture.index
                val countH = if (repeat) AppConfig.VIEW_PORT_WIDTH / lineTexture.bmpSlice.width else 0
                val x = if (countH != 0) global.x else global.x + targetX
                val y = global.y + targetY

                for(xIndex in -1 - countH until countH + 1) {
                    batch.drawQuad(
                        tex = ctx.getTex(lineTexture.bmpSlice),
                        x = x + xIndex * lineTexture.bmpSlice.width + AppConfig.VIEW_PORT_WIDTH * 0.5f + localScroll,
                        y = y + local.offsetY,
                        filtering = false,
                        colorMul = rgba
                    )
                }
            }
            VERTICAL_PLANE -> {
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
            NO_PLANE -> {}
        }
    }
}
