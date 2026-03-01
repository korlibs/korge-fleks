package korlibs.korge.fleks.assets.data

import korlibs.datastructure.IntArray2
import korlibs.image.bitmap.BmpSlice
import kotlin.math.abs

/**
 * Data class for storing world chunks and entities for a game world.
 *
 * We store the level data config in a 2D array depending on its grid-vania position in the world
 * Then later we will spawn the entities depending on the level which the player is currently in
 */
class WorldMapData {
    // Size of whole world in pixel - init with size of one chunk, but will be set to actual world size in init function
    var worldWidth: Float = 1024f
        private set
    var worldHeight: Float = 1024f
        private set
    // Size of a world chunk inside the grid vania array in tiles (all levels have the same size)
    var levelChunkWidth: Int = 64
        private set
    var levelChunkHeight: Int = 64
        private set
    // Size of a tile cell in pixels (all tiles have the same size) - default is 16x16 pixels
    var tileSize: Int = 16
        private set

    internal val chunkMeshes: MutableMap<Int, ChunkAssetInfo> = mutableMapOf()
    internal lateinit var levelGridVania: IntArray2
    internal lateinit var collisionTileSet: CollisionTileSet

    private val levelMidPointX: Int = levelChunkWidth / 2
    private val levelMidPointY: Int = levelChunkHeight / 2

    fun init(
        worldWidth: Float,
        worldHeight: Float,
        levelChunkWidth: Int,
        levelChunkHeight: Int,
        tileSize: Int,
        collisionTiles: List<List<Int>>,
        collisionShapesBitmapSlice: BmpSlice
    ) {
        this.worldWidth = worldWidth
        this.worldHeight = worldHeight
        this.levelChunkWidth = levelChunkWidth
        this.levelChunkHeight = levelChunkHeight
        this.tileSize = tileSize

        // Set up grid-vania array
        levelGridVania = IntArray2(levelChunkWidth, levelChunkHeight) { -1 }
        // Set up collision tile set
        collisionTileSet = CollisionTileSet(collisionTiles, collisionShapesBitmapSlice, tileSize, tileSize)
    }

    /**
     * Check collision at cell position
     */
    fun hasCollision(cx: Int, cy: Int): Boolean {
        // Get the chunk coordinates in the chunk Grid-vania array
        val gridCx =  abs(cx / levelChunkWidth)
        val gridCy = abs(cy / levelChunkHeight)
        return if (levelGridVania.inside(gridCx, gridCy)) {
            // Get the local coordinates within the chunk
            val localCx = cx % levelChunkWidth
            val localCy = cy % levelChunkHeight
            // Check if the tile is not empty and if it has a collision index (bits 20-27) that is not 0
            val chunkId = levelGridVania[gridCx, gridCy]
            val tileIdx = localCx + localCy * levelChunkWidth
            chunkMeshes[chunkId]?.levelMaps["default"]?.stackedTileMapData[tileIdx]?.firstOrNull()?.let { tile ->
                val collisionIndex = (tile and 0xff00000) shr 20  // Get bits 20-27 for collision index
                if (collisionIndex != 0) {
                    // TODO Check shape of collision tile from collisionTileSet using collisionIndex and check if the point (cx, cy)
                    //      is actually colliding with the shape of the collision tile
                    true
                } else false  // No collision index, so we can assume it's a valid tile without collision
            } ?: false  // Tile is empty or no chunk mesh found for the chunk index
        } else false  // Outside levelGridVania bounds
    }

