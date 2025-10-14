package korlibs.korge.fleks.assets.data

import korlibs.image.tiles.Tile
import korlibs.image.tiles.TileMapData
import korlibs.image.tiles.TileSet
import korlibs.korge.fleks.assets.data.ldtk.*
import korlibs.memory.hasBitSet
import kotlin.math.max


class LayerTileMaps(
    private var name: String = "noName",
    ldtkWorld: LDTKWorld,
    ldtkLevel: Level
) {
    private val layerTileMaps: MutableMap<String, TileMapData> = mutableMapOf()

    /**
     * Width of the level in pixels.
     */
    var levelWidth: Int = 0
        private set

    /**
     * Height of the level in pixels.
     */
    var levelHeight: Int = 0
        private set

    fun getTileMapLayer(layer: String): TileMapData =
        if (layerTileMaps.contains(layer)) {
            layerTileMaps[layer]!!
        } else error("AssetStore: Layer '$layer' for Tile map '$name' not found!")

    init {
        // Save TileMapData for each layer from the LDtk level
        ldtkLevel.layerInstances?.forEach { ldtkLayer ->
            // Check if layer has tile set -> store tile map data for layer
            val tilesetExt = ldtkWorld.tilesetDefsById[ldtkLayer.tilesetDefUid]
            if (tilesetExt != null) {
                layerTileMaps[ldtkLayer.identifier] = createTileMapData(ldtkLayer, tilesetExt)
                levelWidth = ldtkLayer.cWid * tilesetExt.def.tileGridSize
                levelHeight = ldtkLayer.cHei * tilesetExt.def.tileGridSize
            }
        }
    }

    fun reloadAsset(ldtkWorld: LDTKWorld, ldtkLevel: Level) {
        ldtkLevel.layerInstances?.forEach { ldtkLayer ->
            // Check if layer has tile set -> store tile map data for layer
            val tilesetExt = ldtkWorld.tilesetDefsById[ldtkLayer.tilesetDefUid]
            if (tilesetExt != null) {
                layerTileMaps[ldtkLayer.identifier] = createTileMapData(ldtkLayer, tilesetExt)
                println("\nTriggering asset change for LDtk level : ${ldtkLevel.identifier}_${ldtkLayer.identifier}")
            }
        }
    }

    /**
     * Get the level data for a specific chunk in the grid vania array.
     * The chunk is identified by its X and Y position in the grid vania array.
     */
    private fun createTileMapData(ldtkLayer: LayerInstance, tilesetExt: ExtTileset) : TileMapData {
        val tileMapData = TileMapData(
            width = ldtkLayer.cWid,
            height = ldtkLayer.cHei,
            tileSet = tilesetExt.tileset
        )
        val gridSize = tilesetExt.def.tileGridSize
        val tilesetWidth = tilesetExt.def.pxWid

        for (tile in ldtkLayer.autoLayerTiles + ldtkLayer.gridTiles) {
            val (px, py) = tile.px
            val x = px / gridSize
            val y = py / gridSize
            val dx = px % gridSize
            val dy = py % gridSize
            val tileId = tile.t  // Tile id in the tileset which identifies the tile graphic
            val flipX = tile.f.hasBitSet(0)
            val flipY = tile.f.hasBitSet(1)

            // Get stack level depending on if the tile overlaps its neighbour cells i.e. the tile has an offset (dx, dy)
            when {
                (dx == 0 && dy == 0) -> {
                    val stackLevel = tileMapData.data.getStackLevel(x, y)
                    tileMapData.data.set(x, y, stackLevel, value = Tile(tile = tileId, offsetX = dx, offsetY = dy, flipX = flipX, flipY = flipY, rotate = false).raw)
                }

                (dx == 0 && dy != 0) -> {
                    val stackLevel = max(tileMapData.data.getStackLevel(x, y), tileMapData.data.getStackLevel(x, y + 1))
                    tileMapData.data.set(x, y, stackLevel, value = Tile(tile = tileId, offsetX = dx, offsetY = dy, flipX = flipX, flipY = flipY, rotate = false).raw)
                    tileMapData.data.set(x, y + 1, stackLevel, value = Tile.ZERO.raw)
                }

                (dx != 0 && dy == 0) -> {
                    val stackLevel = max(tileMapData.data.getStackLevel(x, y), tileMapData.data.getStackLevel(x + 1, y))
                    tileMapData.data.set(x, y, stackLevel, value = Tile(tile = tileId, offsetX = dx, offsetY = dy, flipX = flipX, flipY = flipY, rotate = false).raw)
                    tileMapData.data.set(x + 1, y, stackLevel, value = Tile.ZERO.raw)
                }

                else -> {
                    val stackLevel = korlibs.math.max(
                        tileMapData.data.getStackLevel(x, y),
                        tileMapData.data.getStackLevel(x, y + 1),
                        tileMapData.data.getStackLevel(x + 1, y),
                        tileMapData.data.getStackLevel(x + 1, y + 1)
                    )
                    tileMapData.data.set(x, y, stackLevel, value = Tile(tile = tileId, offsetX = dx, offsetY = dy, flipX = flipX, flipY = flipY, rotate = false).raw)
                    tileMapData.data.set(x, y + 1, stackLevel, value = Tile.ZERO.raw)
                    tileMapData.data.set(x + 1, y, stackLevel, value = Tile.ZERO.raw)
                    tileMapData.data.set(x + 1, y + 1, stackLevel, value = Tile.ZERO.raw)
                }
            }
        }
        return tileMapData
    }
}