package korlibs.korge.fleks.assets.data

import korlibs.datastructure.IntArray2
import korlibs.image.bitmap.BmpSlice


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
    // Size of the world in chunks in horizontal and vertical direction (grid-vania) - init with 1x1, but will be set to actual grid-vania size in init function
    var gridVaniaWidth: Int = 1
        private set
    var gridVaniaHeight: Int = 1
        private set
    // Size of a world chunk inside the grid-vania array in tiles (all levels have the same size)
    var chunkWidth: Int = 64
        private set
    var chunkHeight: Int = 64
        private set
    // Size of a world chunk in pixels
    var chunkPixelWidth: Int = 1024
        private set
    var chunkPixelHeight: Int = 1024
        private set

    // Size of a tile cell in pixels (all tiles have the same size) - default is 16x16 pixels
    var tileSize: Int = 16
        private set

    val chunkLookUpTable: MutableMap<Int, ChunkAssetInfo> = mutableMapOf()
    internal lateinit var chunkGridVania: IntArray2
    internal lateinit var collisionTileSet: CollisionTileSet

    internal var levelMidPointX: Int = chunkWidth / 2
    internal var levelMidPointY: Int = chunkHeight / 2

    // Mutexes for loading adjacent chunks to avoid loading the same chunk multiple times
    internal var loadingTopLeftChunk = false
    internal var loadingTopRightChunk = false
    internal var loadingBottomLeftChunk = false
    internal var loadingBottomRightChunk = false

    // Internal tracing variables
    private var listOfEmptyTilesetErrorsLogged: MutableSet<Pair<String, Int>> = mutableSetOf()

    fun init(
        worldWidth: Float,
        worldHeight: Float,
        gridVaniaWidth: Int,
        gridVaniaHeight: Int,
        chunkWidth: Int,
        chunkHeight: Int,
        tileSize: Int,
        collisionTiles: List<List<Int>>,
        collisionShapesBitmapSlice: BmpSlice
    ) {
        this.worldWidth = worldWidth
        this.worldHeight = worldHeight
        this.gridVaniaWidth = gridVaniaWidth
        this.gridVaniaHeight = gridVaniaHeight
        this.chunkWidth = chunkWidth
        this.chunkHeight = chunkHeight
        this.chunkPixelWidth = chunkWidth * tileSize
        this.chunkPixelHeight = chunkHeight * tileSize
        this.tileSize = tileSize
        this.levelMidPointX = chunkWidth / 2
        this.levelMidPointY = chunkHeight / 2

        // Set up grid-vania array
        chunkGridVania = IntArray2(gridVaniaWidth, gridVaniaHeight) { -1 }
        // Set up collision tile set
        collisionTileSet = CollisionTileSet(collisionTiles, collisionShapesBitmapSlice, tileSize, tileSize)
        // Reset tracing variables
        listOfEmptyTilesetErrorsLogged.clear()
    }

    internal enum class ViewPortPosition {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    fun getChunkIndex(cx: Int, cy: Int): Int =
        if (chunkGridVania.inside(cx, cy)) chunkGridVania[cx, cy]
        else -1  // Outside levelGridVania bounds

    /**
     * Check collision at cell position.
     * Position cannot get negative.
     *
     * @param cx Horizontal position in positive x direction (in pixel)
     * @param cy Vertical positon in positive y direction (in pixel)
     *
     * @return Returns true if position has a collision tile/cell attached otherwise false.
     */
    fun hasCollision(cx: Int, cy: Int): Boolean {
        // Get the chunk coordinates in the chunk Grid-vania array
        val gridCx =  cx / chunkWidth
        val gridCy = cy / chunkHeight

        // TODO refactor collision check and remove need for level Grid Array
        //      -> we can directly check the chunk mesh for the chunk which contains the cell position and check if the tile at the cell position has a collision index (bits 20-27) that is not 0
        return if (chunkGridVania.inside(gridCx, gridCy)) {
            // Get the local coordinates within the chunk
            val localCx = cx % chunkWidth
            val localCy = cy % chunkHeight
            // Check if the tile is not empty and if it has a collision index (bits 20-27) that is not 0
            val chunkId = chunkGridVania[gridCx, gridCy]
            val tileIdx = localCx + localCy * chunkWidth
            chunkLookUpTable[chunkId]?.levelMaps["default"]?.stackedTileMapData[tileIdx]?.firstOrNull()?.let { tile ->
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
        val gridX = cx / chunkWidth
        val gridY = cy / chunkHeight
        val gridX2 = (cx + width) / chunkWidth
        val gridY2 = (cy + height) / chunkHeight
        val xStart = cx % chunkWidth
        val yStart = cy % chunkHeight
        // Check if the view port area overlaps multiple level chunks
        if (gridX == gridX2) {
            // We have only one chunk in horizontal direction
            if (gridY == gridY2) {
                // We have only one chunk in vertical direction
                processTiles(layer, gridX, gridY, xStart, yStart, xStart + width, yStart + height, chunkWidth, chunkHeight, renderCall)
            } else {
                // We have vertically two chunks
                processTiles(layer, gridX, gridY, xStart, yStart, xStart + width, chunkHeight, chunkWidth, chunkHeight, renderCall)
                processTiles(layer, gridX, gridY2, xStart, 0, xStart + width, (yStart + height) % chunkHeight, chunkWidth, chunkHeight, renderCall)
            }
        } else {
            // We have horizontally two chunks
            if (gridY == gridY2) {
                // We have only one chunk in vertical direction
                processTiles(layer, gridX, gridY, xStart, yStart, chunkWidth, yStart + height, chunkWidth, chunkHeight, renderCall)
                processTiles(layer, gridX2, gridY, 0, yStart, (xStart + width) % chunkWidth, yStart + height, chunkWidth, chunkHeight, renderCall)
            } else {
                // We have vertical two levels
                processTiles(layer, gridX, gridY, xStart, yStart, chunkWidth, chunkHeight, chunkWidth, chunkHeight, renderCall)
                processTiles(layer, gridX2, gridY, 0, yStart, (xStart + width) % chunkWidth, chunkHeight, chunkWidth, chunkHeight, renderCall)
                processTiles(layer, gridX, gridY2, xStart, 0, chunkWidth, (yStart + height) % chunkHeight, chunkWidth, chunkHeight, renderCall)
                processTiles(layer, gridX2, gridY2, 0, 0, (xStart + width) % chunkWidth, (yStart + height) % chunkHeight, chunkWidth, chunkHeight, renderCall)
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
        val gridX = cx / chunkWidth
        val gridY = cy / chunkHeight
        val gridX2 = (cx + width) / chunkWidth
        val gridY2 = (cy + height) / chunkHeight
        val xStart = cx % chunkWidth
        val yStart = cy % chunkHeight
        // Check if the view port area overlaps multiple levels
        if (gridX == gridX2) {
            // We have only one level in horizontal direction
            if (gridY == gridY2) {
                // We have only one level in vertical direction
                processCollisionTiles(layer, gridX, gridY, xStart, yStart, xStart + width, yStart + height, chunkWidth, chunkHeight, renderCall)
            } else {
                // We have vertically two levels
                processCollisionTiles(layer, gridX, gridY, xStart, yStart, xStart + width, chunkHeight, chunkWidth, chunkHeight, renderCall)
                processCollisionTiles(layer, gridX, gridY2, xStart, 0, xStart + width, (yStart + height) % chunkHeight, chunkWidth, chunkHeight, renderCall)
            }
        } else {
            // We have horizontal two levels
            if (gridY == gridY2) {
                // We have only one level in vertical direction
                processCollisionTiles(layer, gridX, gridY, xStart, yStart, chunkWidth, yStart + height, chunkWidth, chunkHeight, renderCall)
                processCollisionTiles(layer, gridX2, gridY, 0, yStart, (xStart + width) % chunkWidth, yStart + height, chunkWidth, chunkHeight, renderCall)
            } else {
                // We have vertical two levels
                processCollisionTiles(layer, gridX, gridY, xStart, yStart, chunkWidth, chunkHeight, chunkWidth, chunkHeight, renderCall)
                processCollisionTiles(layer, gridX2, gridY, 0, yStart, (xStart + width) % chunkWidth, chunkHeight, chunkWidth, chunkHeight, renderCall)
                processCollisionTiles(layer, gridX, gridY2, xStart, 0, chunkWidth, (yStart + height) % chunkHeight, chunkWidth, chunkHeight, renderCall)
                processCollisionTiles(layer, gridX2, gridY2, 0, 0, (xStart + width) % chunkWidth, (yStart + height) % chunkHeight, chunkWidth, chunkHeight, renderCall)
            }
        }
    }

    private fun processTiles(layer: String, gridX: Int, gridY: Int, xStart: Int, yStart: Int, xEnd: Int, yEnd: Int, levelWidth: Int, levelHeight: Int, renderCall: (BmpSlice, Float, Float) -> Unit) {
        val chunkIndex = getChunkIndex(gridX, gridY)
        val chunk = chunkLookUpTable[chunkIndex] ?: return  // error("LevelData - processTiles: No chunk mesh found for chunk index '$chunkIndex' in grid position ($gridX, $gridY)!")
        val levelMap = chunk.levelMaps[layer] ?: error("LevelData - processTiles: No level map found for layer '$layer' in chunk index '$chunkIndex'!")
        if (levelMap.listOfTileSets.isEmpty()) {
            // No tileset assigned to level map, so we cannot render any tiles
            if (!listOfEmptyTilesetErrorsLogged.contains(layer to chunkIndex)) {
                println("ERROR: LevelData - processTiles: No tileset assigned to level map for layer '$layer' in chunk index '$chunkIndex'!")
                listOfEmptyTilesetErrorsLogged.add(layer to chunkIndex)
            }
            return
        }
        val chunkX = chunk.chunkX
        val chunkY = chunk.chunkY
        for (tx in xStart until xEnd) {
            for (ty in yStart until yEnd) {
                val tiles = levelMap.stackedTileMapData[tx + ty * chunkWidth]
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
        val chunkIndex = getChunkIndex(gridX, gridY)
        val chunk = chunkLookUpTable[chunkIndex] ?: return  // error("LevelData - processTiles: No chunk mesh found for chunk index '$chunkIndex' in grid position ($gridX, $gridY)!")
        val levelMap = chunk.levelMaps[layer] ?: error("LevelData - processTiles: No level map found for layer '$layer' in chunk index '$chunkIndex'!")
        val chunkX = chunk.chunkX
        val chunkY = chunk.chunkY
        for (tx in xStart until xEnd) {
            for (ty in yStart until yEnd) {
                levelMap.stackedTileMapData[tx + ty * chunkWidth].firstOrNull()?.let { tile ->
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