    /**
     * Iterate over all entities within the chunk, where the camera is currently located, and all
     * adjacent chunks. Call the callback function for each entity config.
     *
     * @param viewPortMiddlePosX Horizontal (middle) position of view port in world grid cells
     * @param viewPortMiddlePosY Vertical (middle) position of view port in world grid cells
     * @param activatedChunks Set of already activated chunk indices to avoid spawning entities multiple times when the
     *        player is moving within the same chunks
     * @param callback Callback function to call for each entity config name - parameter: entity config name
     */
    fun forEachEntityInChunk(viewPortMiddlePosX: Int, viewPortMiddlePosY: Int, activatedChunks: MutableSet<Int>, callback: (String) -> Unit) {
        // Calculate the grid position of the view port middle position
        val gridX: Int = viewPortMiddlePosX / levelChunkWidth
        val gridY: Int = viewPortMiddlePosY / levelChunkHeight

        // Check in which quadrant of the grid the view port is located
        // and iterate over the adjacent chunks (2x2 grid)
        val localViewPortPosX: Int = viewPortMiddlePosX % levelChunkWidth
        val localViewPortPosY: Int = viewPortMiddlePosY % levelChunkHeight

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

        for(x in startGridX .. endGridX) {
            for (y in startGridY .. endGridY) {
                // Check if the chunk is already spawned
                if (levelGridVania.inside(x, y)) {
                    // Get chunk from levelGridVania at grid cell coordinates
                    val chunk = levelGridVania[x, y]
                    if (!activatedChunks.contains(chunk)) {
                        activatedChunks.add(chunk)
                        // Now spawn the entities by providing each name to the callback lambda
                        chunkMeshes[chunk]?.entitiesToBeSpawned?.forEach { entityConfigName ->
                            callback(entityConfigName)
                        }
                    }
                }
            }
        }
    }

    /**
     * Iterate over all tiles within the given view port area and call the renderCall function for each tile.
     *
     * The view port area can overlap up to 4 level chunks, so we need to check in which chunks the view port corners are
     * located and iterate over the tiles of these chunks. The tile index in the level map is used to get the corresponding
     * bitmap slice from the tileset of the chunk, which is then passed to the renderCall function along with the tile's
     * position in pixels.
     *
     * The tile index in the level map is stored in bits 4-19 of the tile data, while bits 0-3 are used for the cluster index
     * to determine which tileset to use. The collision index is stored in bits 20-27 and can be used to get the corresponding
     * collision tile from the collision tile set.
     *
     * @param layer Name of the layer to render (e.g. "default", "background", "foreground") Currently only "default" layer
     *        is supported, but we can easily extend this to support multiple layers in the future. The layer name is used to
     *        get the corresponding level map from the chunk, which contains the tile data for that layer.
     * @param cx Horizontal position of top-left corner of view port in cell coordinates (tiles)
     * @param cy Vertical position of top-left corner of view port in cell coordinates (tiles)
     * @param width Width of view port in tiles
     * @param height Height of view port in tiles
     * @param renderCall Callback function to call for each tile - parameters: bitmap slice of the tile, x position in pixels,
     *        y position in pixels
     */
    fun forEachTile(layer: String, cx: Int, cy: Int, width: Int, height: Int, renderCall: (BmpSlice, Float, Float) -> Unit) {
        // Calculate the view port corners (top-left, top-right, bottom-left and bottom-right positions) in grid-vania indexes
        // and check if the corners are in different level chunks
        val gridX = cx / levelChunkWidth
        val gridY = cy / levelChunkHeight
        val gridX2 = (cx + width) / levelChunkWidth
        val gridY2 = (cy + height) / levelChunkHeight
        val xStart = cx % levelChunkWidth
        val yStart = cy % levelChunkHeight
        // Check if the view port area overlaps multiple level chunks
        if (gridX == gridX2) {
            // We have only one chunk in horizontal direction
            if (gridY == gridY2) {
                // We have only one chunk in vertical direction
                processTiles(layer, gridX, gridY, xStart, yStart, xStart + width, yStart + height, levelChunkWidth, levelChunkHeight, renderCall)
            } else {
                // We have vertically two chunks
                processTiles(layer, gridX, gridY, xStart, yStart, xStart + width, levelChunkHeight, levelChunkWidth, levelChunkHeight, renderCall)
                processTiles(layer, gridX, gridY2, xStart, 0, xStart + width, (yStart + height) % levelChunkHeight, levelChunkWidth, levelChunkHeight, renderCall)
            }
        } else {
            // We have horizontally two chunks
            if (gridY == gridY2) {
                // We have only one chunk in vertical direction
                processTiles(layer, gridX, gridY, xStart, yStart, levelChunkWidth, yStart + height, levelChunkWidth, levelChunkHeight, renderCall)
                processTiles(layer, gridX2, gridY, 0, yStart, (xStart + width) % levelChunkWidth, yStart + height, levelChunkWidth, levelChunkHeight, renderCall)
            } else {
                // We have vertical two levels
                processTiles(layer, gridX, gridY, xStart, yStart, levelChunkWidth, levelChunkHeight, levelChunkWidth, levelChunkHeight, renderCall)
                processTiles(layer, gridX2, gridY, 0, yStart, (xStart + width) % levelChunkWidth, levelChunkHeight, levelChunkWidth, levelChunkHeight, renderCall)
                processTiles(layer, gridX, gridY2, xStart, 0, levelChunkWidth, (yStart + height) % levelChunkHeight, levelChunkWidth, levelChunkHeight, renderCall)
                processTiles(layer, gridX2, gridY2, 0, 0, (xStart + width) % levelChunkWidth, (yStart + height) % levelChunkHeight, levelChunkWidth, levelChunkHeight, renderCall)
            }
        }
    }

