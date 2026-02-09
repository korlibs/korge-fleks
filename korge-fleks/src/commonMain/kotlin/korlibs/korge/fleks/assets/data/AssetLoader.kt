package korlibs.korge.fleks.assets.data

import korlibs.io.file.std.resourcesVfs
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.gameState.GameStateConfig
import korlibs.korge.fleks.utils.EntityConfigSerializer
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

        // First load chunks and get list of asset clusters which need to be loaded.
        loadChunkAssets(worldName, chunkNumber)
        // TODO hardcoded for now
        loadChunkAssets(worldName, 2)
        loadChunkAssets(worldName, 3)
        loadChunkAssets(worldName, 4)
        loadChunkAssets(worldName, 5)
        loadChunkAssets(worldName, 6)
        loadChunkAssets(worldName, 7)

        assetStore.loadClusterAssets("world_1", "common")
        assetStore.loadClusterAssets("world_1", "intro")

/*
        // Sanity check - world needs to be always present
        if (gameStateConfig.world.isEmpty() || gameStateConfig.level.isEmpty()) {
            println("ERROR: World or level string is empty in game config! Cannot load game data!")
            return
        }

        // Start loading world assets
        val worldVfs = resourcesVfs[gameStateConfig.world + "/config.yaml"]
//        if (worldVfs.exists()) {
        // TODO Check why exits() function does not work on Android

        var gameConfigString = worldVfs.readString()
        try {
            val worldConfig = configSerializer.yaml().decodeFromString<AssetModel>(gameConfigString)
            // Enable / disable hot reloading of common assets here
            assetStore.loadAssets(AssetType.WORLD, assetConfig = worldConfig)
        } catch (e: Throwable) {
            println("ERROR: Loading world assets - $e")
        }
//        } else println("WARNING: Cannot find world game config file '${gameStateConfig.world}/config.yaml'!")

        // Start loading level assets
        val levelVfs = resourcesVfs[gameStateConfig.world + "/" + gameStateConfig.level + "/config.yaml"]
//        if (levelVfs.exists()) {
        gameConfigString = levelVfs.readString()
        try {
            val worldConfig = configSerializer.yaml().decodeFromString<AssetModel>(gameConfigString)
            // Enable / disable hot reloading of common assets here
            assetStore.loadAssets(AssetType.LEVEL, assetConfig = worldConfig)
        } catch (e: Throwable) {
            println("ERROR: Loading level assets - $e")
        }
//        } else println("WARNING: Cannot find level game config file '${gameStateConfig.world}/${gameStateConfig.level}/config.yaml'!")

        // Start loading special assets
        if (gameStateConfig.level.isNotEmpty()) {
            val vfs = resourcesVfs[gameStateConfig.world + "/" + gameStateConfig.level + "/" + gameStateConfig.special + "/config.yaml"]
//            if (vfs.exists()) {
            gameConfigString = vfs.readString()
            try {
                val specialConfig = configSerializer.yaml().decodeFromString<AssetModel>(gameConfigString)
                // Enable / disable hot reloading of common assets here
                assetStore.loadAssets(AssetType.SPECIAL, assetConfig = specialConfig)
            } catch (e: Throwable) {
                println("ERROR: Loading special assets - $e")
            }
//            } else println("WARNING: Cannot find special game config file '${gameStateConfig.special}/${gameStateConfig.special}/config.yaml'!")
        }
*/
    }

    private suspend fun loadChunkAssets(worldName: String, chunk: Int) {
        // TODO implement loading of chunk assets
//        resourcesVfs["${chunkPath}.json"].readKorgeFleksLevelDataChunk(
//            chunkPath
//        )
        // By deserializing the chunk json file the EntityConfig objects for the chunk will be created and registered in the EntityFactory.
        // The chunk asset info will be used to load the tile map and other assets for the chunk and to check if the chunk asset version is
        // compatible with the current version of the game.
        val chunkAssetInfo: ChunkAssetInfo = configSerializer.json().decodeFromString(resourcesVfs["${worldName}/level_data/chunk_${chunk}.json"].readString())

        // Get version info
        val major: Int = chunkAssetInfo.version[0]
        val minor: Int = chunkAssetInfo.version[1]
        val build: Int = chunkAssetInfo.version[2]
        // TODO Check later if asset version/build is compatible otherwise convert to new version


        println(chunkAssetInfo)

    }


}
