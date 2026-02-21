package korlibs.korge.fleks.assets.data

import korlibs.image.bitmap.Bitmaps
import korlibs.image.bitmap.BmpSlice
import korlibs.math.geom.RectangleInt


/**
 * A set of [BmpSlice] where all share a [width] and [height].
 * For performance’s sake, ideally, all the slices should be part of the same [Bitmap].
 *
 * Used along [SimpleTileSet] to represent collision tiles.
 */
class CollisionTileSet private constructor(
    val tilesArray: Array<BmpSlice>,

    /** [width] of each tile */
    val width: Int,
    /** [height] of each tile */
    val height: Int
) {
    override fun toString(): String = "CollisionTileSet(size=${width}x$height, tiles=${tilesArray})"
    operator fun get(index: Int): BmpSlice = tilesArray[index]
    var size: Int = tilesArray.size
        private set

    companion object {
        val EMPTY = CollisionTileSet(arrayOf(), 0, 0)

        operator fun invoke(
            tiles: List<List<Int>>,
            collisionTileSetAtlas: BmpSlice,
            tileWidth: Int,
            tileHeight: Int
        ): CollisionTileSet =
            CollisionTileSet(
                tilesArray = Array(tiles.size) { index ->
                    val tile = tiles[index]
                    val x = tile[0]
                    // If the first int in tile list is already -1, it means that the tile is empty and no further info is needed
                    if (x == -1) Bitmaps.transparent // empty tiles do not contain collision info - so they are transparent
                    else if (tile.size > 1) {
                        val y = tile[1]
                        collisionTileSetAtlas.slice(RectangleInt(x, y, tileWidth, tileHeight))
                    } else error("Collision tile data is incomplete for tile index $index! Expected format: [x, y]")
                },
                width = tileWidth,
                height = tileHeight
            )
    }
}
