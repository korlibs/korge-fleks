package korlibs.korge.fleks.components.data

import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This class is used to store the tile map data for a game object. It is not ment to be used as level map data.
 * It contains a list of stacked tiles  and a list of cluster names which
 * are needed to render the tile map. The cluster names are used to determine which tileset a tile is using.
 *
 * Tile:
 *   [
 *     4 bits for tile index in tileset
 *     12 bits for tile position in the tileset atlas
 *     ... other bits not used
 *   ]
 *
 * @param stackedTiles List of stacked tiles. Each tile position can have up to 10 stacked tiles.
 * @param gridWidth The width of the tile map grid (number of tiles in x direction).
 * @param gridHeight The height of the tile map grid (number of tiles in y direction
 * @param clusterList List of cluster names which are needed to render the tile map. The cluster names are used to
 *        determine which tileset a tile is using.
 */
@Serializable @SerialName("TileMap")
class TileMap private constructor(
    val stackedTiles: List<MutableList<Int>> = List(4096) { MutableList(10) { -1 } },
    var gridWidth: Int = 0,
    var gridHeight: Int = 0,
    var gridSize: Int = 0,
    var clusterList: MutableList<String> = MutableList(16) { "" }
) : Poolable<TileMap> {
    // Init an existing data instance with data from another one
    override fun init(from: TileMap) {
        error("Do not call 'init' on TileMap!")
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
    override fun clone(): TileMap = tileMap {
        error("Do not call 'clone' on TileMap, since it is not used as a value property of a component and does not" +
            " contain any data that needs to be cloned. Just use the tileMap function to get a new instance from the pool.")
    }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        //fun staticTileMap(config: TileMap.() -> Unit): TileMap =
        //    TileMap().apply(config)

        // Use this function to get a new instance of the data object from the pool
        fun tileMap(config: TileMap.() -> Unit): TileMap =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "TileMap") { TileMap() }
    }
}