package korlibs.korge.fleks.assets.data

import korlibs.image.bitmap.slice
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.gameState.GameStateConfig
import korlibs.korge.fleks.utils.EntityConfigSerializer
import korlibs.time.Stopwatch
import kotlinx.serialization.modules.SerializersModule


class AssetLoader(
    private val assetStore: AssetStore
) {
    var configSerializer: EntityConfigSerializer = EntityConfigSerializer()

    /**
     * Register a new serializers module for the entity config serializer.
     */
    fun register(name: String, module: SerializersModule) {
        configSerializer.register(name, module)
    }

    /**
     * Function for loading common game assets.
     */
    suspend fun loadCommonAssets() {
        assetStore.loadClusterAssets(clusterName = "common")
    }

    /**
     * Function for loading word, level and special game config and assets. This function should be called
     * whenever new assets need to be loaded. I.e. also within a level when assets of type 'special' should
     * be loaded/reloaded.
     */
    suspend fun loadAssets(gameStateConfig: GameStateConfig) {
        val worldName = gameStateConfig.worldName
        val chunkNumber  = gameStateConfig.chunk

        val sw = Stopwatch().start()
        print("INFO: AssetStore - Start loading world chunk '${worldName}/level_data/common'... ")

        val commonChunkInfo: CommonChunkInfo = configSerializer.json().decodeFromString(resourcesVfs["${worldName}/level_data/common.json"].readString())

        // Get version info
        val major: Int = commonChunkInfo.version[0]
        val minor: Int = commonChunkInfo.version[1]
        val build: Int = commonChunkInfo.version[2]
        // TODO Check later if asset version/build is compatible otherwise convert to new version

        // Load collision shapes for the world chunks
        val collisionShapes = resourcesVfs["${worldName}/level_data/collision_shapes.png"].readBitmap().slice()

        assetStore.worldMapData.init(
            worldWidth = (commonChunkInfo.gridVaniaWidth * commonChunkInfo.chunkWidth * commonChunkInfo.tileSize).toFloat(),
            worldHeight = (commonChunkInfo.gridVaniaHeight * commonChunkInfo.chunkHeight * commonChunkInfo.tileSize).toFloat(),
            levelChunkWidth = commonChunkInfo.chunkWidth,
            levelChunkHeight = commonChunkInfo.chunkHeight,
            tileSize = commonChunkInfo.tileSize,
            collisionTiles = commonChunkInfo.collisionTiles,
            collisionShapesBitmapSlice = collisionShapes
        )
        println("- Resources loaded in ${sw.elapsed}")

        // First load chunks and get list of asset clusters which need to be loaded.
        loadChunkAssets(worldName, chunkNumber)
        // TODO hardcoded for now
        loadChunkAssets(worldName, 2)
        loadChunkAssets(worldName, 3)
        loadChunkAssets(worldName, 4)
        loadChunkAssets(worldName, 5)
        loadChunkAssets(worldName, 6)
        loadChunkAssets(worldName, 7)
    }

    private suspend fun loadChunkAssets(worldName: String, chunk: Int) {
        val sw = Stopwatch().start()
        print("INFO: AssetStore - Start loading world chunk '${worldName}/level_data/chunk_${chunk}'... ")

        // By deserializing the chunk JSON file the EntityConfig objects for the chunk will be created and registered in the EntityFactory.
        // The chunk asset info will be used to load the tile map and other assets for the chunk and to check if the chunk asset version is
        // compatible with the current version of the game.
        val chunkAssetInfo: ChunkAssetInfo = configSerializer.json().decodeFromString(resourcesVfs["${worldName}/level_data/chunk_${chunk}.json"].readString())
        assetStore.addWorldChunk(chunk, chunkAssetInfo)

        println("- Resources loaded in ${sw.elapsed}")

        // Create list of tile sets for each level map in the chunk - this is needed for the renderer to get the correct tile set objects for rendering the tile maps of the chunk.
        chunkAssetInfo.levelMaps.forEach { (_, layer) ->
            // Load cluster assets for the tile map of the chunk if it was not loaded already
            layer.clusterList.forEach { clusterName ->
                assetStore.loadClusterAssets(worldName, clusterName)
            }
            layer.listOfTileSets = layer.clusterList.map { assetStore.getTileSet(it) }
        }
    }
}
