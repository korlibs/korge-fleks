package korlibs.korge.fleks.utils

import korlibs.korge.fleks.assets.data.WorldMapData
import korlibs.korge.fleks.components.DebugCollisionShapes
import korlibs.korge.fleks.components.data.Point.Companion.point

/**
 * Utility functions for collision system.
 */
object CollisionUtils {
    fun debugSaveGridCell(debugShapesComponent: DebugCollisionShapes, cx: Int, cy: Int, worldMapData: WorldMapData) {
        debugShapesComponent.gridCells.add(
            point {
                x = cx.toFloat() * worldMapData.tileSize
                y = cy.toFloat() * worldMapData.tileSize
            }
        )
    }

    fun debugSaveRatioPointX(debugShapesComponent: DebugCollisionShapes, x: Float, y: Float) {
        debugShapesComponent.ratioPositionsX.add(
            point {
                this.x = x
                this.y = y
            }
        )
    }
    fun debugSaveRatioPointY(debugShapesComponent: DebugCollisionShapes, x: Float, y: Float) {
        debugShapesComponent.ratioPositionsY.add(
            point {
                this.x = x
                this.y = y
            }
        )
    }
}
