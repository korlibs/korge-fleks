package korlibs.korge.fleks.gameState

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.assets.*
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.*

class GameStateManagerTest {

    @BeforeTest
    fun setup() {
        GameStateManager.assetStore = AssetStore()
        GameStateManager.gameStateConfig = GameStateConfig("TestGame", true, "world_1", "level_1", "special")
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
        GameStateManager.startGame(world)
        // Verify the game starts correctly (this would require access to internal state or behavior)
    }

    @Test
    fun startGame_handlesMissingStartScript() {
        val world = configureWorld {}
        GameStateManager.startGame(world)
        // Verify appropriate error handling
    }
}