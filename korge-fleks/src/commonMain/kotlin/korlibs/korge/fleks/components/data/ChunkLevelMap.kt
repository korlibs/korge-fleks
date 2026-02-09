package korlibs.korge.fleks.components.data

import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This class is used to store the tile map data for a world chunk.
 * It contains a list of stacked tiles for each position in the chunk and a list of cluster names which
 * are needed to render the tile map. The cluster names are used to determine which tileset a tile is using.
 *
 * Tile:
 *   [
 *     4 bits for tile index in tileset
 *     12 bits for tile position in the tileset atlas
 *     ... bits for collision data
 *   ]
 *
 * @param stackedTiles List of stacked tiles for each position in the chunk. Each position can have up to 10 stacked tiles.
 * @param gridWidth The width of the tile map grid (number of tiles in x direction).
 * @param gridHeight The height of the tile map grid (number of tiles in y direction
 * @param clusterList List of cluster names which are needed to render the tile map. The cluster names are used to
 *        determine which tileset a tile is using.
 */
// TODO rename to TileMap
@Serializable @SerialName("ChunkLevelMap")
class ChunkLevelMap private constructor(
    val stackedTiles: List<MutableList<Int>> = List(4096) { MutableList(10) { -1 } },
    var gridWidth: Int = 0,
    var gridHeight: Int = 0,
    var gridSize: Int = 0,
    var clusterList: MutableList<String> = MutableList(16) { "" }
) : Poolable<ChunkLevelMap> {
    // Init an existing data instance with data from another one
    override fun init(from: ChunkLevelMap) {
        error("Do not call 'init' on ChunkLevelMap!")
    }

    // Cleanup data instance manually
    // This is used for data instances when they are a value property of a component
    override fun cleanup() {
        gridWidth = 0
        gridHeight = 0
        gridSize = 0
        clusterList.fill("undefined")
    }

    // Clone a new data instance from the pool
    override fun clone(): ChunkLevelMap = chunkLevelMap {
        error("Do not call 'clone' on ChunkLevelMap, since it is not used as a value property of a component and does not" +
            " contain any data that needs to be cloned. Just use the chunkLevelMap function to get a new instance from the pool.")
    }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        //fun staticChunkLevelMap(config: ChunkLevelMap.() -> Unit): ChunkLevelMap =
        //    ChunkLevelMap().apply(config)

        // Use this function to get a new instance of the data object from the pool
        fun chunkLevelMap(config: ChunkLevelMap.() -> Unit): ChunkLevelMap =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "ChunkLevelMap") { ChunkLevelMap() }
    }
}