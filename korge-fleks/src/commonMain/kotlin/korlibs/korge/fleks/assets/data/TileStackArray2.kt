package korlibs.korge.fleks.assets.data

import korlibs.datastructure.IArray2
import korlibs.datastructure.IArray2.Companion.forEachPosRect
import korlibs.datastructure.indexOr
import korlibs.image.tiles.Tile
import korlibs.korge.fleks.utils.TileAsInt
import korlibs.math.geom.PointInt
import korlibs.math.geom.RectangleInt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable @SerialName("TileStack")
data class TileStack(
    var size: Int = 0,
    @Serializable(with = TileAsInt::class)
    val stack: Array<Tile> = Array(10) { Tile.ZERO }
) {
    override fun equals(other: Any?): Boolean =
        (other is TileStack) && this.size == other.size && this.stack.contentEquals(other.stack)
    override fun hashCode(): Int = size + stack.contentHashCode()
}

interface ITileStackArray2 : IArray2<TileStack> {
    fun setFast(idx: Int, value: TileStack)
    fun getFast(idx: Int): TileStack
    operator fun get(x: Int, y: Int): TileStack = getFast(indexOr(x, y))
    operator fun set(x: Int, y: Int, value: TileStack) = setFast(indexOr(x, y), value)
    override fun setAt(idx: Int, value: TileStack) = setFast(idx, value)
    override fun getAt(idx: Int): TileStack = getFast(idx)
    operator fun get(p: PointInt): TileStack = get(p.x, p.y)
    operator fun set(p: PointInt, value: TileStack) = set(p.x, p.y, value)
    operator fun set(rect: RectangleInt, value: TileStack) = forEachPosRect(this, rect) { x, y -> this[x, y] = value }
}

@Serializable @SerialName("TileStackArray2")
class TileStackArray2(override val width: Int, override val height: Int, val data: Array<TileStack>) : ITileStackArray2 {
    init {
        IArray2.checkArraySize(width, height, data.size)
    }

    constructor(width: Int, height: Int) : this(width, height, Array(width * height) { TileStack() } )

    companion object {
        val empty = TileStackArray2(0, 0)
    }

    override fun equals(other: Any?): Boolean =
        (other is TileStackArray2) && this.width == other.width && this.height == other.height && this.data.contentEquals(other.data)

    override fun hashCode(): Int = width + height + data.contentHashCode()
    // TODO: refactor for poolable interface
    fun clone() = TileStackArray2(width, height, data.clone2())
    override fun iterator(): Iterator<TileStack> = data.iterator()
    override fun toString(): String = asString()

    override fun getFast(idx: Int): TileStack = data.getOrElse(idx) { data[0] }
    override fun setFast(idx: Int, value: TileStack) { if (idx in data.indices) data[idx] = value }
}

fun Array<TileStack>.clone2(): Array<TileStack> {
    val result = Array(size) { TileStack() }
    for (i in indices) {
        result[i] = this[i].copy()
    }
    return result
}
