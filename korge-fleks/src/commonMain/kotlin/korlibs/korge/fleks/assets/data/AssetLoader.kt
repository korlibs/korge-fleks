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
        // TODO hardcoded for now - we need to load cluster assets depending on the position in the world
        assetStore.loadClusterAssets(world = 1, "common")
        assetStore.loadClusterAssets(world = 1, "intro")

        // TODO hardcoded for now
        loadChunkAssets("world_1/level_data/chunk_1")
        loadChunkAssets("world_1/level_data/chunk_2")
        loadChunkAssets("world_1/level_data/chunk_3")
        loadChunkAssets("world_1/level_data/chunk_4")
        loadChunkAssets("world_1/level_data/chunk_5")
        loadChunkAssets("world_1/level_data/chunk_6")
        loadChunkAssets("world_1/level_data/chunk_7")
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

    private suspend fun loadChunkAssets(chunkPath: String) {
        // TODO implement loading of chunk assets
//        resourcesVfs["${chunkPath}.json"].readKorgeFleksLevelDataChunk(
//            chunkPath
//        )
        val chunkAssetInfo: ChunkAssetInfo = configSerializer.json().decodeFromString(resourcesVfs["${chunkPath}.json"].readString())

        // Get version info
        val major: Int = chunkAssetInfo.version[0]
        val minor: Int = chunkAssetInfo.version[1]
        val build: Int = chunkAssetInfo.version[2]
        // Check later if asset version/build is compatible otherwise convert to new version

        // Load entities
        println(chunkAssetInfo)

    }


}
