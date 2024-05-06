package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.collection.*
import korlibs.datastructure.iterators.*
import korlibs.image.font.*
import korlibs.image.text.*
import korlibs.korge.annotations.*
import korlibs.korge.assetmanager.*
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
    private val family: Family

    @OptIn(KorgeExperimental::class)
    override fun renderInternal(ctx: RenderContext) {
        // Sort sprite and text entities by their layerIndex
        family.sort(comparator)

        // Iterate over all entities which should be rendered in this view
        family.forEach { entity ->
            val (x, y) = entity[PositionComponent]
            val (rgba) = entity[RgbaComponent]

            // Rendering path for sprites
            if (entity has SpriteComponent) {
                val (name, anchorX, anchorY, animation, frameIndex) = entity[SpriteComponent]
                val imageFrame = AssetStore.getImageFrame(name, animation, frameIndex)

                ctx.useBatcher { batch ->
                    // Iterate over all layers of each sprite for the frame number
                    imageFrame.layerData.fastForEachReverse { layer ->
                        batch.drawQuad(
                            tex = ctx.getTex(layer.slice),
                            x = x + layer.targetX - anchorX,
                            y = y + layer.targetY - anchorY,
                            filtering = false,
                            colorMul = rgba,
                            // TODO: Add possiblility to use a custom shader - add ShaderComponent or similar
                            program = null
                        )
                    }
                }
            }
            // Rendering path for text
            else if (entity has TextComponent) {
                val (text, fontName, textRangeStart, textRangeEnd, width, height, wordWrap,  horizontalAlign, verticalAlign) = entity[TextComponent]

                renderCtx2d(ctx) { render ->
                    var n = 0
                    RichTextData(
                        text = text,
                        font = AssetStore.getFont(fontName)
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
                            it.pos,
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
        family = world.family {
            all(layerTag, LayerComponent, PositionComponent, RgbaComponent)
                .any(LayerComponent, SpriteComponent, TextComponent)
        }
    }
}
