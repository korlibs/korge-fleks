package korlibs.korge.fleks.components.data


/**
 * This class is used to store the mesh data for a world chunk. It contains references to the neighboring chunks and
 * the list of entity names. Entity names are used to determine which entities need to be spawned in the chunk.
 *
 * Hint: Position of entities are always relative to the chunk.
 *
 * @param chunkTop Reference to the top neighboring chunk.
 * @param chunkBottom Reference to the bottom neighboring chunk.
 * @param chunkLeft Reference to the left neighboring chunk.
 * @param chunkRight Reference to the right neighboring chunk.
 * @param chunkX The x coordinate of the chunk in the world grid.
 * @param chunkY The y coordinate of the chunk in the world grid.
 * @param listOfEntityNames List of entity names which need to be spawned in the chunk.
 * @param stackedTiles List of stacked tiles for each grid in the chunk. Each grid can have up to 10 stacked tiles.
 * @param gridWidth The width of the tile map grid (number of tiles in x direction).
 * @param gridHeight The height of the tile map grid (number of tiles in y direction).
 * @param gridSize The size (width and height) of each tile in pixels.
 * @param clusterList List of asset cluster names which are needed to render the tile map. The cluster names are used to
 *        determine which tileset a tile is using.
 */
data class ChunkMesh(
    // Mesh data
    var chunkTop: ChunkMesh? = null,
    var chunkBottom: ChunkMesh? = null,
    var chunkLeft: ChunkMesh? = null,
    var chunkRight: ChunkMesh? = null,
    var chunkX: Int = 0,
    var chunkY: Int = 0,

    // Entity data
    val listOfEntityNames: MutableList<String> = MutableList(64) { "" },

    // Level map data
    val stackedTiles: List<MutableList<Int>> = List(4096) { MutableList(10) { -1 } },
    var gridWidth: Int = 0,
    var gridHeight: Int = 0,
    var gridSize: Int = 0,
    var clusterList: MutableList<String> = MutableList(16) { "" }
)
