package korlibs.korge.fleks.utils

import korlibs.korge.fleks.components.DebugCollisionShapes
import kotlin.test.Test
import kotlin.test.assertEquals

class CollisionUtilsTest {

    @Test
    fun testDebugSaveRatioPointX() {
        val debugShapes = DebugCollisionShapes.staticDebugCollisionShapesComponent {}
        CollisionUtils.debugSaveRatioPointX(debugShapes, 10.0, 20.0)

        assertEquals(1, debugShapes.ratioPositionsX.size)
        assertEquals(10.0, debugShapes.ratioPositionsX[0].x)
        assertEquals(20.0, debugShapes.ratioPositionsX[0].y)
    }

    @Test
    fun testDebugSaveRatioPointY() {
        val debugShapes = DebugCollisionShapes.staticDebugCollisionShapesComponent {}
        CollisionUtils.debugSaveRatioPointY(debugShapes, 30.0, 40.0)

        assertEquals(1, debugShapes.ratioPositionsY.size)
        assertEquals(30.0, debugShapes.ratioPositionsY[0].x)
        assertEquals(40.0, debugShapes.ratioPositionsY[0].y)
    }
}
