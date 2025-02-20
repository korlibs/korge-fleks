package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.collection.*
import korlibs.event.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.input.*
import korlibs.korge.render.*
import korlibs.korge.view.*
import korlibs.math.*
import korlibs.math.geom.*


inline fun Container.levelMapRenderSystem(world: World, layerTag: RenderLayerTag, callback: @ViewDslMarker LevelMapRenderSystem.() -> Unit = {}) =
    LevelMapRenderSystem(world, layerTag).addTo(this, callback)

/**
 * Here we do not render the actual level map yet.
 * Instead, we add the view object for the level map to the container.
 */
class LevelMapRenderSystem(
    private val world: World,
    layerTag: RenderLayerTag,
    private val comparator: EntityComparator = compareEntity(world) { entA, entB -> entA[LayerComponent].layerIndex.compareTo(entB[LayerComponent].layerIndex) }
) : View() {
    private val family: Family = world.family { all(layerTag, LayerComponent, LevelMapComponent) }

    private val assetStore: AssetStore = world.inject(name = "AssetStore")

    // Debugging layer rendering
    private var renderLayer = 0

    override fun renderInternal(ctx: RenderContext) {
        val camera: Entity = world.getMainCamera()
        val cameraPosition = with(world) { camera[PositionComponent] }
        val cameraViewPort = with(world) { camera[SizeIntComponent] }
        val cameraViewPortHalf = with(world) { camera[SizeComponent] }

        // Sort level maps by their layerIndex
        family.sort(comparator)

        // Iterate over all entities which should be rendered in this view
        family.forEach { entity ->
            val (rgba) = entity[RgbaComponent]
            val (levelName, layerNames) = entity[LevelMapComponent]
            val worldData = assetStore.getWorldData(levelName)

            // Calculate viewport position in world coordinates from Camera position (x,y) + offset
            val viewPortPosX: Float = cameraPosition.x + cameraPosition.offsetX - cameraViewPortHalf.width
            val viewPortPosY: Float = cameraPosition.y + cameraPosition.offsetY - cameraViewPortHalf.height

            layerNames.forEach { layerName ->
//                val tileMap = assetStore.getTileMapData(levelName, layerName)
                val levelMap = worldData.getLevelMap(layerName)
//                val tileSet = tileMap.tileSet
//                val tileWidth = tileSet.width
//                val tileHeight = tileSet.height
//                val offsetScale = tileMap.offsetScale

                // TODO check if we can use different tile sizes for different layers of a level
                //      if not move below x - y calculation out of forEach loop

                // Start and end indexes of viewport area (in grid coordinates)
                val xStart: Int = viewPortPosX.toInt() / worldData.tileSize - 1  // x in positive direction;  -1 = start one tile before
                val xTiles = (cameraViewPort.width / worldData.tileSize) + 3
//                val xEnd: Int = xStart + xTiles

                val yStart: Int = viewPortPosY.toInt() / worldData.tileSize - 1  // y in negative direction;  -1 = start one tile before
                val yTiles = cameraViewPort.height / worldData.tileSize + 3
//                val yEnd: Int = yStart + yTiles

//                val maxStackLevel = levelMap.getMaxStackLevel(xStart, yStart, xTiles, yTiles)

                ctx.useBatcher { batch ->
                    levelMap.forEachTile(xStart, yStart, xTiles, yTiles) { slice, px, py ->
                        batch.drawQuad(
                            tex = ctx.getTex(slice),
                            x = px - viewPortPosX,
                            y = py - viewPortPosY,
                            filtering = false,
                            colorMul = rgba,
                            program = null // Possibility to use a custom shader - add ShaderComponent or similar
                        )
                    }
/*
                    // Render tiles with world coordinates - tiles could come from different levels at level edges
                    // TODO - levels might have different max stack levels!!!
                    for (l in 0 until maxStackLevel) {  //tileMap.maxLevel) {
                        // level is the "layer" from stacked tiles in ldtk
                        val stackLevel =
                            if (renderLayer == 0) l
                            else (renderLayer - 1).clamp(0, l)

                        for (tx in xStart until xEnd) {
                            for (ty in yStart until yEnd) {
//                                val tile = tileMap[tx, ty, stackLevel]

                                // TODO Get tile from world data (can be from different levels)
                                val tile = levelMap[tx, ty, stackLevel]
//                                val tileInfo = tileSet.getInfo(tile.tile)
                                val tileInfo = levelMap.getTileInfo(tx, ty, tile.tile)

                                if (tileInfo != null) {
                                    val px = (tx * worldData.tileSize) + tile.offsetX - viewPortPosX
                                    val py =  (ty * worldData.tileSize) + tile.offsetY - viewPortPosY

                                    batch.drawQuad(
                                        tex = ctx.getTex(tileInfo.slice),
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
*/
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
