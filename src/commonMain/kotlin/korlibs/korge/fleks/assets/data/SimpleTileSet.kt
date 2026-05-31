package korlibs.korge.fleks.assets.data

import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.BmpSlice
import korlibs.image.tiles.TileMapData
import korlibs.image.tiles.TileShapeInfo
import korlibs.math.geom.RectangleInt


/**
 * A set of [BmpSlice] where all share a [width] and [height].
 * For performance’s sake, ideally, all the slices should be part of the same [Bitmap].
 *
 * Used along [TileMapData] to represent tiles.
 */
class SimpleTileSet private constructor(
    val tilesArray: Array<TileSetTileInfo?>,

    /** [width] of each tile */
    val width: Int,
    /** [height] of each tile */
    val height: Int
) {
    override fun toString(): String = "TileSet(size=${width}x$height, tiles=${tilesArray})"
    operator fun get(index: Int): BmpSlice? = tilesArray[index]?.slice
    var size: Int = tilesArray.size
        private set

    companion object {
        val EMPTY = SimpleTileSet(arrayOf(), 0, 0)

        operator fun invoke(
            tiles: List<List<Int>>,
            tilesetAtlases: List<BmpSlice>,
            tileWidth: Int,
            tileHeight: Int
        ): SimpleTileSet =
            SimpleTileSet(
                tilesArray = Array(tiles.size) { index ->
                    val tile = tiles[index]
                    val tilesetIndex = tile[0]
                    // If the first int in tile list is already -1, it means that the tile is empty and no further info is needed
                    if (tilesetIndex == -1) null  // empty tiles are null
                    else {
                        val x = tile[1]
                        val y = tile[2]
                        TileSetTileInfo(
                            id = index,
                            slice = tilesetAtlases[tilesetIndex].slice(RectangleInt(x, y, tileWidth, tileHeight))
                        )
                    }
                },
                width = tileWidth,
                height = tileHeight
            )
    }

    data class TileSetAnimationFrame(
        val tileId: Int,
        val duration: Float,
    )

    data class TileSetTileInfo(
        val id: Int,  // Could be removed
        val slice: BmpSlice,
        val frames: List<TileSetAnimationFrame> = emptyList()
    )
}
