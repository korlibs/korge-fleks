package korlibs.korge.fleks.components

import com.github.quillraven.fleks.configureWorld
import korlibs.korge.fleks.components.Info
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DebugInfoTest {

    private val expectedWorld = configureWorld {}
    private val recreatedWorld = configureWorld {}

    @Test
    fun testDebugInfoSerialization() {

        val info = Info(
            name = "DebugTest",
            showPivotPoint = true
        )

        val entity = expectedWorld.entity {
            it += info
        }

        CommonTestEnv.serializeDeserialize(expectedWorld, recreatedWorld)

        // get the component from entity with the same id from the new created world
        val newInfo = with (recreatedWorld) { recreatedWorld.asEntityBag()[entity.id][Info] }

        assertEquals(info.name, newInfo.name, "Check 'name' property to be equal")
        assertEquals(info.showPivotPoint, newInfo.showPivotPoint, "Check 'showPivotPoint' property to be equal")
    }
}
