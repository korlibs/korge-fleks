package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.collection.*
import korlibs.datastructure.iterators.*
import korlibs.graphics.shader.Program
import korlibs.image.bitmap.*
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.font.*
import korlibs.image.text.*
import korlibs.korge.blend.BlendMode
import korlibs.korge.annotations.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.Layer.Companion.LayerComponent
import korlibs.korge.fleks.components.NinePatch.Companion.NinePatchComponent
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Position.Companion.staticPositionComponent
import korlibs.korge.fleks.components.Rgba.Companion.RgbaComponent
import korlibs.korge.fleks.components.Sprite.Companion.SpriteComponent
import korlibs.korge.fleks.components.SpriteLayers.Companion.SpriteLayersComponent
import korlibs.korge.fleks.components.TextField.Companion.TextFieldComponent
import korlibs.korge.fleks.components.TileMap.Companion.TileMapComponent
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.AppConfig
import korlibs.korge.fleks.utils.getMainCameraOrNull
import korlibs.korge.render.*
import korlibs.math.geom.*


interface RenderSystem {
    fun render(ctx: RenderContext)
}

/**
 * A [ObjectRenderSystem] extending [RenderSystem] is responsible for rendering all entities which have a [PositionComponent],
 * [LayerComponent] and [RgbaComponent] and one of the following components:
 * - [SpriteComponent]
 * - [TextFieldComponent]
 * - [SpriteLayersComponent]
 * - [NinePatchComponent]
 * - [TileMapComponent]
 * This system is used to render all game objects which are not part of the level map or parallax background.
 *
 * HINT: This renderer does not use caching of geometry or vertices.
 */