    /**
     * Iterate over all collision tiles within the given view port area and call the renderCall function for each tile.
     *
     * @param layer Name of the layer to check for collision tiles (usually "default")
     * @param cx Horizontal position of top-left corner of view port in cell coordinates (tiles)
     * @param cy Vertical position of top-left corner of view port in cell coordinates (tiles)
     * @param width Width of view port in tiles
     * @param height Height of view port in tiles
     * @param renderCall Callback function to call for each collision tile; parameters: collision index, x position in pixels,
     *        y position in pixels
     */
    fun forEachCollisionTile(layer: String, cx: Int, cy: Int, width: Int, height: Int, renderCall: (BmpSlice, Float, Float) -> Unit) {
        // Calculate the view port corners (top-left, top-right, bottom-left and bottom-right positions) in gridvania indexes
        // and check if the corners are in different level maps (tileMapData)
        val gridX = cx / levelChunkWidth
        val gridY = cy / levelChunkHeight
        val gridX2 = (cx + width) / levelChunkWidth
        val gridY2 = (cy + height) / levelChunkHeight
        val xStart = cx % levelChunkWidth
        val yStart = cy % levelChunkHeight
        // Check if the view port area overlaps multiple levels
        if (gridX == gridX2) {
            // We have only one level in horizontal direction
            if (gridY == gridY2) {
                // We have only one level in vertical direction
                processCollisionTiles(layer, gridX, gridY, xStart, yStart, xStart + width, yStart + height, levelChunkWidth, levelChunkHeight, renderCall)
            } else {
                // We have vertically two levels
                processCollisionTiles(layer, gridX, gridY, xStart, yStart, xStart + width, levelChunkHeight, levelChunkWidth, levelChunkHeight, renderCall)
                processCollisionTiles(layer, gridX, gridY2, xStart, 0, xStart + width, (yStart + height) % levelChunkHeight, levelChunkWidth, levelChunkHeight, renderCall)
            }
        } else {
            // We have horizontal two levels
            if (gridY == gridY2) {
                // We have only one level in vertical direction
                processCollisionTiles(layer, gridX, gridY, xStart, yStart, levelChunkWidth, yStart + height, levelChunkWidth, levelChunkHeight, renderCall)
                processCollisionTiles(layer, gridX2, gridY, 0, yStart, (xStart + width) % levelChunkWidth, yStart + height, levelChunkWidth, levelChunkHeight, renderCall)
            } else {
                // We have vertical two levels
                processCollisionTiles(layer, gridX, gridY, xStart, yStart, levelChunkWidth, levelChunkHeight, levelChunkWidth, levelChunkHeight, renderCall)
                processCollisionTiles(layer, gridX2, gridY, 0, yStart, (xStart + width) % levelChunkWidth, levelChunkHeight, levelChunkWidth, levelChunkHeight, renderCall)
                processCollisionTiles(layer, gridX, gridY2, xStart, 0, levelChunkWidth, (yStart + height) % levelChunkHeight, levelChunkWidth, levelChunkHeight, renderCall)
                processCollisionTiles(layer, gridX2, gridY2, 0, 0, (xStart + width) % levelChunkWidth, (yStart + height) % levelChunkHeight, levelChunkWidth, levelChunkHeight, renderCall)
            }
        }
    }

