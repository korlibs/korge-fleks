package korlibs.korge.fleks.components

import com.github.quillraven.fleks.configureWorld
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Position.Companion.positionComponent
import korlibs.korge.fleks.gameState.GameStateManager
import korlibs.korge.fleks.utils.Pool
import korlibs.korge.fleks.utils.addKorgeFleksInjectables
import kotlin.test.Test
import kotlin.test.assertEquals


internal class PositionTest {
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
    fun testPositionSerialization() {
        println("TEST CASE: testPositionSerialization")

        val compUnderTest = positionComponent {
            x = 5.2f
            y = 42.1f
        }

        val entity = expectedWorld.entity {
            it += compUnderTest
        }

        CommonTestEnv.serializeDeserialize(expectedWorld, recreatedWorld)

        // get the component from entity with the same id from the new created world
        val newEntity = recreatedWorld.asEntityBag()[entity.id]
        val newCompUnderTest = with (recreatedWorld) { newEntity[PositionComponent] }

        assertEquals(compUnderTest.x, newCompUnderTest.x, "Check 'x' property to be equal")
        assertEquals(compUnderTest.y, newCompUnderTest.y, "Check 'y' property to be equal")

        // Delete the entity with the component from the expected world -> put component back to the pool
        expectedWorld.removeAll()

        Pool.doPoolUsageCheckAfterUnloading()
    }
//*/
}
