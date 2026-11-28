package korlibs.korge.fleks.utils

import korlibs.korge.fleks.components.DebugCollisionShapes
import korlibs.korge.fleks.components.data.Point.Companion.point

/**
 * Utility functions for collision system.
 */
object CollisionUtils {
    fun debugSaveGridCell(debugShapesComponent: DebugCollisionShapes, cx: Int, cy: Int, tileSize: Int) {
        debugShapesComponent.gridCells.add(
            point {
                x = (cx * tileSize).toDouble()
                y = (cy * tileSize).toDouble()
            }
        )
    }

    fun debugSaveRatioPointX(debugShapesComponent: DebugCollisionShapes, x: Double, y: Double) {
        debugShapesComponent.ratioPositionsX.add(
            point {
                this.x = x
                this.y = y
            }
        )
    }

    fun debugSaveRatioPointY(debugShapesComponent: DebugCollisionShapes, x: Double, y: Double) {
        debugShapesComponent.ratioPositionsY.add(
            point {
                this.x = x
                this.y = y
            }
        )
    }
}
