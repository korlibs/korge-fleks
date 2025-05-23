package korlibs.korge.fleks.assets

import korlibs.datastructure.IArray2
import korlibs.datastructure.IArray2.Companion.forEachPosRect
import korlibs.datastructure.indexOr
import korlibs.math.geom.PointInt
import korlibs.math.geom.RectangleInt
import kotlinx.serialization.Serializable

@Serializable
data class ChunkConfig(
    var entitiesSpawned: Boolean = false,
    // var entities ...
//    var entityConfigNames: List<String>? = null
)

interface IChunkArray2 : IArray2<ChunkConfig> {
    fun setFast(idx: Int, value: ChunkConfig)
    fun getFast(idx: Int): ChunkConfig
    operator fun get(x: Int, y: Int): ChunkConfig = getFast(indexOr(x, y))
    operator fun set(x: Int, y: Int, value: ChunkConfig) = setFast(indexOr(x, y), value)
    override fun setAt(idx: Int, value: ChunkConfig) = setFast(idx, value)
    override fun getAt(idx: Int): ChunkConfig = getFast(idx)
    operator fun get(p: PointInt): ChunkConfig = get(p.x, p.y)
    operator fun set(p: PointInt, value: ChunkConfig) = set(p.x, p.y, value)
    operator fun set(rect: RectangleInt, value: ChunkConfig) = forEachPosRect(this, rect) { x, y -> this[x, y] = value }
}

@Serializable
class ChunkArray2(override val width: Int, override val height: Int, val data: Array<ChunkConfig>) : IChunkArray2 {
    init {
        IArray2.checkArraySize(width, height, data.size)
    }

    constructor(width: Int, height: Int) : this(width, height, Array(width * height) { ChunkConfig() } )

    companion object {
        val empty = ChunkArray2(0, 0)
    }

    override fun equals(other: Any?): Boolean {
        return (other is ChunkArray2) && this.width == other.width && this.height == other.height && this.data.contentEquals(
            other.data
        )
    }

    override fun hashCode(): Int = width + height + data.contentHashCode()
    // TODO: refactor for poolable interface
    fun clone() = ChunkArray2(width, height, data.clone2())
    override fun iterator(): Iterator<ChunkConfig> = data.iterator()
    override fun toString(): String = asString()

    override fun getFast(idx: Int): ChunkConfig = data.getOrElse(idx) { data[0] }
    override fun setFast(idx: Int, value: ChunkConfig) { if (idx in data.indices) data[idx] = value }
}

fun Array<ChunkConfig>.clone2(): Array<ChunkConfig> {
    val result = Array(size) { ChunkConfig() }
    for (i in indices) {
        result[i] = this[i].copy()
    }
    return result
}
