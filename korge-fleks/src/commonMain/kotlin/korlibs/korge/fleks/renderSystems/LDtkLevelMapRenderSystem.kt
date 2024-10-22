package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.collection.*
import korlibs.event.*
import korlibs.image.color.*
import korlibs.korge.annotations.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.input.*
import korlibs.korge.render.*
import korlibs.korge.view.*
import korlibs.math.*
import korlibs.math.geom.*


inline fun Container.ldtkLevelMapRenderSystem(viewPortSize: SizeInt, world: World, layerTag: RenderLayerTag, callback: @ViewDslMarker LDtkLevelMapRenderSystem.() -> Unit = {}) =
    LDtkLevelMapRenderSystem(viewPortSize, world, layerTag).addTo(this, callback)

/**
 * Here we do not render the actual level map yet.
 * Instead, we add the view object for the level map to the container.
 */
class LDtkLevelMapRenderSystem(
    private val viewPortSize: SizeInt,
    world: World,
    layerTag: RenderLayerTag,
    private val comparator: EntityComparator = compareEntity(world) { entA, entB -> entA[LayerComponent].layerIndex.compareTo(entB[LayerComponent].layerIndex) }
) : View() {
    private val family: Family = world.family { all(layerTag, LayerComponent, PositionComponent, LdtkLevelMapComponent) }
    private val assetStore: AssetStore = world.inject(name = "AssetStore")

    // Debugging layer rendering
    private var renderLayer = 0

    override fun renderInternal(ctx: RenderContext) {
        // Sort level maps by their layerIndex
        family.sort(comparator)

        // Iterate over all entities which should be rendered in this view
        family.forEach { entity ->
            val (x, y, offsetX, offsetY) = entity[PositionComponent]
            val (levelName, layerNames) = entity[LdtkLevelMapComponent]

            val rgba = Colors.WHITE  // TODO: use here alpha from ldtk layer

            val tileMap = assetStore.getTileMapData(levelName, layerNames.first())  // TODO: Render all layers, not only first one
            val tileSet = tileMap.tileSet
            val tileSetWidth = tileSet.width
            val tileSetHeight = tileSet.height
            val offsetScale = tileMap.offsetScale

            val xTiles = viewPortSize.width / tileSetWidth + 1
            val yTiles = viewPortSize.height / tileSetHeight + 1

            ctx.useBatcher { batch ->
                for (l in 0 until tileMap.maxLevel) {
                    val level =
                        if (renderLayer == 0) l
                        else (renderLayer - 1).clamp(0, l)

                    for (tx in 0 until xTiles) {
                        for (ty in 0 until yTiles) {
                            val tile = tileMap[tx, ty, level]
                            val info = tileSet.getInfo(tile.tile)
                            if (info != null) {
                                val px = x + (tx * tileSetWidth) + (tile.offsetX * offsetScale)
                                val py = y + (ty * tileSetHeight) + (tile.offsetY * offsetScale)

                                batch.drawQuad(
                                    tex = ctx.getTex(info.slice),
                                    x = px,
                                    y = py,
                                    filtering = false,
                                    colorMul = rgba,
                                    // TODO: Add possibility to use a custom shader - add ShaderComponent or similar
                                    program = null
                                )

                            }
                        }
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

        // For debugging layer rendering
        keys {
            justDown(Key.N0) { renderLayer = 0 }
            justDown(Key.N1) { renderLayer = 1 }
            justDown(Key.N2) { renderLayer = 2 }
            justDown(Key.N3) { renderLayer = 3 }
            justDown(Key.N4) { renderLayer = 4 }
            justDown(Key.N5) { renderLayer = 5 }
            justDown(Key.N6) { renderLayer = 6 }
            justDown(Key.N7) { renderLayer = 7 }
            justDown(Key.N8) { renderLayer = 8 }
            justDown(Key.N9) { renderLayer = 9 }
        }

    }
}
