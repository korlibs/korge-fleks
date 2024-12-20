package korlibs.korge.fleks.gameState

import com.github.quillraven.fleks.World
import korlibs.io.file.std.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.entity.EntityFactory
import korlibs.korge.fleks.utils.*
import korlibs.korge.ldtk.*
import kotlinx.serialization.*
import kotlinx.serialization.modules.*

/**
 * The game state manager is responsible for loading and managing the game state.
 * It loads the game config file 'game_config.yaml' and the common config file 'common/config.yaml' at the start of the game.
 */
object GameStateManager {
    var assetStore: AssetStore = AssetStore()
    var firstGameStart: Boolean = true

    internal lateinit var gameStateConfig: GameStateConfig

    /**
     * Register a new serializers module for the entity config serializer.
     */
    fun register(name: String, module: SerializersModule) {
        assetStore.configDeserializer.register(name, module)
    }

    /**
     * Init function for loading common game config and assets. Call this before any KorGE Scene is active.
     */
    suspend fun initGameState() {
        // Check for game config file
        val gameStateConfigVfs = resourcesVfs["game_config.yaml"]

//        if (gameStateConfigVfs.exists()) {
            // TODO Check why exits() function does not work on Android

            val gameStateConfigString = gameStateConfigVfs.readString()
            try {
                gameStateConfig = assetStore.configDeserializer.yaml().decodeFromString<GameStateConfig>(gameStateConfigString)
            } catch (e: Throwable) {
                gameStateConfig = GameStateConfig("", true, "", "", "")
                println("ERROR: Loading game state config - $e")
            }
//        } else {
//            gameStateConfig = GameStateConfig("", true, "", "", "")
//            println("ERROR: Cannot find game state config file 'game_config.yaml'!")
//        }
        firstGameStart = gameStateConfig.firstStart

        val vfs = resourcesVfs["common/config.yaml"]
//        if (vfs.exists()) {
            // TODO Check why exits() function does not work on Android

            val gameConfigString = vfs.readString()
            try {
                val commonConfig = assetStore.configDeserializer.yaml().decodeFromString<AssetModel>(gameConfigString)
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
    suspend fun loadAssets() {
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
                val worldConfig = assetStore.configDeserializer.yaml().decodeFromString<AssetModel>(gameConfigString)
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
                val worldConfig = assetStore.configDeserializer.yaml().decodeFromString<AssetModel>(gameConfigString)
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
                    val specialConfig = assetStore.configDeserializer.yaml().decodeFromString<AssetModel>(gameConfigString)
                    // Enable / disable hot reloading of common assets here
                    assetStore.loadAssets(AssetType.SPECIAL, assetConfig = specialConfig)
                } catch (e: Throwable) {
                    println("ERROR: Loading special assets - $e")
                }
//            } else println("WARNING: Cannot find special game config file '${gameStateConfig.special}/${gameStateConfig.special}/config.yaml'!")
        }
    }

    /**
     * This function is called to start the game. It will load the "start script" from the current LDtk level and
     * create and configure the very first entity of the game.
     *
     * Hint: Make sure that a game object with name "start_script" is present in each LDtk level. It can be of different
     * EntityConfig type. The type can be set in LDtk level map editor.
     */
    fun startGame(world: World) {
        // TODO: Check if save game is available and load it

        // Load start script from level
        val startScript = "${gameStateConfig.level}_start_script"
        if (EntityFactory.contains(startScript)) {
            println("INFO: Starting '${gameStateConfig.level}' with script: '$startScript'.")
            world.createAndConfigureEntity(startScript)
        }
        else println("Error: Cannot start '${gameStateConfig.level}'! EntityConfig with name '$startScript' does not exist!")
    }
}
