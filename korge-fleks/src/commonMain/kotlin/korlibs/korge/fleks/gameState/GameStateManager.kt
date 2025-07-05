package korlibs.korge.fleks.gameState

import com.github.quillraven.fleks.World
import korlibs.io.file.std.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.assets.data.AssetType
import korlibs.korge.fleks.entity.EntityFactory
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*
import kotlinx.serialization.modules.*

/**
 * The game state manager is responsible for loading and managing the game state.
 * It loads the game config file 'game_config.yaml' and the common config file 'common/config.yaml' at the start of the game.
 */
object GameStateManager {
    var assetStore: AssetStore = AssetStore()
    var configSerializer: EntityConfigSerializer = EntityConfigSerializer()

    var gameRunning: Boolean = true

    /**
     * Register a new serializers module for the entity config serializer.
     */
    fun register(name: String, module: SerializersModule) {
        configSerializer.register(name, module)
    }

    /**
     * Init function for loading common game config and assets. Call this before any KorGE Scene is active.
     */
    suspend fun initGameState(): GameStateConfig {
        val vfs = resourcesVfs["common/config.yaml"]
//        if (vfs.exists()) {
        // TODO Check why exits() function does not work on Android

        val gameConfigString = vfs.readString()
        try {
            val commonConfig = configSerializer.yaml().decodeFromString<AssetModel>(gameConfigString)
            // Enable / disable hot reloading of common assets here
            assetStore.loadAssets(AssetType.COMMON, assetConfig = commonConfig)
        } catch (e: Throwable) { println("ERROR: Loading common assets - $e") }
//        } else println("WARNING: Cannot find common entity config file 'common/config.yaml'!")

        // Check for game config file
        val gameStateConfigVfs = resourcesVfs["game_config.yaml"]

//        if (gameStateConfigVfs.exists()) {
            // TODO Check why exits() function does not work on Android

            val gameStateConfigString = gameStateConfigVfs.readString()
            return try {
                 configSerializer.yaml().decodeFromString<GameStateConfig>(gameStateConfigString)
            } catch (e: Throwable) {
                println("ERROR: Loading game state config - $e")
                GameStateConfig("jobesLegacy", 0, true, "world_1", "level_1", "intro", "start_intro")
            }
//        } else {
//            return GameStateConfig("jobesLegacy", 0, true, "", "", "")
//        }
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

    /**
     * This function is called to start the game. It will load a specific "start script" from the current LDtk world and
     * create and configure the very first entity of the game.
     *
     * Hint: Make sure that a game object with name start script name is present in each LDtk world. It can be of different
     * EntityConfig type. The type can be set in LDtk level map editor.
     */
    fun World.startGame(gameStateConfig: GameStateConfig, loadSnapshot: Boolean = false) {
        // Check if save game is available and load it
        if (loadSnapshot) {
            // Load save game
            println("INFO: Loading save game")
        } else {
            // Start new game - Load start script and create entity
            if (EntityFactory.contains(gameStateConfig.startScript)) {
                println("INFO: Start game with asset config '${gameStateConfig.world}_${gameStateConfig.level}_${gameStateConfig.special}' and start script '${gameStateConfig.startScript}'")
                createAndConfigureEntity(gameStateConfig.startScript)
            }
            else println("ERROR: Cannot start game! EntityConfig with name '${gameStateConfig.startScript}' does not exist!")
        }

    }
}
