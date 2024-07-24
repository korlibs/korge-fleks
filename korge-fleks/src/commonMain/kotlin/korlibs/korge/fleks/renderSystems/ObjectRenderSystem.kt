package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.collection.*
import korlibs.datastructure.iterators.*
import korlibs.image.font.*
import korlibs.image.text.*
import korlibs.korge.annotations.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.render.*
import korlibs.korge.view.*
import korlibs.math.geom.*


/**
 * Creates a new [ObjectRenderSystem], allowing to configure with [callback], and attaches the newly created view to the
 * receiver "this".
 */
inline fun Container.objectRenderSystem(viewPortSize: SizeInt, world: World, layerTag: RenderLayerTag, callback: @ViewDslMarker ObjectRenderSystem.() -> Unit = {}) =
    ObjectRenderSystem(viewPortSize, world, layerTag).addTo(this, callback)

class ObjectRenderSystem(
    private val viewPortSize: SizeInt,
    world: World,
    layerTag: RenderLayerTag,
    private val comparator: EntityComparator = compareEntity(world) { entA, entB -> entA[LayerComponent].layerIndex.compareTo(entB[LayerComponent].layerIndex) }
) : View() {
    private val family: Family = world.family { all(layerTag, LayerComponent, PositionComponent, RgbaComponent)
        .any(LayerComponent, SpriteComponent, LayeredSpriteComponent, TextFieldComponent, SpriteLayersComponent)
    }
    private val assetStore: AssetStore = world.inject(name = "AssetStore")


    @OptIn(KorgeExperimental::class)
    override fun renderInternal(ctx: RenderContext) {
        // Sort sprite and text entities by their layerIndex
        family.sort(comparator)

        // Iterate over all entities which should be rendered in this view
        family.forEach { entity ->
            val (x, y, offsetX, offsetY) = entity[PositionComponent]
            val (rgba) = entity[RgbaComponent]

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
                                    x = x + layerData.targetX - anchorX + layerProps.offsetX,
                                    y = y + layerData.targetY - anchorY + layerProps.offsetY,
                                    filtering = false,
                                    colorMul = layerProps.rgba,
                                    // TODO: Add possibility to use a custom shader - add ShaderComponent or similar
                                    program = null
                                )
                            }
                        }
                    }
                } else {
                    ctx.useBatcher { batch ->
                        // Iterate over all layers of each sprite for the frame number
                        imageFrame.layerData.fastForEach { layerData ->
                            batch.drawQuad(
                                tex = ctx.getTex(layerData.slice),
                                x = x + offsetX + layerData.targetX - anchorX,
                                y = y + offsetY + layerData.targetY - anchorY,
                                filtering = false,
                                colorMul = rgba,
                                // TODO: Add possibility to use a custom shader - add ShaderComponent or similar
                                program = null
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
                            x = x + image.targetX - anchorX + layer.position.x + layer.position.offsetX,
                            y = y + image.targetY - anchorY + layer.position.y + layer.position.offsetY,
                            filtering = false,
                            colorMul = layer.rgba.rgba,
                            // TODO: Add possibility to use a custom shader - add ShaderComponent or similar
                            program = null
                        )
                    }
                }
            }
            // Rendering path for text
            else if (entity has TextFieldComponent) {
                val (text, fontName, textRangeStart, textRangeEnd, width, height, wordWrap,  horizontalAlign, verticalAlign) = entity[TextFieldComponent]
                val offset: Point = Point(offsetX, offsetY)

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
        }
    }

    // Set size of render view to display size
    override fun getLocalBoundsInternal(): Rectangle =
        Rectangle(0, 0, viewPortSize.width, viewPortSize.height)

    init {
        name = layerTag.toString()
    }
}
