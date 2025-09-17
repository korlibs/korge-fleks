package korlibs.korge.fleks.components

import com.github.quillraven.fleks.configureWorld
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.Info.Companion.InfoComponent
import korlibs.korge.fleks.components.Info.Companion.infoComponent
import korlibs.korge.fleks.gameState.GameStateManager
import korlibs.korge.fleks.utils.Pool
import korlibs.korge.fleks.utils.addKorgeFleksInjectables
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DebugInfoTest {
//*
    private val assetStore = AssetStore().also { it.testing = true }
    private val gameState = GameStateManager()

    private val expectedWorld = configureWorld {
        addKorgeFleksInjectables(assetStore, gameState)
    }
    private val recreatedWorld = configureWorld {
        addKorgeFleksInjectables(assetStore, gameState)
    }

    @Test
    fun testDebugInfoSerialization() {
        println("TEST CASE: testDebugInfoSerialization")

        val info = infoComponent {
            name = "DebugTest"
            entityId = 42
        }

        val entity = expectedWorld.entity {
            it += info
        }

        CommonTestEnv.serializeDeserialize(expectedWorld, recreatedWorld)

        // get the component from entity with the same id from the new created world
        val newEntity = recreatedWorld.asEntityBag()[entity.id]
        val newInfo = with (recreatedWorld) { newEntity[InfoComponent] }

        assertEquals(info.name, newInfo.name, "Check 'name' property to be equal")
        assertEquals(info.entityId, newInfo.entityId, "Check 'entityId' property to be equal")

        // Delete the entity with the component from the expected world -> put component back to the pool
        expectedWorld.removeAll()

        Pool.doPoolUsageCheckAfterUnloading()
    }
//*/
}
