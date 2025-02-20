package korlibs.korge.fleks.assets

import korlibs.image.bitmap.*
import korlibs.image.tiles.*

/**
 *
 * @param tileSize - Size of a grid cell in pixels (e.g. 16 for 16x16 tile size)
 * @param width - Width of whole level in pixels
 * @param height - Height of whole level in pixels
 */
data class WorldData(
    var tileSize: Int = 16,

    // Size of the whole world (in pixels)
    var width: Float = 0f,
    var height: Float = 0f,
    var gridWidth: Int = 0,
    var gridHeight: Int = 0,

    var layerlevelMaps: MutableMap<String, LevelMap> = mutableMapOf(),
) {

    fun getLevelMap(layerName: String) : LevelMap {
        if (!layerlevelMaps.contains(layerName)) println("WARNING: Level map for layer '$layerName' does not exist!")
        return layerlevelMaps[layerName] ?: LevelMap(1, 1)
    }
}

data class LevelMap(
    // Size of a level inside the grid vania array (all levels have the same size)
    val gridWidth: Int,
    val gridHeight: Int,
    val entities: MutableList<String> = mutableListOf(),  // TODO change to list
    var levelGridVania: List<List<LevelData>> = listOf(),
) {
    /**
     * Iterate over all tiles within the given view port area and call the renderCall function for each tile.
     *
     * @param x - horizontal position of top-left corner of view port in tiles
     * @param y - vertical position of top-left corner of view port in tiles
     * @param width - width of view port in tiles
     * @param height - height of view port in tiles
     */
    fun forEachTile(x: Int, y: Int, width: Int, height: Int, renderCall: (BmpSlice, Float, Float) -> Unit) {
        // Calculate the view port corners (top-left, top-right, bottom-left and bottom-right positions) in gridvania indexes
        // and check if the corners are in different level maps (tileMapData)
        val gridX = x / gridWidth
        val gridY = y / gridHeight
        val gridX2 = (x + width) / gridWidth
        val gridY2 = (y + height) / gridHeight

        val xStart = x % gridWidth
        val yStart = y % gridHeight

        // Check if the view port area overlaps multiple levels
        if (gridX == gridX2) {
            // We have only one level in horizontal direction
            if (gridY == gridY2) {
                // We have only one level in vertical direction
                processTiles(gridX, gridY, xStart, yStart, xStart + width, yStart + height, renderCall)
            } else {
                // We have vertically two levels
                processTiles(gridX, gridY, xStart, yStart, xStart + width, gridHeight, renderCall)
                processTiles(gridX, gridY2, xStart, 0, xStart + width, (yStart + height) % gridHeight, renderCall)
            }
        } else {
            // We have horizontal two levels
            if (gridY == gridY2) {
                // We have only one level in vertical direction
                processTiles(gridX, gridY, xStart, yStart, gridWidth, yStart + height, renderCall)
                processTiles(gridX2, gridY, 0, yStart, (xStart + width) % gridWidth, yStart + height, renderCall)
            } else {
                // We have vertical two levels
                processTiles(gridX, gridY, xStart, yStart, gridWidth, gridHeight, renderCall)
                processTiles(gridX2, gridY, 0, yStart, (xStart + width) % gridWidth, gridHeight, renderCall)
                processTiles(gridX, gridY2, xStart, 0, gridWidth, (yStart + height) % gridHeight, renderCall)
                processTiles(gridX2, gridY2, 0, 0, (xStart + width) % gridWidth, (yStart + height) % gridHeight, renderCall)
            }
        }
    }

    private fun processTiles(gridX: Int, gridY: Int, xStart: Int, yStart: Int, xEnd: Int, yEnd: Int, renderCall: (BmpSlice, Float, Float) -> Unit) {
        levelGridVania[gridX][gridY].tileMapData?.let { tileMap ->
            val tileSet = tileMap.tileSet
            val tileWidth = tileSet.width
            val tileHeight = tileSet.height

            for (l in 0 until tileMap.maxLevel) {
                for (tx in xStart until xEnd) {
                    for (ty in yStart until yEnd) {
                        val tile = tileMap[tx, ty, l]
                        val tileInfo = tileSet.getInfo(tile.tile)
                        if (tileInfo != null) {
                            val px = (tx * tileWidth) + tile.offsetX + (gridX * gridWidth * tileWidth)
                            val py = (ty * tileHeight) + tile.offsetY + (gridY * gridHeight * tileWidth)
                            renderCall(tileInfo.slice, px.toFloat(), py.toFloat())
                        }
                    }
                }
            }
        }

    }
}

// Data class for storing level data like grizSize, width, height, entities, tileMapData
data class LevelData(
    var type: AssetType = AssetType.COMMON,  // TODO: Remove
    val gridSize: Int = 16,  // TODO: Remove

    val entities: MutableList<String> = mutableListOf(),
    var tileMapData: TileMapData? = null
)
