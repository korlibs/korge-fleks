package korlibs.korge.fleks.assets.data

import korlibs.korge.fleks.utils.EntityConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
data class CommonChunkInfo(
    @SerialName("v") val version: List<Int> = emptyList(),
    @SerialName("x") val gridVaniaWidth: Int = 0,   // in tiles
    @SerialName("y") val gridVaniaHeight: Int = 0,  // in tiles
    @SerialName("w") val chunkWidth: Int = 0,   // in tiles
    @SerialName("h") val chunkHeight: Int = 0,  // in tiles
    @SerialName("t") val tileSize: Int = 0      // in pixels
)

@Serializable
data class ChunkAssetInfo(
    @SerialName("e") val entities: List<EntityConfig> = emptyList(),

    @SerialName("x") val chunkX: Int,  // in grid coordinates
    @SerialName("y") val chunkY: Int,

    @SerialName("t") val chunkTop: Int,
    @SerialName("b") val chunkBottom: Int,
    @SerialName("l") val chunkLeft: Int,
    @SerialName("r") val chunkRight: Int,

    @SerialName("ls") val levelMaps: Map<String, TileMapInfo>
) {
    @Transient
    lateinit var listOfEntityNames: List<String>
    @Transient
    var chunkPositionX: Float = 0f  // in pixels
    @Transient
    var chunkPositionY: Float = 0f

    @Serializable
    data class TileMapInfo(
        @SerialName("s") val speedFactor: Float,
        @SerialName("m") val stackedTileMapData: List<List<Int>> = emptyList(),  // Stacked tile map data for each world level chunk
//        @SerialName("w") val gridWidth: Int = 0,  TODO check if we need this
//        @SerialName("h") val gridHeight: Int = 0,
//        @SerialName("t") val gridSize: Int = 0,

        @SerialName("c") val clusterList: List<String> = emptyList()  // Needed by renderer for offsets of tilesets in clusters
    ) {
        @Transient
        lateinit var listOfTileSets: List<SimpleTileSet>
    }
}
