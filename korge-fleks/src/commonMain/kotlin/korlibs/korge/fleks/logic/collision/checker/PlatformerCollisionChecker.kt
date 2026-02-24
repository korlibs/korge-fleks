package korlibs.korge.fleks.logic.collision.checker

import com.github.quillraven.fleks.World.Companion.inject
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.assets.data.gameObject.CollisionData
import korlibs.korge.fleks.components.DebugCollisionShapes
import korlibs.korge.fleks.components.data.Point.Companion.point
import korlibs.korge.fleks.logic.collision.GridPosition
import korlibs.korge.fleks.utils.DebugPointPool
import kotlin.math.ceil

class PlatformerCollisionChecker(
    private val debugPointPool: DebugPointPool
) : CollisionChecker() {
    private val grid = GridPosition()
    private val worldMapData = inject<AssetStore>("AssetStore").worldMapData

    override fun checkXCollision(
        cx: Int,
        cy: Int,
        xr: Float,
        yr: Float,
        velocityX: Float,
        velocityY: Float,
        collisionBox: CollisionData,
        debugShapesComponent: DebugCollisionShapes?
    ): Int {
        val gridSize: Float = worldMapData.tileSize.toFloat()
        var dir = 0
        val yrTop: Float = yr + (collisionBox.y.toFloat() / gridSize)
        val yrBottom: Float = yr + ((collisionBox.y + collisionBox.height) / gridSize)

        // Check direction of movement
        if (velocityX > 0f) {  // Moving right
            val xrRight: Float = xr + ((collisionBox.x + collisionBox.width) / gridSize)

            grid.setAndNormalizeX(cx, xrRight)  // Get cell of right corner of the collision box
            val checkDistance = grid.cx - cx  // Check distance between pivot point cell and right corner cell
            val checkRight: Float = checkDistance.toFloat() - 0.0001f  // To avoid floating point precision issues

            debugShapesComponent?.let { debugSaveRatioPoint(it, cx, cy, xrRight, yr) }

            // Cell coordinates of top corner of the collision box
            grid.setAndNormalizeY(cy, yrTop)  // Top corner of the collision box
            val cyTop = grid.cy
            // Check collision in bottom cell and if yr is greater than 1 (yr is over cell bounds)
            repeat( ceil(collisionBox.height / gridSize).toInt()) { i ->
                debugShapesComponent?.let { debugSaveGridCell(it, cx + checkDistance, cyTop + i) }

                if (worldMapData.hasCollision(cx + checkDistance, cyTop + i) && xrRight >= checkRight) {  // Check the next X cell
                    dir = 1
                }
            }
            // Cell coordinates of bottom corner of the collision box
            grid.setAndNormalizeY(cy, yrBottom)  // Bottom corner of the collision box
            val cyBottom = grid.cy
            debugShapesComponent?.let { debugSaveGridCell(it, cx + checkDistance, cyBottom) }
            if (worldMapData.hasCollision(cx + checkDistance, cyBottom) && xrRight >= checkRight) {
                dir = 1
            }
        } else if (velocityX < 0f) {  // Moving left
            val xrLeft: Float = xr + (collisionBox.x / gridSize)

            grid.setAndNormalizeX(cx, xrLeft)  // Get cell of left corner of the collision box
            val checkDistance = cx - grid.cx  // Check distance between pivot point cell and left corner cell
            val checkLeft: Float = (1 - checkDistance).toFloat() + 0.0001f  // To avoid floating point precision issues

            debugShapesComponent?.let { debugSaveRatioPoint(it, cx, cy, xrLeft, yr) }

            // Cell coordinates of top corner of the collision box
            grid.setAndNormalizeY(cy, yrTop)  // Top corner of the collision box
            val cyTop = grid.cy
            // Check collision in bottom cell and if yr is greater than 1 (yr is over cell bounds)
            repeat( ceil(collisionBox.height / gridSize).toInt()) { i ->
                debugShapesComponent?.let { debugSaveGridCell(it, cx - checkDistance, cyTop + i) }

                if (worldMapData.hasCollision(cx - checkDistance, cyTop + i) && xrLeft <= checkLeft) {  // Check the next X cell
                    dir = -1
                }
            }
            // Cell coordinates of bottom corner of the collision box
            grid.setAndNormalizeY(cy, yrBottom)  // Bottom corner of the collision box
            val cyBottom = grid.cy
            debugShapesComponent?.let { debugSaveGridCell(debugShapesComponent, cx - checkDistance, cyBottom) }
            if (worldMapData.hasCollision(cx - checkDistance, cyBottom) && xrLeft <= checkLeft) {
                dir = -1
            }
        }
        return dir
    }

    override fun checkYCollision(
        cx: Int,
        cy: Int,
        xr: Float,
        yr: Float,
        velocityX: Float,
        velocityY: Float,
        collisionBox: CollisionData,
        debugShapesComponent: DebugCollisionShapes?
    ): Int {
        val gridSize: Float = worldMapData.tileSize.toFloat()
        var dir = 0
        val xrLeft: Float = xr + (collisionBox.x.toFloat() / gridSize)
        val xrRight: Float = xr + ((collisionBox.x + collisionBox.width) / gridSize)

        // Check direction of movement
        if (velocityY > 0f) {  // Moving down
            val yrBottom: Float = yr + ((collisionBox.y + collisionBox.height) / gridSize)

            grid.setAndNormalizeY(cy, yrBottom)  // Get cell of bottom corner of the collision box
            val checkDistance = grid.cy - cy  // Check distance between pivot point cell and bottom corner cell
            val checkBottom: Float = checkDistance.toFloat() - 0.0001f  // To avoid floating point precision issues

            debugShapesComponent?.let { debugSaveRatioPoint(debugShapesComponent, cx, cy, xr, yrBottom) }

            // Cell coordinates of left corner of the collision box
            grid.setAndNormalizeX(cx, xrLeft)  // Left corner of the collision box
            val cxLeft = grid.cx
            // Check collision in bottom cell and if yr is greater than 1 (yr is over cell bounds)
            repeat( ceil(collisionBox.width / gridSize).toInt()) { i ->
                debugShapesComponent?.let { debugSaveGridCell(debugShapesComponent, cxLeft + i, cy + checkDistance) }

                if (worldMapData.hasCollision(cxLeft + i, cy + checkDistance) && yrBottom >= checkBottom) {  // Check the next Y cell
                    dir = 1
                }
            }
            // Cell coordinates of right corner of the collision box
            grid.setAndNormalizeX(cx, xrRight)  // Right corner of the collision box
            val cxRight = grid.cx
            debugShapesComponent?.let { debugSaveGridCell(debugShapesComponent, cxRight, cy + checkDistance) }
            if (worldMapData.hasCollision(cxRight, cy + checkDistance) && yrBottom >= checkBottom) {
                dir = 1
            }
        } else if (velocityY < 0f) {  // Moving up
            val yrTop: Float = yr + (collisionBox.y.toFloat() / gridSize)

            grid.setAndNormalizeY(cy, yrTop)  // Get cell of top corner of the collision box
            val checkDistance = cy - grid.cy  // Check distance between pivot point cell and top corner cell
            val checkTop: Float = (1 - checkDistance).toFloat() + 0.0001f  // To avoid floating point precision issues

            debugShapesComponent?.let { debugSaveRatioPoint(debugShapesComponent, cx, cy, xr, yrTop) }

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
                x = cx.toFloat() * worldMapData.tileSize
                y = cy.toFloat() * worldMapData.tileSize
            }
        )
    }

    private fun debugSaveRatioPoint(debugShapesComponent: DebugCollisionShapes, cx: Int, cy: Int, xr: Float, yr: Float) {
        debugShapesComponent.ratioPositions.add(
            point {
                x = (cx.toFloat() + xr) * worldMapData.tileSize
                y = (cy.toFloat() + yr) * worldMapData.tileSize
            }
        )
    }
}
