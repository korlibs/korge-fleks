package korlibs.korge.fleks.assets.data

import korlibs.datastructure.Extra
import korlibs.datastructure.IntMap
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.BitmapCoords
import korlibs.image.bitmap.Bitmaps
import korlibs.image.bitmap.BmpSlice
import korlibs.image.bitmap.bmp
import korlibs.image.tiles.TileMapData
import korlibs.image.tiles.TileShapeInfo
import korlibs.math.geom.SizeInt
import korlibs.time.FastDuration
import korlibs.time.fast
import korlibs.time.toDuration
import kotlin.getValue
import kotlin.time.Duration


data class TileSetAnimationFrame(
    val tileId: Int,
    val fastDuration: FastDuration,
) : Extra by Extra.Mixin() {
    val duration get() = fastDuration.toDuration()

    companion object {
        @Deprecated("", ReplaceWith("TileSetAnimationFrame(tileId, duration.fast)", "korlibs.image.tiles.TileSetAnimationFrame", "korlibs.time.fast"))
        operator fun invoke(tileId: Int, duration: Duration): TileSetAnimationFrame = TileSetAnimationFrame(tileId, duration.fast)
    }
}

data class TileSetTileInfo(
    val id: Int,
    val slice: BmpSlice,
    val frames: List<TileSetAnimationFrame> = emptyList(),
    val collision: TileShapeInfo? = null,
) : Extra by Extra.Mixin() {
    val width get() = slice.width
    val height get() = slice.height
    val name: String? get() = slice.name
}

/**
 * A set of [BmpSlice] where all share a [width] and [height].
 * For performance’s sake, ideally, all the slices should be part of the same [Bitmap].
 *
 * Used along [TileMapData] to represent tiles.
 */
class TileSet2 private constructor(
    val tilesMap: IntMap<TileSetTileInfo>,

    /** [width] of each tile */
    val width: Int = if (tilesMap.size == 0) 0 else tilesMap.firstValue().slice.width,
    /** [height] of each tile */
    val height: Int = if (tilesMap.size == 0) 0 else tilesMap.firstValue().slice.height
) {
    val tileSize: SizeInt get() = SizeInt(width, height)

    override fun toString(): String = "TileSet(size=${width}x$height, tiles=${tilesMap.keys.toList()})"

    val base: Bitmap by lazy { if (tilesMap.size == 0) Bitmaps.transparent.bmp else tilesMap.firstValue().slice.bmp }
    val hasMultipleBaseBitmaps by lazy { tilesMap.values.any { it !== null && it.slice.bmp !== base } }
    val infos by lazy { Array<TileSetTileInfo?>(tilesMap.keys.maxOrNull()?.plus(1) ?: 0) { tilesMap[it] } }
    val textures by lazy { Array<BitmapCoords?>(tilesMap.keys.maxOrNull()?.plus(1) ?: 0) { tilesMap[it]?.slice } }

    //init {
    //    println("texturesMap: ${texturesMap.toMap()}")
    //    println("textures: ${textures.size}")
    //}

    fun getInfo(index: Int): TileSetTileInfo? = infos.getOrNull(index)
    fun getSlice(index: Int): BmpSlice? = getInfo(index)?.slice
    operator fun get(index: Int): BmpSlice? = getSlice(index)

    companion object {
        val EMPTY = TileSet2(IntMap())

        operator fun invoke(
            tilesMap: IntMap<TileSetTileInfo>,
            width: Int = if (tilesMap.size == 0) 0 else tilesMap.firstValue().slice.width,
            height: Int = if (tilesMap.size == 0) 0 else tilesMap.firstValue().slice.height
        ): TileSet2 {
            return TileSet2(tilesMap, width, height)
        }
    }
}
