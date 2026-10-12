package korlibs.korge.fleks.utils

import korlibs.korge.fleks.components.Collision
import korlibs.korge.fleks.components.Position
import korlibs.math.toIntCeil
import korlibs.math.toIntFloor
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Utility functions for collision system.
 */
object CollisionUtils {
    /**
     * Check if the object stays inside the current grid cell after applying movement.
     *
     * Not used, because mostly irrelevant.
     *
     * @param positionComponent The position component of the object.
     * @param overallMovementX The overall movement in the X direction.
     * @param overallMovementY The overall movement in the Y direction.
     * @param collisionComponent The collision component of the object. It contains the collision rect.
     * @param gridSize The size of the grid.
     *
     * @return `true` if the object stays inside the current grid cell, `false` otherwise.
     */
//    fun staysInsideCurrentCell(
//        positionComponent: Position,
//        overallMovementX: Float,
//        overallMovementY: Float,
//        collisionComponent: Collision,
//        gridSize: Int
//    ): Boolean {
//        // Calculate X position of the collision box edge which is moving in positive direction
//        val x =
//            if (overallMovementX > 0f) positionComponent.x + collisionComponent.rect.x + collisionComponent.rect.width
//            else positionComponent.x + collisionComponent.rect.x
//        val currentCellX = (x / gridSize).toIntFloor()
//        val nextCellX = ((x + overallMovementX) / gridSize).toIntFloor()
//        if (nextCellX != currentCellX) return false
//
//        // Calculate Y position of the collision box edge which is moving in positive direction
//        val y =
//            if (overallMovementY > 0f) positionComponent.y + collisionComponent.rect.y + collisionComponent.rect.height
//            else positionComponent.y + collisionComponent.rect.y
//        val currentCellY = (y / gridSize).toIntFloor()
//        val nextCellY = ((y + overallMovementY) / gridSize).toIntFloor()
//        return nextCellY == currentCellY
//    }

    /**
     * Calculate the number of steps needed to move the entity in relation to the step size (usually 16x16 pixels or smaller).
     *
     * @param horizontalMovement The horizontal movement of the entity.
     * @param verticalMovement The vertical movement of the entity.
     * @param gridSize The size of a step. Can be smaller than the grid size.
     * @return The number of steps needed for collision checking.
     */
    fun getCollisionSteps(horizontalMovement: Float, verticalMovement: Float, gridSize: Float): Int =
        (max(abs(horizontalMovement), abs(verticalMovement)) / gridSize).toIntCeil()

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
     * @see getCollisionSteps
     */
    fun processCollisionSteps(
        steps: Int,
        overallMovementX: Float,
        overallMovementY: Float,
        callback: (movementX: Float, movementY: Float) -> Boolean)
    {
        if (steps > 0) {
            // Init movement and take into account that overall movement can be negative
            val movementX: Float = overallMovementX / steps
            val movementY: Float = overallMovementY / steps
            var i = 0

            while (i < steps) {
                val result = callback(movementX, movementY)
                if (result) break
                i++
            }
        }
    }
}
