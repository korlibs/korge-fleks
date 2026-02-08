package korlibs.korge.fleks.assets.data

import korlibs.korge.fleks.utils.EntityConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ChunkAssetInfo(
    val version: List<Int> = emptyList(),
    val entities: List<EntityConfig> = emptyList(),
    val levelMap: Map<String, TileMapInfo>
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