package korlibs.korge.fleks.utils

import korlibs.korge.fleks.components.DebugCollisionShapes
import kotlin.test.Test
import kotlin.test.assertEquals

class CollisionUtilsTest {

    @Test
    fun testDebugSaveRatioPointX() {
        val debugShapes = DebugCollisionShapes.staticDebugCollisionShapesComponent {}
        CollisionUtils.debugSaveRatioPointX(debugShapes, 10f, 20f)

        assertEquals(1, debugShapes.ratioPositionsX.size)
        assertEquals(10f, debugShapes.ratioPositionsX[0].x)
        assertEquals(20f, debugShapes.ratioPositionsX[0].y)
    }

    @Test
    fun testDebugSaveRatioPointY() {
        val debugShapes = DebugCollisionShapes.staticDebugCollisionShapesComponent {}
        CollisionUtils.debugSaveRatioPointY(debugShapes, 30f, 40f)

        assertEquals(1, debugShapes.ratioPositionsY.size)
        assertEquals(30f, debugShapes.ratioPositionsY[0].x)
        assertEquals(40f, debugShapes.ratioPositionsY[0].y)
    }
}
