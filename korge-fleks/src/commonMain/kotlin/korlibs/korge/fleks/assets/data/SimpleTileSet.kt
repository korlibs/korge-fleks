package korlibs.korge.fleks.assets.data

import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.BmpSlice
import korlibs.image.tiles.TileMapData
import korlibs.image.tiles.TileShapeInfo
import korlibs.math.geom.RectangleInt


data class TileSetAnimationFrame(
    val tileId: Int,
    val duration: Float,
)

data class TileSetTileInfo(
    val id: Int,  // Could be removed
    val slice: BmpSlice,
    val frames: List<TileSetAnimationFrame> = emptyList(),
    val collision: TileShapeInfo? = null,
)

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

    companion object {
        val EMPTY = SimpleTileSet(arrayOf(), 0, 0)

        operator fun invoke(
            tiles: Array<IntArray>,
            tilesetAtlases: List<BmpSlice>,
            tileWidth: Int,
            tileHeight: Int
        ): SimpleTileSet {
            return SimpleTileSet(
                tilesArray = Array(tiles.size) { index ->
                    val tile = tiles[index]
                    val tilesetIndex = tile[0]
                    val x = tile[1]
                    val y = tile[2]
                    // Put null if tile is empty (identified by x=0 and y=0)
                    if (x == 0 && y == 0) null
                    else {
                        val slice = tilesetAtlases[tilesetIndex].slice(RectangleInt(x, y, tileWidth, tileHeight))
                        TileSetTileInfo(
                            id = index,
                            slice = slice
                        )
                    }
                },
                width = tileWidth,
                height = tileHeight
            )
        }
    }
}
