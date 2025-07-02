package korlibs.korge.fleks.prefab.data

import korlibs.datastructure.Array2
import korlibs.datastructure.IntArray2
import korlibs.image.bitmap.BmpSlice
import korlibs.image.tiles.TileMapData

/**
 * Data class for storing level maps and entities for a game world.
 *
 * @param width - Width of whole world in pixels
 * @param height - Height of whole world in pixels
 */
data class LevelData(
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
    val gridVaniaHeight: Int = 0,
    // Tile maps for special layers (intro, cut-scenes, etc.) - tile maps are independent of the level chunks
    val specialTileMaps: MutableMap<String, TileMapData> = mutableMapOf()
) {
    private val levelMidPointX: Int = levelGridWidth / 2
    private val levelMidPointY: Int = levelGridHeight / 2

    /**
     * Represents a chunk of the level, containing entity configurations, tile map data and collision map.
     *
     * @property entityConfigNames - List of entity configuration names in this chunk.
     * @property tileMapData - Map of tile map data for different layers in this chunk.
     * @property collisionMap - Collision map for this chunk, if any.
     */
    data class Chunk(
        var entityConfigNames: List<String>? = null,
        /*
         * Map of tile map data for different layers in this chunk.
         * The key is the layer name, and the value is the TileMapData for that layer.
         */
        var tileMapData: Map<String, TileMapData> = mapOf(),
        var collisionMap: IntArray2? = null
    )


    init {
        // Sanity check for levelGridVania dimensions
        levelGridVania.forEach { chunk ->
            require(
                chunk.collisionMap == null ||
                chunk.collisionMap?.width == levelGridWidth && chunk.collisionMap?.height == levelGridHeight
            ) {
                "Collision map dimensions must match level grid dimensions!"
            }
        }
    }

    /**
     * Check collision at cell position
     */
    fun hasCollision(cx: Int, cy: Int): Boolean {
        // Get the chunk coordinates in the levelGridvainia array
        val gridCx = cx / levelGridWidth
        val gridCy = cy / levelGridHeight
        return if (levelGridVania.inside(gridCx, gridCy)) {
            // Get the local coordinates within the chunk
            val localCx = cx % levelGridWidth
            val localCy = cy % levelGridHeight
            // Check if the collision map exists and if the tile is not empty
            levelGridVania[gridCx, gridCy].collisionMap?.let { collisionMap ->
                // Local coordinates are within the bounds of the collision map because we used modulo
                // with levelGridWidth and levelGridHeight
                collisionMap[localCx, localCy] != 0
            } ?: false  // No collision map exists for this chunk --> no collision
        } else true  // Outside levelGridVania bounds
    }

    /**
     * Iterate over all entities within the chunk, where the camera is currently located, and all
     * adjacent chunks. Call the callback function for each entity config.
     */
    fun forEachEntityInChunk(viewPortMiddlePosX: Int, viewPortMiddlePosY: Int, levelChunkConfig: ChunkArray2, callback: (String) -> Unit) {
        // Calculate the grid position of the view port middle position
        val gridX: Int = viewPortMiddlePosX / levelGridWidth
        val gridY: Int = viewPortMiddlePosY / levelGridHeight

        // Check in which quadrant of the grid the view port is located
        // and iterate over the adjacent chunks (2x2 grid)
        val localViewPortPosX: Int = viewPortMiddlePosX % levelGridWidth
        val localViewPortPosY: Int = viewPortMiddlePosY % levelGridHeight

        var startGridX: Int
        var startGridY: Int
        var endGridX: Int
        var endGridY: Int

        if (localViewPortPosX < levelMidPointX) {
            if (localViewPortPosY < levelMidPointY) {
                // Top-left quadrant
                startGridX = gridX - 1
                endGridX = gridX
                startGridY = gridY - 1
                endGridY = gridY
            } else {
                // Bottom-left quadrant
                startGridX = gridX - 1
                endGridX = gridX
                startGridY = gridY
                endGridY = gridY + 1
            }
        } else {
            if (localViewPortPosY < levelMidPointY) {
                // Top-right quadrant
                startGridX = gridX
                endGridX = gridX + 1
                startGridY = gridY - 1
                endGridY = gridY
            } else {
                // Bottom-right quadrant
                startGridX = gridX
                endGridX = gridX + 1
                startGridY = gridY
                endGridY = gridY + 1
            }
        }

        for( x in startGridX .. endGridX) {
            for (y in startGridY .. endGridY) {
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
     * @param cx - horizontal position of top-left corner of view port in cell coordinates (tiles)
     * @param cy - vertical position of top-left corner of view port in cell coordinates (tiles)
     * @param width - width of view port in tiles
     * @param height - height of view port in tiles
     */
    fun forEachTile(layer: String, cx: Int, cy: Int, width: Int, height: Int, renderCall: (BmpSlice, Float, Float) -> Unit) {
        // Calculate the view port corners (top-left, top-right, bottom-left and bottom-right positions) in gridvania indexes
        // and check if the corners are in different level maps (tileMapData)
        val gridX = cx / levelGridWidth
        val gridY = cy / levelGridHeight
        val gridX2 = (cx + width) / levelGridWidth
        val gridY2 = (cy + height) / levelGridHeight
        val xStart = cx % levelGridWidth
        val yStart = cy % levelGridHeight
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