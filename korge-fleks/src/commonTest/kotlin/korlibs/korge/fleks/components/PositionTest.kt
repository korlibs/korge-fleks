package korlibs.korge.fleks.components

import com.github.quillraven.fleks.configureWorld
import kotlin.test.Test
import kotlin.test.assertEquals


internal class PositionTest {

    private val expectedWorld = configureWorld {}
    private val recreatedWorld = configureWorld {}

    @Test
    fun testPositionSerialization() {

        val compUnderTest = PositionComponent(
            x = 5.2f,
            y = 42.1f,
        )

        val entity = expectedWorld.entity {
            it += compUnderTest
        }

        CommonTestEnv.serializeDeserialize(expectedWorld, recreatedWorld)

        // get the component from entity with the same id from the new created world
        val newEntity = recreatedWorld.asEntityBag()[entity.id]
        val newCompUnderTest = with (recreatedWorld) { newEntity[PositionComponent] }

        assertEquals(compUnderTest.x, newCompUnderTest.x, "Check 'x' property to be equal")
        assertEquals(compUnderTest.y, newCompUnderTest.y, "Check 'y' property to be equal")
    }
}