class ObjectRenderSystem(
    private val world: World,
    layerTag: RenderLayerTag,
    private val comparator: EntityComparator = compareEntity(world) { entA, entB -> entA[LayerComponent].index.compareTo(entB[LayerComponent].index) }
) : RenderSystem {
    private val family: Family = world.family { all(layerTag, PositionComponent, LayerComponent, RgbaComponent)
        .any(PositionComponent, LayerComponent, SpriteComponent, TextFieldComponent, SpriteLayersComponent, NinePatchComponent, TileMapComponent)
    }
    private val assetStore: AssetStore = world.inject(name = "AssetStore")
    private val position: Position = staticPositionComponent {}

    @OptIn(KorgeExperimental::class)
    override fun render(ctx: RenderContext) {
        val camera: Entity = world.getMainCameraOrNull() ?: return

        // Sort sprite and text entities by their layerIndex
        family.sort(comparator)

        // Iterate over all entities which should be rendered in this view
        family.forEach { entity ->
            val rgba = entity[RgbaComponent].rgba
            val entityPosition = entity[PositionComponent]

            // Take over entity position
            position.init(entityPosition)

            if (entity hasNo ScreenCoordinatesTag) {
                // Transform world coordinates to screen coordinates
                position.run { world.convertToScreenCoordinates(camera) }
            }

            // Rendering path for sprites
            if (entity has SpriteComponent && entity[SpriteComponent].visible) {
                val spriteComponent = entity[SpriteComponent]
                val sprite = assetStore.getTextureSprite(spriteComponent.name)
                val texture = sprite[spriteComponent.frameIndex]

                ctx.useBatcher { batch ->
                    val px = position.x + position.offsetX + (if (spriteComponent.flipX) (sprite.width - texture.targetX - texture.bmpSlice.width) else texture.targetX) - spriteComponent.anchorX
                    val py = position.y + position.offsetY + (if (spriteComponent.flipY) (sprite.height - texture.targetY - texture.bmpSlice.height) else texture.targetY) - spriteComponent.anchorY
                    if (spriteComponent.flipX) {
                        batch.drawQuadFlippedX(  // mirror texture horizontally
                            tex = ctx.getTex(texture.bmpSlice),
                            x = px,
                            y = py,
                            filtering = false,
                            colorMul = rgba,
                            program = null // Possibility to use a custom shader - add ShaderComponent or similar
                        )
                    } else {
                        batch.drawQuad(
                            tex = ctx.getTex(texture.bmpSlice),
                            x = px,
                            y = py,
                            filtering = false,
                            colorMul = rgba,
                            program = null
                        )
                    }
                }
            }
            // Rendering path for text (not optimized - no caching)
            else if (entity has TextFieldComponent) {
                val textFieldComponent = entity[TextFieldComponent]
                val offset = Point(position.offsetX, position.offsetY)

                ctx.useCtx2d { render ->  //context ->
//                    context.keep {
//                        context.size = Size(this@renderCtx2d.width, this@renderCtx2d.height)
//                        context.blendMode = renderBlendMode
//                        context.multiplyColor = renderColorMul
//                        context.setMatrix(globalMatrix)
//                        block(context)
//                    }

//                renderCtx2d(ctx) { render ->
                    var n = 0
                    RichTextData(
                        text = textFieldComponent.text,
                        font = assetStore.getFont(textFieldComponent.fontName)
                    ).place(
                        bounds = Rectangle(position.x, position.y, textFieldComponent.width, textFieldComponent.height),
                        wordWrap = textFieldComponent.wordWrap,
                        includePartialLines = false,
                        ellipsis = null,
                        fill = null,
                        stroke = null,
                        align = TextAlignment(textFieldComponent.horizontalAlign, textFieldComponent.verticalAlign),
                        includeFirstLineAlways = true
                    ).fastForEach {
                        render.drawText(
                            it.text,
                            it.font.lazyBitmap,
                            it.size,
                            it.pos + offset,
                            color = rgba,
                            baseline = true,
                            textRangeStart = textFieldComponent.textRangeStart - n,
                            textRangeEnd = textFieldComponent.textRangeEnd - n,
                            filtering = false,
                        )
                        n += it.text.length
                    }
                }
            }
            // Rendering path for 9-patch graphic (not optimized - no caching)
            else if (entity has NinePatchComponent) {
                val ninePatchComponent = entity[NinePatchComponent]
                val ninePatch = assetStore.getNinePatch(ninePatchComponent.name)

                val numQuads = ninePatch.info.totalSegments
                val indices = TexturedVertexArray.quadIndices(numQuads)
                val tva = TexturedVertexArray(numQuads * 4, indices)
                var index = 0
                val viewBounds = RectangleInt(position.x.toInt(), position.y.toInt(), ninePatchComponent.width.toInt(), ninePatchComponent.height.toInt())
                ninePatch.info.computeScale(viewBounds) { segment, xx, yy, ww, hh ->
                    val bmpSlice = ninePatch.getSegmentBmpSlice(segment)
                    tva.quad(index++ * 4,
                        xx.toFloat(), yy.toFloat(),
                        ww.toFloat(), hh.toFloat(),
                        Matrix.IDENTITY, bmpSlice, rgba
                    )
                }

                ctx.useBatcher { batch ->
                    batch.drawVertices(tva, ctx.getTex(ninePatch.content.bmp), smoothing = false, BlendMode.NORMAL)
                }
            }
            else if (entity has TileMapComponent) {
                val tileMapComponent = entity[TileMapComponent]

                tileMapComponent.layerNames.forEach { layerName ->
                    val tileMap = assetStore.getTileMapData(tileMapComponent.levelName).getTileMapLayer(layerName)

                    val tileSet = tileMap.tileSet
                    val gridWidth = tileSet.width
                    val gridHeight = tileSet.height
                    val offsetScale = tileMap.offsetScale

                    // Draw only visible tiles
                    val tileMapPosX: Float = position.x + position.offsetX
                    val tileMapPosY: Float = position.y + position.offsetY

                    // Start and end indexes of viewport area
                    val xStart: Int = tileMapPosX.toInt() / gridWidth - 1  // x in positive direction;  -1 = start one tile before
                    val xTiles = AppConfig.VIEW_PORT_WIDTH / gridWidth + 3
                    val xEnd: Int = xStart + xTiles

                    val yStart: Int = tileMapPosY.toInt() / gridHeight - 1  // y in negative direction;  -1 = start one tile before
                    val yTiles = AppConfig.VIEW_PORT_HEIGHT / gridHeight + 3
                    val yEnd: Int = yStart + yTiles

                    ctx.useBatcher { batch ->
                        for (l in 0 until tileMap.maxLevel) {  // Render all stacked tiles in the tile map
                            for (tx in xStart until xEnd) {
                                for (ty in yStart until yEnd) {
                                    val tile = tileMap[tx, ty, l]
                                    val info = tileSet.getInfo(tile.tile)
                                    if (info != null) {
                                        val px = (tx * gridWidth) + (tile.offsetX * offsetScale) - tileMapPosX
                                        val py = (ty * gridHeight) + (tile.offsetY * offsetScale) - tileMapPosY

                                        batch.drawQuad(
                                            tex = ctx.getTex(info.slice),
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
                    }
                }
            }
        }
    }
}

/**
 * Draws a textured [tex] quad at [x], [y] with size [width]x[height] and flipped in X direction.
 *
 * It uses [m] transform matrix, an optional [filtering] and [colorMul], [blendMode] and [program] as state for drawing it.
 *
 * Note: To draw solid quads, you can use [Bitmaps.white] + [AgBitmapTextureManager] as texture and the [colorMul] as quad color.
 */
fun BatchBuilder2D.drawQuadFlippedX(
    tex: TextureCoords,
    x: Float,
    y: Float,
    width: Float = tex.width.toFloat(),
    height: Float = tex.height.toFloat(),
    m: Matrix = Matrix.IDENTITY,
    filtering: Boolean = true,
    colorMul: RGBA = Colors.WHITE,
    blendMode: BlendMode = BlendMode.NORMAL,
    program: Program? = null
) {
    setStateFast(tex.base, filtering, blendMode, program, icount = 6, vcount = 4)
    drawQuadFlippedXFast(x, y, width, height, m, tex, colorMul)
}

fun BatchBuilder2D.drawQuadFlippedXFast(
    x: Float, y: Float, width: Float, height: Float,
    m: Matrix,
    tex: BmpCoords,
    colorMul: RGBA,
) {
    val x0 = (x + width)
    val x1 = x
    val y0 = y
    val y1 = (y + height)
    drawQuadFast(
        m.transformX(x0, y0), m.transformY(x0, y0),
        m.transformX(x1, y0), m.transformY(x1, y0),
        m.transformX(x1, y1), m.transformY(x1, y1),
        m.transformX(x0, y1), m.transformY(x0, y1),
        tex, colorMul,
    )
}