    private fun processTiles(layer: String, gridX: Int, gridY: Int, xStart: Int, yStart: Int, xEnd: Int, yEnd: Int, levelWidth: Int, levelHeight: Int, renderCall: (BmpSlice, Float, Float) -> Unit) {
        val chunkIndex = levelGridVania[gridX, gridY]
        val chunk = chunkMeshes[chunkIndex] ?: return  // error("LevelData - processTiles: No chunk mesh found for chunk index '$chunkIndex' in grid position ($gridX, $gridY)!")
        val levelMap = chunk.levelMaps[layer] ?: error("LevelData - processTiles: No level map found for layer '$layer' in chunk index '$chunkIndex'!")
        val chunkX = chunk.chunkX
        val chunkY = chunk.chunkY
        for (tx in xStart until xEnd) {
            for (ty in yStart until yEnd) {
                val tiles = levelMap.stackedTileMapData[tx + ty * levelChunkWidth]
                tiles.forEach { tile ->
                    val clusterIndex = tile and 0xf           // Get bits 0-3 for cluster index
                    val tileIndex = (tile and 0xffff0) shr 4  // Get bits 4-16 for tile index in tileset
                    levelMap.listOfTileSets[clusterIndex][tileIndex]?.let { bmpSlice ->
                        val px = (tx * tileSize) + (chunkX * levelWidth * tileSize)
                        val py = (ty * tileSize) + (chunkY * levelHeight * tileSize)
                        renderCall(bmpSlice, px.toFloat(), py.toFloat())
                    }
                }
            }
        }
    }

    private fun processCollisionTiles(layer: String, gridX: Int, gridY: Int, xStart: Int, yStart: Int, xEnd: Int, yEnd: Int, levelWidth: Int, levelHeight: Int, renderCall: (BmpSlice, Float, Float) -> Unit) {
        val chunkIndex = levelGridVania[gridX, gridY]
        val chunk = chunkMeshes[chunkIndex] ?: return  // error("LevelData - processTiles: No chunk mesh found for chunk index '$chunkIndex' in grid position ($gridX, $gridY)!")
        val levelMap = chunk.levelMaps[layer] ?: error("LevelData - processTiles: No level map found for layer '$layer' in chunk index '$chunkIndex'!")
        val chunkX = chunk.chunkX
        val chunkY = chunk.chunkY
        for (tx in xStart until xEnd) {
            for (ty in yStart until yEnd) {
                levelMap.stackedTileMapData[tx + ty * levelChunkWidth].firstOrNull()?.let { tile ->
                    val collisionIndex = (tile and 0xff00000) shr 20  // Get bits 20-27 for collision index
                    if (collisionIndex != 0) {
                        // Get collision tile from collision tileset using collisionIndex and call renderCall with it
                        val collisionTile = collisionTileSet[collisionIndex]
                        val px = (tx * tileSize) + (chunkX * levelWidth * tileSize)
                        val py = (ty * tileSize) + (chunkY * levelHeight * tileSize)
                        renderCall(collisionTile, px.toFloat(), py.toFloat())
                    }
                }
            }
        }
    }
}