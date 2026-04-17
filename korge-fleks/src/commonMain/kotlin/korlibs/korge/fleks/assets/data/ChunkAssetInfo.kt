package korlibs.korge.fleks.assets.data

import korlibs.korge.fleks.entity.EntityBlueprint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


/**
 * Common chunk info which is shared across all world chunks and stored in a separate file.
 * This is done to avoid redundant storage of this information for each world chunk.
 *
 * @param version The version of the chunk asset info format. This is used for compatibility checks when loading chunk assets.
 * @param gridVaniaWidth The width of the Grid-vania world map in chunks.
 * @param gridVaniaHeight The height of the Grid-vania world map in chunks.
 * @param chunkWidth The width of a world chunk in tiles.
 * @param chunkHeight The height of a world chunk in tiles.
 * @param tileSize The size of a tile in pixels. Usually 16 pixels.
 * @param collisionTiles A list of collision tiles for each collision index from world chunk map. Each collision tile is represented as a
 *                       list of integers with the following format: [x, y, width, height], where x and y are the coordinates of the top-left
 *                       corner of the collision tile in pixels, and width and height are the dimensions of the collision tile in pixels.
 */
@Serializable
data class CommonChunkInfo(
    @SerialName("v") val version: List<Int> = emptyList(),
    @SerialName("x") val gridVaniaWidth: Int = 0,   // in chunks
    @SerialName("y") val gridVaniaHeight: Int = 0,  // in chunks
    @SerialName("w") val chunkWidth: Int = 0,       // in tiles
    @SerialName("h") val chunkHeight: Int = 0,      // in tiles
    @SerialName("t") val tileSize: Int = 0,         // in pixels
    @SerialName("c") val collisionTiles: List<List<Int>> = emptyList()  // Collision tiles for each collision index from world chunk map (tiles)
)


/**
 * Chunk asset info which is stored for each world chunk and contains information about the entities and tile maps in the chunk.
 *
 * @param entities List of entity configurations which are used to create entities in the world chunk.
 * @param entitiesToBeSpawned List of entity names which shall be spawned automatically by the WorldChunkSystem.
 * @param chunkX The x coordinate of the chunk within the grid-vania world map.
 * @param chunkY The y coordinate of the chunk within the grid-vania world map.
 * @param chunkTop The id of the neighbor chunk at the top of the chunk. -1 if there is no neighbor chunk at the top.
 * @param chunkBottom The id of the neighbor chunk at the bottom of the chunk. -1 if there is no neighbor chunk at the bottom.
 * @param chunkLeft The id of the neighbor chunk at the left of the chunk. -1 if there is no neighbor chunk at the left.
 * @param chunkRight The id of the neighbor chunk at the right of the chunk. -1 if there is no neighbor chunk at the right.
 * @param levelMaps A map of tile maps for the world chunk. The key is the name of the layer of the tile map
 *                  (e.g. "background", "main", "foreground") and the value is the tile map info for that layer.
 */
@Serializable
data class ChunkAssetInfo(
    @SerialName("e") val entities: List<EntityBlueprint> = emptyList(),
    @SerialName("s") val entitiesToBeSpawned: List<String> = emptyList(),

    @SerialName("x") val chunkX: Int,  // in grid coordinates
    @SerialName("y") val chunkY: Int,

    @SerialName("t") val chunkTop: Int,
    @SerialName("b") val chunkBottom: Int,
    @SerialName("l") val chunkLeft: Int,
    @SerialName("r") val chunkRight: Int,

    @SerialName("ls") val levelMaps: Map<String, TileMapInfo>
) {
    @Transient
    var chunkPositionX: Float = 0f  // in pixels
    @Transient
    var chunkPositionY: Float = 0f

    @Serializable
    data class TileMapInfo(
        @SerialName("s") val speedFactor: Float,
        @SerialName("m") val stackedTileMapData: List<List<Int>> = emptyList(),  // Stacked tile map data for each world level chunk
        @SerialName("w") val tileMapWidth: Int = 0,  // Needed for background and foreground layers - those layers have different tile map sizes
        @SerialName("h") val tileMapHeight: Int = 0,  // because they are scrolling with different speeds

        @SerialName("c") val clusterList: List<String> = emptyList()  // Needed by renderer for offsets of tilesets in clusters
    ) {
        @Transient
        lateinit var listOfTileSets: List<SimpleTileSet>
    }
}
