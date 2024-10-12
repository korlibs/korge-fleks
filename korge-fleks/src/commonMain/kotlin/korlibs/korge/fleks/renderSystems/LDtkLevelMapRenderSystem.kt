package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.collection.*
import korlibs.image.color.*
import korlibs.korge.annotations.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.render.*
import korlibs.korge.view.*
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

    @OptIn(KorgeExperimental::class)
    override fun renderInternal(ctx: RenderContext) {
        // Sort level maps by their layerIndex
        family.sort(comparator)

        // Iterate over all entities which should be rendered in this view
        family.forEach { entity ->
            val (x, y, offsetX, offsetY) = entity[PositionComponent]
            val ldtkLevelMapComponent = entity[LdtkLevelMapComponent]
            val levelLayer = ldtkLevelMapComponent.levelLayer

            val rgba = Colors.WHITE  // TODO: use here alpha from ldtk layer

            val tileMap = assetStore.getTileMapData(levelLayer)
            val tileSet = tileMap.tileSet
            val tileSetWidth = tileSet.width
            val tileSetHeight = tileSet.height
            val offsetScale = tileMap.offsetScale

            val xTiles = viewPortSize.width / tileSetWidth + 1
            val yTiles = viewPortSize.height / tileSetHeight + 1

            ctx.useBatcher { batch ->
                for (l in 0 until tileMap.maxLevel) {
//                    val l = 2
                    for (tx in 0 until xTiles) {
                        for (ty in 0 until yTiles) {
                            val tile = tileMap[tx, ty, l]
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
    }
}
