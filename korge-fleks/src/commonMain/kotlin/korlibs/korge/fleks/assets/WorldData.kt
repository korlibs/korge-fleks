package korlibs.korge.fleks.assets

import korlibs.datastructure.Array2
import korlibs.datastructure.IntArray2
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
    val width: Float = 0f,
    val height: Float = 0f,
    // Size of a level inside the grid vania array in tiles (all levels have the same size)
    val levelGridWidth: Int = 0,
    val levelGridHeight: Int = 0,
    // Size of a tile cell in pixels (e.g. 16 for 16x16 tile size)
    val tileSize: Int = 1,
    // Level maps
    val levelGridVania: Array2<Chunk>,
    val gridVaniaWidth: Int = 0,
    val gridVaniaHeight: Int = 0
) {

    data class Chunk(
        var entityConfigNames: List<String>? = null,
        var tileMapData: Map<String, TileMapData> = mapOf(),
        var collisionMap: IntArray2? = null
    )

    /**
     * Iterate over all entities within the chunk, where the camera is currently located, and all
     * adjacent chunks. Call the callback function for each entity config.
     */
    fun forEachEntityInChunk(viewPortMiddlePosX: Int, viewPortMiddlePosY: Int, levelChunkConfig: ChunkArray2, callback: (String) -> Unit) {
        // Calculate the grid position of the view port middle position
        val gridX: Int = viewPortMiddlePosX / levelGridWidth
        val gridY: Int = viewPortMiddlePosY / levelGridHeight

        for( x in gridX - 1..gridX + 1) {
            for (y in gridY - 1..gridY + 1) {
                // Check if the chunk is already spawned
                if (levelGridVania.inside(x, y) && !levelChunkConfig[x, y].entitiesSpawned) {
                    levelChunkConfig[x, y].entitiesSpawned = true
                    levelGridVania[x, y].entityConfigNames?.forEach { entityConfigName ->
                        callback(entityConfigName)
                    }
                }
            }
        }
    }

    /**
     * Iterate over all tiles within the given view port area and call the renderCall function for each tile.
     *
     * @param x - horizontal position of top-left corner of view port in tiles
     * @param y - vertical position of top-left corner of view port in tiles
     * @param width - width of view port in tiles
     * @param height - height of view port in tiles
     */
    fun forEachTile(layer: String, x: Int, y: Int, width: Int, height: Int, renderCall: (BmpSlice, Float, Float) -> Unit) {
        // Calculate the view port corners (top-left, top-right, bottom-left and bottom-right positions) in gridvania indexes
        // and check if the corners are in different level maps (tileMapData)
        val gridX = x / levelGridWidth
        val gridY = y / levelGridHeight
        val gridX2 = (x + width) / levelGridWidth
        val gridY2 = (y + height) / levelGridHeight
        val xStart = x % levelGridWidth
        val yStart = y % levelGridHeight
        // Check if the view port area overlaps multiple levels
        if (gridX == gridX2) {
            // We have only one level in horizontal direction
            if (gridY == gridY2) {
                // We have only one level in vertical direction
                processTiles(layer, gridX, gridY, xStart, yStart, xStart + width, yStart + height, levelGridWidth, levelGridHeight, renderCall)
            } else {
                // We have vertically two levels
                processTiles(layer, gridX, gridY, xStart, yStart, xStart + width, levelGridHeight, levelGridWidth, levelGridHeight, renderCall)
                processTiles(layer, gridX, gridY2, xStart, 0, xStart + width, (yStart + height) % levelGridHeight, levelGridWidth, levelGridHeight, renderCall)
            }
        } else {
            // We have horizontal two levels
            if (gridY == gridY2) {
                // We have only one level in vertical direction
                processTiles(layer, gridX, gridY, xStart, yStart, levelGridWidth, yStart + height, levelGridWidth, levelGridHeight, renderCall)
                processTiles(layer, gridX2, gridY, 0, yStart, (xStart + width) % levelGridWidth, yStart + height, levelGridWidth, levelGridHeight, renderCall)
            } else {
                // We have vertical two levels
                processTiles(layer, gridX, gridY, xStart, yStart, levelGridWidth, levelGridHeight, levelGridWidth, levelGridHeight, renderCall)
                processTiles(layer, gridX2, gridY, 0, yStart, (xStart + width) % levelGridWidth, levelGridHeight, levelGridWidth, levelGridHeight, renderCall)
                processTiles(layer, gridX, gridY2, xStart, 0, levelGridWidth, (yStart + height) % levelGridHeight, levelGridWidth, levelGridHeight, renderCall)
                processTiles(layer, gridX2, gridY2, 0, 0, (xStart + width) % levelGridWidth, (yStart + height) % levelGridHeight, levelGridWidth, levelGridHeight, renderCall)
            }
        }
    }

    fun forEachCollisionTile(x: Int, y: Int, width: Int, height: Int, renderCall: (Int, Float, Float) -> Unit) {
        // Calculate the view port corners (top-left, top-right, bottom-left and bottom-right positions) in gridvania indexes
        // and check if the corners are in different level maps (tileMapData)
        val gridX = x / levelGridWidth
        val gridY = y / levelGridHeight
        val gridX2 = (x + width) / levelGridWidth
        val gridY2 = (y + height) / levelGridHeight
        val xStart = x % levelGridWidth
        val yStart = y % levelGridHeight
        // Check if the view port area overlaps multiple levels
        if (gridX == gridX2) {
            // We have only one level in horizontal direction
            if (gridY == gridY2) {
                // We have only one level in vertical direction
                processCollisionTiles(gridX, gridY, xStart, yStart, xStart + width, yStart + height, levelGridWidth, levelGridHeight, renderCall)
            } else {
                // We have vertically two levels
                processCollisionTiles(gridX, gridY, xStart, yStart, xStart + width, levelGridHeight, levelGridWidth, levelGridHeight, renderCall)
                processCollisionTiles(gridX, gridY2, xStart, 0, xStart + width, (yStart + height) % levelGridHeight, levelGridWidth, levelGridHeight, renderCall)
            }
        } else {
            // We have horizontal two levels
            if (gridY == gridY2) {
                // We have only one level in vertical direction
                processCollisionTiles(gridX, gridY, xStart, yStart, levelGridWidth, yStart + height, levelGridWidth, levelGridHeight, renderCall)
                processCollisionTiles(gridX2, gridY, 0, yStart, (xStart + width) % levelGridWidth, yStart + height, levelGridWidth, levelGridHeight, renderCall)
            } else {
                // We have vertical two levels
                processCollisionTiles(gridX, gridY, xStart, yStart, levelGridWidth, levelGridHeight, levelGridWidth, levelGridHeight, renderCall)
                processCollisionTiles(gridX2, gridY, 0, yStart, (xStart + width) % levelGridWidth, levelGridHeight, levelGridWidth, levelGridHeight, renderCall)
                processCollisionTiles(gridX, gridY2, xStart, 0, levelGridWidth, (yStart + height) % levelGridHeight, levelGridWidth, levelGridHeight, renderCall)
                processCollisionTiles(gridX2, gridY2, 0, 0, (xStart + width) % levelGridWidth, (yStart + height) % levelGridHeight, levelGridWidth, levelGridHeight, renderCall)
            }
        }
    }

    private fun processTiles(layer: String, gridX: Int, gridY: Int, xStart: Int, yStart: Int, xEnd: Int, yEnd: Int, levelWidth: Int, levelHeight: Int, renderCall: (BmpSlice, Float, Float) -> Unit) {
        levelGridVania[gridX, gridY].tileMapData[layer]?.let { tileMap ->
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

    private fun processCollisionTiles(gridX: Int, gridY: Int, xStart: Int, yStart: Int, xEnd: Int, yEnd: Int, levelWidth: Int, levelHeight: Int, renderCall: (Int, Float, Float) -> Unit) {
        levelGridVania[gridX, gridY].collisionMap?.let { collisionMap ->
            for (tx in xStart until xEnd) {
                for (ty in yStart until yEnd) {
                    val tile = collisionMap[tx, ty]
                    if (tile != 0) {
                        val px = tx * tileSize + (gridX * levelWidth * tileSize)
                        val py = ty * tileSize + (gridY * levelHeight * tileSize)
                        renderCall(tile, px.toFloat(), py.toFloat())
                    }
                }
            }
        }
    }
}
