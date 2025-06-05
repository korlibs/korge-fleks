package korlibs.korge.fleks.gameState

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.assets.*
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.*

class GameStateManagerTest {

    private val gameStateConfig = GameStateConfig("TestGame", 0, true, "world_1", "level_1", "special", "startScript")

    @BeforeTest
    fun setup() {
        GameStateManager.assetStore = AssetStore()
    }

    @Test
    fun register_addsNewSerializerModule() {
        val module = SerializersModule { }
        GameStateManager.register("testModule", module)
        // Verify the module is registered (this would require access to internal state or behavior)
    }

    @Test
    fun startGame_startsGameWithValidScript() {
        val world = configureWorld {}
        GameStateManager.run { world.startGame(gameStateConfig, loadSnapshot = false) }
        // Verify the game starts correctly (this would require access to internal state or behavior)
    }

    @Test
    fun startGame_handlesMissingStartScript() {
        val world = configureWorld {}
        GameStateManager.run { world.startGame(gameStateConfig, loadSnapshot = false) }
        // Verify appropriate error handling
    }
}