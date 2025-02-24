package korlibs.korge.fleks.assets

import korlibs.image.bitmap.*
import korlibs.image.tiles.*

/**
 * Data class for storing level maps and entities for a game world.
 *
 * @param width - Width of whole world in pixels
 * @param height - Height of whole world in pixels
 */
data class WorldData(
    // Size of all gridvania levels in the world (in pixels / world coordinates)
    var width: Float = 0f,
    var height: Float = 0f,
    // Size of a level inside the grid vania array in tiles (all levels have the same size)
    var levelWidth: Int = 0,
    var levelHeight: Int = 0,
    // Size of a grid cell in pixels (e.g. 16 for 16x16 tile size)
    var gridSize: Int = 1,
    var layerlevelMaps: MutableMap<String, LevelMap> = mutableMapOf(),
) {

    fun getLevelMap(layerName: String): LevelMap {
        if (!layerlevelMaps.contains(layerName)) println("WARNING: Level map for layer '$layerName' does not exist!")
        return layerlevelMaps[layerName] ?: LevelMap()
    }

    data class LevelMap(
        val levelGridVania: List<List<LevelData>> = listOf()
    ) {
        /**
         * Iterate over all tiles within the given view port area and call the renderCall function for each tile.
         *
         * @param x - horizontal position of top-left corner of view port in tiles
         * @param y - vertical position of top-left corner of view port in tiles
         * @param width - width of view port in tiles
         * @param height - height of view port in tiles
         * @param levelWidth - width of a level in tiles
         * @param levelHeight - height of a level in tiles
         */
        fun forEachTile(x: Int, y: Int, width: Int, height: Int, levelWidth: Int, levelHeight: Int, renderCall: (BmpSlice, Float, Float) -> Unit) {
            // Calculate the view port corners (top-left, top-right, bottom-left and bottom-right positions) in gridvania indexes
            // and check if the corners are in different level maps (tileMapData)
            val gridX = x / levelWidth
            val gridY = y / levelHeight
            val gridX2 = (x + width) / levelWidth
            val gridY2 = (y + height) / levelHeight

            val xStart = x % levelWidth
            val yStart = y % levelHeight

            // Check if the view port area overlaps multiple levels
            if (gridX == gridX2) {
                // We have only one level in horizontal direction
                if (gridY == gridY2) {
                    // We have only one level in vertical direction
                    processTiles(gridX, gridY, xStart, yStart, xStart + width, yStart + height, levelWidth, levelHeight, renderCall)
                } else {
                    // We have vertically two levels
                    processTiles(gridX, gridY, xStart, yStart, xStart + width, levelHeight, levelWidth, levelHeight, renderCall)
                    processTiles(gridX, gridY2, xStart, 0, xStart + width, (yStart + height) % levelHeight, levelWidth, levelHeight, renderCall)
                }
            } else {
                // We have horizontal two levels
                if (gridY == gridY2) {
                    // We have only one level in vertical direction
                    processTiles(gridX, gridY, xStart, yStart, levelWidth, yStart + height, levelWidth, levelHeight, renderCall)
                    processTiles(gridX2, gridY, 0, yStart, (xStart + width) % levelWidth, yStart + height, levelWidth, levelHeight, renderCall)
                } else {
                    // We have vertical two levels
                    processTiles(gridX, gridY, xStart, yStart, levelWidth, levelHeight, levelWidth, levelHeight, renderCall)
                    processTiles(gridX2, gridY, 0, yStart, (xStart + width) % levelWidth, levelHeight, levelWidth, levelHeight, renderCall)
                    processTiles(gridX, gridY2, xStart, 0, levelWidth, (yStart + height) % levelHeight, levelWidth, levelHeight, renderCall)
                    processTiles(gridX2, gridY2, 0, 0, (xStart + width) % levelWidth, (yStart + height) % levelHeight, levelWidth, levelHeight, renderCall)
                }
            }
        }

        private fun processTiles(gridX: Int, gridY: Int, xStart: Int, yStart: Int, xEnd: Int, yEnd: Int, levelWidth: Int, levelHeight: Int, renderCall: (BmpSlice, Float, Float) -> Unit) {
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
                                val px = (tx * tileWidth) + tile.offsetX + (gridX * levelWidth * tileWidth)
                                val py = (ty * tileHeight) + tile.offsetY + (gridY * levelHeight * tileHeight)
                                renderCall(tileInfo.slice, px.toFloat(), py.toFloat())
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Data class for storing level data like entities and tileMapData
     */
    data class LevelData(
        var entities: List<String> = listOf(),
        var tileMapData: TileMapData? = null
    )
}
