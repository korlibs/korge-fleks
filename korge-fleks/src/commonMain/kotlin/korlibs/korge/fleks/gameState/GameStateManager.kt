package korlibs.korge.fleks.gameState

import com.charleskorn.kaml.Yaml
import korlibs.io.file.std.resourcesVfs
import kotlinx.serialization.decodeFromString


/**
 * The game state manager is responsible for loading and managing the game state.
 * It loads the game config file 'game_config.yaml' and the common config file 'common/config.yaml' at the start of the game.
 */
class GameStateManager {

    var gameRunning: Boolean = true

    lateinit var config: GameStateConfig

    suspend fun loadGameConfig() {
        // Check for game config file
        val gameStateConfigVfs = resourcesVfs["game_config.yaml"]

//        if (gameStateConfigVfs.exists()) {
        // TODO Check why exits() function does not work on Android

        val gameStateConfigString = gameStateConfigVfs.readString()
        config = try {
            Yaml().decodeFromString<GameStateConfig>(gameStateConfigString)
        } catch (e: Throwable) {
            println("ERROR: Loading game state config - $e")
            GameStateConfig("jobesLegacy", 0, true, "world_1", "level_1", "intro", "start_intro")
        }
//        } else {
//            return GameStateConfig("jobesLegacy", 0, true, "", "", "")
//        }

    }
}
