package korlibs.korge.fleks.logic.collision

import korlibs.korge.fleks.assets.data.WorldMapData
import korlibs.korge.fleks.components.DebugCollisionShapes
import korlibs.korge.fleks.utils.CollisionUtils.debugSaveGridCell
import korlibs.korge.fleks.utils.CollisionUtils.debugSaveRatioPointX
import korlibs.korge.fleks.utils.CollisionUtils.debugSaveRatioPointY
import korlibs.math.toIntFloor
import kotlin.math.max
import kotlin.math.min

const val NO_COLLISION_SLOPE = 0.0
const val FULL_COLLISION_SLOPE = 16.0

/**
 * This class handles collision detection for the game. It probes horizontally and vertically for collision
 * against static level geometry, including slopes.
 */
class CollisionHandler(
    private val worldMapData: WorldMapData
) {
    /**
     * Probes the horizontally leading edge of the moved collision box across [verticalSteps] sample points against static level geometry.
     *
     * Samples along the $Y$-axis from top-to-bottom (when moving down or stationary) or bottom-to-top (when moving up)
     * to check if any probed point intersects a solid tile cell in `worldMapData`.
     *
     * @param xMovement Horizontal movement delta for the sub-step in pixels.
     * @param yMovement Vertical movement delta for the sub-step in pixels.
     * @param yPosTop Top boundary coordinate of the collision box.
     * @param yPosBottom Bottom boundary coordinate of the collision box.
     * @param xPosMovedLeadingEdge Target $X$-coordinate of the leading vertical edge after horizontal movement.
     * @param verticalSteps Number of vertical sub-step sample points along the leading edge.
     * @param gridSize Size of a tile grid cell in pixels.
     * @param debugShapesComponent Optional component for visual debugging of probe points and checked grid cells.
     * @return `true` if any probed point intersects a solid level tile; `false` otherwise.
     */
    fun probeCollisionHorizontally(
        xMovement: Double,
        yMovement: Double,
        yPosTop: Double,
        yPosBottom: Double,
        xPosMovedLeadingEdge: Double,
        verticalSteps: Int,
        gridSize: Double,
        debugShapesComponent: DebugCollisionShapes?
    ): Boolean {
        for (i in 0..verticalSteps) {
            // Horizontal movement probe: sample y along the vertical leading edge (xPosMovedLeadingEdge)
            val yPosMovedProbe = if (yMovement < 0.0) {  // Moving up - Y Axis inverted
                val xPosMovedProbeFromBottom = yPosBottom - i * gridSize
                max(xPosMovedProbeFromBottom, yPosTop)
            } else {  // Moving down - Y Axis inverted
                val xPosMovedProbeFromTop = yPosTop + i * gridSize
                min(xPosMovedProbeFromTop, yPosBottom)
            }
            // Calculate the cell index and probe the cell for collision
            val cxProbe = (xPosMovedLeadingEdge / gridSize).toIntFloor()
            val cyProbe = (yPosMovedProbe / gridSize).toIntFloor()

            debugShapesComponent?.let { debugSaveRatioPointX(it, xPosMovedLeadingEdge, yPosMovedProbe) }
            debugShapesComponent?.let { debugSaveGridCell(it, cxProbe, cyProbe, worldMapData.tileSize) }

            if (worldMapData.hasCollision(cxProbe, cyProbe)) return true
        }
        return false
    }

    /**
     * Probes the vertically leading edge of the moved collision box across [horizontalSteps] sample points against static level geometry.
     *
     * Samples along the $X$-axis from left-to-right (when moving right or stationary) or right-to-left (when moving left)
     * to check if any probed point intersects a solid tile cell in `worldMapData`.
     *
     * @param xMovement Horizontal movement delta for the sub-step in pixels.
     * @param yMovement Vertical movement delta for the sub-step in pixels.
     * @param xPosMovedLeft Left boundary $X$-coordinate of the moved collision box.
     * @param xPosMovedRight Right boundary $X$-coordinate of the moved collision box.
     * @param yPosMovedLeadingEdge Target $Y$-coordinate of the leading horizontal edge after vertical movement.
     * @param horizontalSteps Number of horizontal sub-step sample points along the leading edge.
     * @param gridSize Size of a tile grid cell in pixels.
     * @param debugShapesComponent Optional component for visual debugging of probe points and checked grid cells.
     * @return `true` if any probed point intersects a solid level tile; `false` otherwise.
     */
    fun probeCollisionVertically(
        xMovement: Double,
        yMovement: Double,
        xPosMovedLeft: Double,
        xPosMovedRight: Double,
        yPosMovedLeadingEdge: Double,
        horizontalSteps: Int,
        gridSize: Double,
        debugShapesComponent: DebugCollisionShapes?
    ): Boolean {
        for (i in 0..horizontalSteps) {
            // Vertical movement probe: sample x along the horizontal leading edge (yPosMovedLeadingEdge)
            // e.g. when the player is standing on a platform --> we collide here
            val xPosMovedProbe = if (xMovement < 0.0) {  // Move to left side
                val xPosMovedProbeFromRight = xPosMovedRight - i * gridSize
                max(xPosMovedProbeFromRight, xPosMovedLeft)
            } else {  // Move to right side
                val xPosMovedProbeFromLeft = xPosMovedLeft + i * gridSize
                min(xPosMovedProbeFromLeft, xPosMovedRight)
            }
            // Calculate the cell index and probe the cell for collision
            val cxProbe = (xPosMovedProbe / gridSize).toIntFloor()
            val cyProbe = (yPosMovedLeadingEdge / gridSize).toIntFloor()

            debugShapesComponent?.let { debugSaveRatioPointY(it, xPosMovedProbe, yPosMovedLeadingEdge) }
            debugShapesComponent?.let { debugSaveGridCell(it, cxProbe, cyProbe, worldMapData.tileSize) }

            if (worldMapData.hasCollision(cxProbe, cyProbe, fullSlopeCheck = true)) return true
        }
        return false
    }

    /**
     * Probe for slopes on the left and right corners of the bottom collision rect edge.
     *
     * @param yMovement Vertical movement delta for the sub-step in pixels.
     * @param xPosLeft Left boundary $X$-coordinate of the collision rect edge.
     * @param yPosTop Top boundary $Y$-coordinate of the collision rect edge.
     * @param xPosRight Right boundary $X$-coordinate of the collision rect edge.
     * @param yPosBottom Bottom boundary $Y$-coordinate of the collision rect edge.
     * @param gridSize Size of a tile grid cell in pixels.
     * @param debugShapesComponent Optional component for visual debugging of probe points and checked grid cells.
     *
     * @return The maximum slope value between the left and right corners of the bottom collision rect edge.
     */
    fun probeHorizontalSlopes(
        yMovement: Double,
        xPosLeft: Double,
        yPosTop: Double,
        xPosRight: Double,
        yPosBottom: Double,
        gridSize: Double,
        debugShapesComponent: DebugCollisionShapes? = null
    ): Double {
        val yPosProbe = if (yMovement < 0.0) yPosTop else yPosBottom  // Moving up - Y Axis inverted

        val cxLeft: Int = (xPosLeft / gridSize).toIntFloor()
        val cyLeft: Int = (yPosProbe / gridSize).toIntFloor()
        val xrLeft: Double = xPosLeft % gridSize
        // Left Edge of the bottom collision rect edge
        val slopeLeft = worldMapData.getCollisionSlope(cxLeft, cyLeft, xrLeft)

        val cxRight: Int = (xPosRight / gridSize).toIntFloor()
        val cyRight: Int = (yPosProbe / gridSize).toIntFloor()
        val xrRight: Double = xPosRight % gridSize
        // Right Edge of the bottom collision rect edge
        val slopeRight = worldMapData.getCollisionSlope(cxRight, cyRight, xrRight)

//        println("slope left: $slopeLeft, slope right: $slopeRight")

        return max(slopeLeft, slopeRight)
    }
}
