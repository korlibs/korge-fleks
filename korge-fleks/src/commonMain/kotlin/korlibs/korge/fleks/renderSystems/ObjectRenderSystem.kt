package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.collection.*
import korlibs.datastructure.iterators.*
import korlibs.image.bitmap.*
import korlibs.image.font.*
import korlibs.image.text.*
import korlibs.korge.annotations.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.Layer.Companion.LayerComponent
import korlibs.korge.fleks.components.LayeredSprite.Companion.LayeredSpriteComponent
import korlibs.korge.fleks.components.NinePatch.Companion.NinePatchComponent
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Position.Companion.staticPositionComponent
import korlibs.korge.fleks.components.Rgba.Companion.RgbaComponent
import korlibs.korge.fleks.components.Sprite.Companion.SpriteComponent
import korlibs.korge.fleks.components.SpriteLayers.Companion.SpriteLayersComponent
import korlibs.korge.fleks.components.TextField.Companion.TextFieldComponent
import korlibs.korge.fleks.components.TileMap.Companion.TileMapComponent
import korlibs.korge.fleks.components.getImageFrame
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.getMainCamera
import korlibs.korge.fleks.utils.AppConfig
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
    private val comparator: EntityComparator = compareEntity(world) { entA, entB -> entA[LayerComponent].index.compareTo(entB[LayerComponent].index) }
) : View() {
    private val family: Family = world.family { all(layerTag, PositionComponent, LayerComponent, RgbaComponent)
        .any(PositionComponent, LayerComponent, SpriteComponent, LayeredSpriteComponent, TextFieldComponent, SpriteLayersComponent, NinePatchComponent, TileMapComponent)
    }
    private val assetStore: AssetStore = world.inject(name = "AssetStore")
    private val position: Position = staticPositionComponent {}

    @OptIn(KorgeExperimental::class)
    override fun renderInternal(ctx: RenderContext) {
        val camera: Entity = world.getMainCamera()

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
            if (entity has SpriteComponent) {
                val spriteComponent = entity[SpriteComponent]
                val imageFrame = assetStore.getImageFrame(spriteComponent.name, spriteComponent.animation, spriteComponent.frameIndex)

                if (entity has SpriteLayersComponent) {
                    val layerMap = entity[SpriteLayersComponent].layerMap
                    ctx.useBatcher { batch ->
                        // Iterate over all layers of each sprite for the frame number
                        imageFrame.layerData.fastForEach { layerData ->
                            val layerName = layerData.layer.name ?: ""

                            layerMap[layerName]?.let { layerProps ->

                                batch.drawQuad(
                                    tex = ctx.getTex(layerData.slice),
                                    x = position.x + layerData.targetX - spriteComponent.anchorX + layerProps.offsetX,
                                    y = position.y + layerData.targetY - spriteComponent.anchorY + layerProps.offsetY,
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
                            val px = position.x + position.offsetX + layerData.targetX - spriteComponent.anchorX
                            val py = position.y + position.offsetY + layerData.targetY - spriteComponent.anchorY
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
                val layeredSpriteComponent = entity[LayeredSpriteComponent]
                val imageFrame = assetStore.getImageFrame(layeredSpriteComponent.name, layeredSpriteComponent.animation, layeredSpriteComponent.frameIndex)

                ctx.useBatcher { batch ->
                    // Iterate over all layers of each sprite for the frame number
                    layeredSpriteComponent.layerList.fastForEachWithIndex { index, layer ->
                        // Get image data for specific layer from asset store
                        val image = imageFrame.layerData[index]
                        batch.drawQuad(
                            tex = ctx.getTex(image.slice),
                            x = position.x + image.targetX - layeredSpriteComponent.anchorX + layer.position.x + layer.position.offsetX,
                            y = position.y + image.targetY - layeredSpriteComponent.anchorY + layer.position.y + layer.position.offsetY,
                            filtering = false,
                            colorMul = layer.rgba.rgba,
                            program = null // Possibility to use a custom shader - add ShaderComponent or similar
                        )
                    }
                }
            }
            // Rendering path for text (not optimized - no caching)
            else if (entity has TextFieldComponent) {
                val textFieldComponent = entity[TextFieldComponent]
                val offset = Point(position.offsetX, position.offsetY)

                renderCtx2d(ctx) { render ->
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
                    batch.drawVertices(tva, ctx.getTex(ninePatch.content.bmp), smoothing = false, blendMode)
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

    // Set size of render view to display size
    override fun getLocalBoundsInternal(): Rectangle = with (world) {
        return Rectangle(0, 0, AppConfig.VIEW_PORT_WIDTH, AppConfig.VIEW_PORT_HEIGHT)
    }

    init {
        name = layerTag.toString()
    }
}
