package korlibs.korge.fleks.assets.data

import korlibs.io.file.std.resourcesVfs
import korlibs.korge.fleks.assets.AssetModel
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.gameState.GameStateConfig
import korlibs.korge.fleks.utils.EntityConfigSerializer
import kotlinx.serialization.decodeFromString
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
     *
     * @param testing Flag to indicate that the game is running in testing mode. In that case sounds are not loaded since
     *                headless runners (Github actions) do not support sound playback.
     */
    suspend fun loadCommonAssets(testing: Boolean = false) {
        assetStore.testing = testing

        val vfs = resourcesVfs["common/config.yaml"]
//        if (vfs.exists()) {
        // TODO Check why exists() function does not work on Android

        val gameConfigString = vfs.readString()
        try {
            val commonConfig = configSerializer.yaml().decodeFromString<AssetModel>(gameConfigString)
            // Enable / disable hot reloading of common assets here
            assetStore.loadAssets(AssetType.COMMON, assetConfig = commonConfig)
        } catch (e: Throwable) { println("ERROR: Loading common assets - $e") }
//        } else println("WARNING: Cannot find common entity config file 'common/config.yaml'!")
    }

    /**
     * Function for loading word, level and special game config and assets. This function should be called
     * whenever new assets need to be loaded. I.e. also within a level when assets of type 'special' should
     * be loaded/reloaded.
     */
    suspend fun loadAssets(gameStateConfig: GameStateConfig) {
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
    }
}
