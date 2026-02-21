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
        config = try {
            val gameStateConfigString = resourcesVfs["game_config.yaml"].readString()
            Yaml().decodeFromString<GameStateConfig>(gameStateConfigString)
        } catch (e: Throwable) {
            println("ERROR: Loading game state config - $e")
            GameStateConfig("jobesLegacy", 0, true, "world_1", 1, "start_intro")
        }
    }
}
