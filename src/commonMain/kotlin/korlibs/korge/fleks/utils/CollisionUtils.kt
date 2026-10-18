package korlibs.korge.fleks.utils

import korlibs.korge.fleks.assets.data.WorldMapData
import korlibs.korge.fleks.components.DebugCollisionShapes
import korlibs.korge.fleks.components.data.Point.Companion.point
import korlibs.math.toIntCeil
import kotlin.math.abs
import kotlin.math.max

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

    /**
     * Calculate the number of steps needed to move the entity in relation to the step size (usually 16x16 pixels or smaller).
     *
     * @param horizontalMovement The horizontal movement of the entity.
     * @param verticalMovement The vertical movement of the entity.
     * @param gridSize The size of a step. Can be smaller than the grid size.
     * @return The number of steps needed for collision checking.
     */
    fun getCollisionSteps(horizontalMovement: Float, verticalMovement: Float, gridSize: Float): Int {
        if (gridSize < 1f && gridSize > -1f) {
            println("ERROR: Step denominator is very small (${gridSize})!")
            return 0
        }
        val steps = (max(abs(horizontalMovement), abs(verticalMovement)) / gridSize).toIntCeil()
        if (steps > 100) {
            println("ERROR: Steps number is very high (${steps})!")
            return 100
        }
        return steps
    }

    /**
     * Process the collision steps and call the callback function for each step.
     *
     * @param steps The number of steps to process.
     * @param overallMovementX The overall movement in the X direction.
     * @param overallMovementY The overall movement in the Y direction.
     * @param callback The callback function to call for each step. It takes the movement in the X and Y
     *        direction as parameters and returns a boolean value indicating whether to stop processing
     *        the steps. If the callback returns `true`, the processing is stopped.
     *
     * @return `true` if the callback returned `true` for any step, `false` otherwise.
     * @see getCollisionSteps
     */
    fun processCollisionSteps(
        steps: Int,
        overallMovementX: Float,
        overallMovementY: Float,
        callback: (movementX: Float, movementY: Float) -> Boolean
    ): Boolean {
        if (steps > 0) {
            // Init movement and take into account that overall movement can be negative
            val movementX: Float = overallMovementX / steps
            val movementY: Float = overallMovementY / steps
            var i = 0

            while (i < steps) {
                val result = callback(movementX, movementY)
                if (result) return true
                i++
            }
        }
        return false
    }
}
