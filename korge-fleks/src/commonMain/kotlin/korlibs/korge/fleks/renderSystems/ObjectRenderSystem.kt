package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.collection.*
import korlibs.datastructure.iterators.*
import korlibs.image.bitmap.*
import korlibs.image.font.*
import korlibs.image.text.*
import korlibs.korge.annotations.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.render.*
import korlibs.korge.view.*
import korlibs.math.geom.*


/**
 * Creates a new [ObjectRenderSystem], allowing to configure with [callback], and attaches the newly created view to the
 * receiver "this".
 *
 * HINT: This renderer is preliminary and does not use caching of geometry or vertices. It might be replaced by
 * renderers from KorGE 6.
 */
inline fun Container.objectRenderSystem(world: World, layerTag: RenderLayerTag, callback: @ViewDslMarker ObjectRenderSystem.() -> Unit = {}) =
    ObjectRenderSystem(world, layerTag).addTo(this, callback)

class ObjectRenderSystem(
    private val world: World,
    layerTag: RenderLayerTag,
    private val comparator: EntityComparator = compareEntity(world) { entA, entB -> entA[LayerComponent].layerIndex.compareTo(entB[LayerComponent].layerIndex) }
) : View() {
    private val family: Family = world.family { all(layerTag, PositionComponent, LayerComponent, RgbaComponent)
        .any(PositionComponent, LayerComponent, SpriteComponent, LayeredSpriteComponent, TextFieldComponent, SpriteLayersComponent, NinePatchComponent)
    }
    private val assetStore: AssetStore = world.inject(name = "AssetStore")

    @OptIn(KorgeExperimental::class)
    override fun renderInternal(ctx: RenderContext) {
        val camera: Entity = world.getMainCamera()

        // Sort sprite and text entities by their layerIndex
        family.sort(comparator)

        // Iterate over all entities which should be rendered in this view
        family.forEach { entity ->
            val (rgba) = entity[RgbaComponent]
            val entityPosition = entity[PositionComponent]

            val position: PositionComponent = if (entity has ScreenCoordinatesTag) {
                // Take over entity coordinates
                entityPosition
            } else {
                // Transform world coordinates to screen coordinates
                entityPosition.run {  world.convertToScreenCoordinates(camera) }
            }

            // Rendering path for sprites
            if (entity has SpriteComponent) {
                val (name, anchorX, anchorY, animation, frameIndex) = entity[SpriteComponent]
                val imageFrame = assetStore.getImageFrame(name, animation, frameIndex)

                if (entity has SpriteLayersComponent) {
                    val (layerMap) = entity[SpriteLayersComponent]
                    ctx.useBatcher { batch ->
                        // Iterate over all layers of each sprite for the frame number
                        imageFrame.layerData.fastForEach { layerData ->
                            val layerName = layerData.layer.name ?: ""

                            layerMap[layerName]?.let { layerProps ->

                                batch.drawQuad(
                                    tex = ctx.getTex(layerData.slice),
                                    x = position.x + layerData.targetX - anchorX + layerProps.offsetX,
                                    y = position.y + layerData.targetY - anchorY + layerProps.offsetY,
                                    filtering = false,
                                    colorMul = layerProps.rgba,
                                    program = null // Possibility to use a custom shader - add ShaderComponent or similar
                                )
                            }
                        }
                    }
                } else {
                    ctx.useBatcher { batch ->
                        // Iterate over all layers of each sprite for the frame number
                        imageFrame.layerData.fastForEach { layerData ->
                            val px = position.x + position.offsetX + layerData.targetX - anchorX
                            val py = position.y + position.offsetY + layerData.targetY - anchorY
                            batch.drawQuad(
                                tex = ctx.getTex(layerData.slice),
                                x = px,
                                y = py,
                                filtering = false,
                                colorMul = rgba,
                                program = null // Possibility to use a custom shader - add ShaderComponent or similar
                            )
                        }
                    }
                }
            }
            else if (entity has LayeredSpriteComponent) {
                val (name, anchorX, anchorY, animation, frameIndex, _, _, _, _, _, _, layerList) = entity[LayeredSpriteComponent]
                val imageFrame = assetStore.getImageFrame(name, animation, frameIndex)

                ctx.useBatcher { batch ->
                    // Iterate over all layers of each sprite for the frame number
                    layerList.fastForEachWithIndex { index, layer ->
                        // Get image data for specific layer from asset store
                        val image = imageFrame.layerData[index]
                        batch.drawQuad(
                            tex = ctx.getTex(image.slice),
                            x = position.x + image.targetX - anchorX + layer.position.x + layer.position.offsetX,
                            y = position.y + image.targetY - anchorY + layer.position.y + layer.position.offsetY,
                            filtering = false,
                            colorMul = layer.rgba.rgba,
                            program = null // Possibility to use a custom shader - add ShaderComponent or similar
                        )
                    }
                }
            }
            // Rendering path for text (not optimized - no caching)
            else if (entity has TextFieldComponent) {
                val (text, fontName, textRangeStart, textRangeEnd, width, height, wordWrap,  horizontalAlign, verticalAlign) = entity[TextFieldComponent]
                val offset: Point = Point(position.offsetX, position.offsetY)

                renderCtx2d(ctx) { render ->
                    var n = 0
                    RichTextData(
                        text = text,
                        font = assetStore.getFont(fontName)
                    ).place(
                        bounds = Rectangle(x, y, width, height),
                        wordWrap = wordWrap,
                        includePartialLines = false,
                        ellipsis = null,
                        fill = null,
                        stroke = null,
                        align = TextAlignment(horizontalAlign, verticalAlign),
                        includeFirstLineAlways = true
                    ).fastForEach {
                        render.drawText(
                            it.text,
                            it.font.lazyBitmap,
                            it.size,
                            it.pos + offset,
                            color = rgba,
                            baseline = true,
                            textRangeStart = textRangeStart - n,
                            textRangeEnd = textRangeEnd - n,
                            filtering = false,
                        )
                        n += it.text.length
                    }
                }
            }
            // Rendering path for 9-patch graphic (not optimized - no caching)
            else if (entity has NinePatchComponent) {
                val (name, width, height) = entity[NinePatchComponent]
                val ninePatch = assetStore.getNinePatch(name)

                val numQuads = ninePatch.info.totalSegments
                val indices = TexturedVertexArray.quadIndices(numQuads)
                val tva = TexturedVertexArray(numQuads * 4, indices)
                var index = 0
                val viewBounds = RectangleInt(x.toInt(), y.toInt(), width.toInt(), height.toInt())
                ninePatch.info.computeScale(viewBounds) { segment, xx, yy, ww, hh ->
                    val bmpSlice = ninePatch.getSegmentBmpSlice(segment)
                    tva.quad(index++ * 4,
                        xx.toFloat(), yy.toFloat(),
                        ww.toFloat(), hh.toFloat(),
                        Matrix.IDENTITY, bmpSlice, rgba
                    )
                }

                ctx.useBatcher { batch ->
                    batch.drawVertices(tva, ctx.getTex(ninePatch.content.bmp), smoothing = false, blendMode)
                }
            }
        }
    }

    // Set size of render view to display size
    override fun getLocalBoundsInternal(): Rectangle = with (world) {
        val camera: Entity = getMainCamera()
        val cameraViewPort = camera[SizeIntComponent]
        return Rectangle(0, 0, cameraViewPort.width, cameraViewPort.height)
    }

    init {
        name = layerTag.toString()
    }
}
