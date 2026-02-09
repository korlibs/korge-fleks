package korlibs.korge.fleks.assets.data

import korlibs.korge.fleks.utils.EntityConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ChunkAssetInfo(
    @SerialName("v") val version: List<Int> = emptyList(),
    @SerialName("e") val entities: List<EntityConfig> = emptyList(),

    @SerialName("x") val chunkX: Int,
    @SerialName("y") val chunkY: Int,

    @SerialName("t") val chunkTop: Int,
    @SerialName("b") val chunkBottom: Int,
    @SerialName("l") val chunkLeft: Int,
    @SerialName("r") val chunkRight: Int,

    @SerialName("ls") val levelMaps: Map<String, TileMapInfo>
) {
    @Serializable
    data class TileMapInfo(
        @SerialName("s") val speedFactor: Float,
        @SerialName("m") val stackedTileMapData: List<List<Int>> = emptyList(),  // Stacked tile map data for each world level chunk
        @SerialName("w") val gridWidth: Int = 0,
        @SerialName("h") val gridHeight: Int = 0,
        @SerialName("g") val gridSize: Int = 0,

        @SerialName("c") val clusterList: List<String> = emptyList()  // Needed by renderer for offsets of tilesets in clusters
    )
}
