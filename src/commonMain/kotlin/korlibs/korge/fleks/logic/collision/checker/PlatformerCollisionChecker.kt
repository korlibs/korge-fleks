package korlibs.korge.fleks.logic.collision.checker

import korlibs.korge.fleks.assets.data.WorldMapData
import korlibs.korge.fleks.assets.data.gameObject.CollisionRect
import korlibs.korge.fleks.components.DebugCollisionShapes
import korlibs.korge.fleks.components.data.Point.Companion.point
import korlibs.korge.fleks.logic.collision.GridPosition
import kotlin.math.ceil

class PlatformerCollisionChecker(
    private val worldMapData: WorldMapData
) : CollisionChecker() {

    private val grid = GridPosition()

    override fun checkXCollision(
        cx: Int,  // (cx, cy) cell where the pivot point is located
        cy: Int,
        xr: Double,  // ratio of the pivot point inside the cell
        yr: Double,
        velocityX: Double,
        collisionBox: CollisionRect,
        debugShapesComponent: DebugCollisionShapes?
    ): Int {
        val gridSize: Double = worldMapData.tileSize.toDouble()
        var dir = 0
        val yrTop: Double = yr + (collisionBox.y.toDouble() / gridSize)
        val yrBottom: Double = yr + ((collisionBox.y + collisionBox.height) / gridSize)

        // Check direction of movement
        if (velocityX > 0.0) {  // Moving right
            val xrRight: Double = xr + ((collisionBox.x + collisionBox.width) / gridSize)

            grid.setAndNormalizeX(cx, xrRight)  // Get cell of right corner of the collision box
            val checkDistance = grid.cx - cx  // Check distance between pivot point cell and right corner cell
            val checkRight: Double = checkDistance.toDouble() - 0.0001  // To avoid floating point precision issues

//            debugShapesComponent?.let { debugSaveRatioPointX(it, cx, cy, xrRight, yr) }

            // Cell coordinates of top corner of the collision box
            grid.setAndNormalizeY(cy, yrTop)  // Top corner of the collision box
            val cyTop = grid.cy
            // Check collision in bottom cell and if yr is greater than 1 (yr is over cell bounds)
            repeat( ceil(collisionBox.height / gridSize).toInt()) { i ->
//                debugShapesComponent?.let { debugSaveGridCell(it, cx + checkDistance, cyTop + i) }

                if (worldMapData.hasCollision(cx + checkDistance, cyTop + i) && xrRight >= checkRight) {  // Check the next X cell
                    dir = 1
                }
            }
            // Cell coordinates of bottom corner of the collision box
            grid.setAndNormalizeY(cy, yrBottom)  // Bottom corner of the collision box
            val cyBottom = grid.cy
//            debugShapesComponent?.let { debugSaveGridCell(it, cx + checkDistance, cyBottom) }
            if (worldMapData.hasCollision(cx + checkDistance, cyBottom) && xrRight >= checkRight) {
                dir = 1
            }
        } else if (velocityX < 0.0) {  // Moving left
            val xrLeft: Double = xr + (collisionBox.x.toDouble() / gridSize)

            grid.setAndNormalizeX(cx, xrLeft)  // Get cell of left corner of the collision box
            val checkDistance = cx - grid.cx  // Check distance between pivot point cell and left corner cell
            val checkLeft: Double = (1.0 - checkDistance) + 0.0001  // To avoid floating point precision issues

//            debugShapesComponent?.let { debugSaveRatioPointX(it, cx, cy, xrLeft, yr) }

            // Cell coordinates of top corner of the collision box
            grid.setAndNormalizeY(cy, yrTop)  // Top corner of the collision box
            val cyTop = grid.cy
            // Check collision in bottom cell and if yr is greater than 1 (yr is over cell bounds)
            repeat( ceil(collisionBox.height / gridSize).toInt()) { i ->
//                debugShapesComponent?.let { debugSaveGridCell(it, cx - checkDistance, cyTop + i) }

                if (worldMapData.hasCollision(cx - checkDistance, cyTop + i) && xrLeft <= checkLeft) {  // Check the next X cell
                    dir = -1
                }
            }
            // Cell coordinates of bottom corner of the collision box
            grid.setAndNormalizeY(cy, yrBottom)  // Bottom corner of the collision box
            val cyBottom = grid.cy
//            debugShapesComponent?.let { debugSaveGridCell(debugShapesComponent, cx - checkDistance, cyBottom) }
            if (worldMapData.hasCollision(cx - checkDistance, cyBottom) && xrLeft <= checkLeft) {
                dir = -1
            }
        }
        return dir
    }

    override fun checkYCollision(
        cx: Int,  // (cx, cy) cell where the pivot point is located
        cy: Int,
        xr: Double,  // ratio of the pivot point inside the cell
        yr: Double,
        velocityY: Double,
        collisionBox: CollisionRect,
        debugShapesComponent: DebugCollisionShapes?
    ): Int {
        val gridSize: Double = worldMapData.tileSize.toDouble()
        var dir = 0  // colliding in direction (-1 = up, 1 = down, 0 = no collision)

        // left and right X-coordinates of the collision rect
        val xrLeft: Double = xr + (collisionBox.x.toDouble() / gridSize)  // collisionBox.x is X-offset to the pivot point of the object
        val xrRight: Double = xr + ((collisionBox.x + collisionBox.width) / gridSize)

        // Check direction of movement
        if (velocityY > 0.0) {  // Moving down
            val yrBottom: Double = yr + ((collisionBox.y + collisionBox.height) / gridSize)  // bottom corner of the collision box

            grid.setAndNormalizeY(cy, yrBottom)  // Get cell of bottom corner of the collision box
            val checkDistance = grid.cy - cy  // Check distance between pivot point cell and bottom corner cell
            val checkBottom: Double = checkDistance.toDouble() - 0.0001  // To avoid floating point precision issues

            debugShapesComponent?.let { debugSaveRatioPointY(debugShapesComponent, cx, cy, xrLeft, yrBottom) }

            // Cell coordinates of left corner of the collision box
            grid.setAndNormalizeX(cx, xrLeft)  // Left corner of the collision box
            val cxLeft = grid.cx
            // Check collision in bottom cell and if yr is greater than 1 (yr is over cell bounds)
            repeat( ceil(collisionBox.width / gridSize).toInt()) { i ->
                debugShapesComponent?.let { debugSaveGridCell(debugShapesComponent, cxLeft + i, cy + checkDistance) }
//                debugShapesComponent?.let { debugSaveGridCell(debugShapesComponent, cxLeft + i, grid.cy) }

                if (worldMapData.hasCollision(cxLeft + i, cy + checkDistance) && yrBottom >= checkBottom) {  // Check the next Y cell
//                if (worldMapData.hasCollision(cxLeft + i, grid.cy) && yrBottom >= checkBottom) {  // Check the next Y cell
                    dir = 1
                    return@repeat
                }
            }
            // Cell coordinates of right corner of the collision box
            grid.setAndNormalizeX(cx, xrRight)  // Right corner of the collision box
            val cxRight = grid.cx
            debugShapesComponent?.let { debugSaveGridCell(debugShapesComponent, cxRight, cy + checkDistance) }
            if (worldMapData.hasCollision(cxRight, cy + checkDistance) && yrBottom >= checkBottom) {
                dir = 1
            }
        } else if (velocityY < 0.0) {  // Moving up
            val yrTop: Double = yr + (collisionBox.y.toDouble() / gridSize)

            grid.setAndNormalizeY(cy, yrTop)  // Get cell of top corner of the collision box
            val checkDistance = cy - grid.cy  // Check distance between pivot point cell and top corner cell
            val checkTop: Double = (1.0 - checkDistance) + 0.0001  // To avoid floating point precision issues

            debugShapesComponent?.let { debugSaveRatioPointY(debugShapesComponent, cx, cy, xr, yrTop) }

            // Cell coordinates of left corner of the collision box
            grid.setAndNormalizeX(cx, xrLeft)  // Left corner of the collision box
            val cxLeft = grid.cx
            // Check collision in bottom cell and if yr is greater than 1 (yr is over cell bounds)
            repeat( ceil(collisionBox.width / gridSize).toInt()) { i ->
                debugShapesComponent?.let { debugSaveGridCell(debugShapesComponent, cxLeft + i, cy - checkDistance) }

                if (worldMapData.hasCollision(cxLeft + i, cy - checkDistance) && yrTop <= checkTop) {  // Check the next Y cell
                    dir = -1
                }
            }
            // Cell coordinates of right corner of the collision box
            grid.setAndNormalizeX(cx, xrRight)  // Right corner of the collision box
            val cxRight = grid.cx
            debugShapesComponent?.let { debugSaveGridCell(debugShapesComponent, cxRight, cy - checkDistance) }
            if (worldMapData.hasCollision(cxRight, cy - checkDistance) && yrTop <= checkTop) {
                dir = -1
            }
        }
        return dir
    }

    private fun debugSaveGridCell(debugShapesComponent: DebugCollisionShapes, cx: Int, cy: Int) {
        debugShapesComponent.gridCells.add(
            point {
                x = cx.toDouble() * worldMapData.tileSize
                y = cy.toDouble() * worldMapData.tileSize
            }
        )
    }

    private fun debugSaveRatioPointX(debugShapesComponent: DebugCollisionShapes, cx: Int, cy: Int, xr: Double, yr: Double) {
        debugShapesComponent.ratioPositionsX.add(
            point {
                x = (cx.toDouble() + xr) * worldMapData.tileSize
                y = (cy.toDouble() + yr) * worldMapData.tileSize
            }
        )
    }

    private fun debugSaveRatioPointY(debugShapesComponent: DebugCollisionShapes, cx: Int, cy: Int, xr: Double, yr: Double) {
        debugShapesComponent.ratioPositionsY.add(
            point {
                x = (cx.toDouble() + xr) * worldMapData.tileSize
                y = (cy.toDouble() + yr) * worldMapData.tileSize
            }
        )
    }
}
