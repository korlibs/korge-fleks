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
 *
 * @property worldMapData The world map data containing level tile geometry and collision layout.
 */
class CollisionHandler(
    private val worldMapData: WorldMapData
) {
    private val gridSize: Double = worldMapData.tileSize.toDouble()

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
        debugShapesComponent: DebugCollisionShapes?
    ): Boolean {
        for (i in 0..verticalSteps) {
            // Horizontal movement probe: sample y along the vertical leading edge (xPosMovedLeadingEdge)
            val yPosMovedProbe = if (yMovement < 0.0) {  // Moving up - Y Axis inverted
                val yPosMovedProbeFromBottom = yPosBottom - i * gridSize
                max(yPosMovedProbeFromBottom, yPosTop)
            } else {  // Moving down - Y Axis inverted
                val yPosMovedProbeFromTop = yPosTop + i * gridSize
                min(yPosMovedProbeFromTop, yPosBottom)
            }
            // Calculate the cell index and probe the cell for collision
            val cxProbe = (xPosMovedLeadingEdge / gridSize).toIntFloor()
            val cyProbe = (yPosMovedProbe / gridSize).toIntFloor()

            debugShapesComponent?.let { debugSaveRatioPointX(it, xPosMovedLeadingEdge, yPosMovedProbe) }
//            debugShapesComponent?.let { debugSaveGridCell(it, cxProbe, cyProbe, worldMapData.tileSize) }

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
        debugShapesComponent: DebugCollisionShapes? = null
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
//            debugShapesComponent?.let { debugSaveGridCell(it, cxProbe, cyProbe, worldMapData.tileSize) }

            if (worldMapData.hasCollision(cxProbe, cyProbe, fullSlopeCheck = true)) return true
        }
        return false
    }

    /**
     * Probes for slope collision height at the horizontal midpoint of the leading horizontal edge.
     *
     * Selects the top or bottom leading edge based on vertical movement direction and queries
     * `worldMapData` for any slope value defined at that cell and sub-pixel position.
     *
     * @param yMovement Vertical movement delta for the sub-step in pixels.
     * @param xCollisionRectLeft Left boundary $X$-coordinate of the collision box.
     * @param xCollisionRectRight Right boundary $X$-coordinate of the collision box.
     * @param yCollisionRectTop Top boundary $Y$-coordinate of the collision box.
     * @param yCollisionRectBottom Bottom boundary $Y$-coordinate of the collision box.
     * @param debugShapesComponent Optional component for visual debugging of probe points and checked grid cells.
     * @return The collision slope height value at the probed position.
     */
    fun probeHorizontalSlopes(
        yMovement: Double,
        xCollisionRectLeft: Double,
        xCollisionRectRight: Double,
        yCollisionRectTop: Double,
        yCollisionRectBottom: Double,
        debugShapesComponent: DebugCollisionShapes? = null
    ): Double {
        val xPosProbe = (xCollisionRectLeft + xCollisionRectRight) / 2.0
        val yPosProbe = if (yMovement < 0.0) yCollisionRectTop else yCollisionRectBottom  // Moving up - Y Axis inverted

        val cx: Int = (xPosProbe / gridSize).toIntFloor()
        val cy: Int = (yPosProbe / gridSize).toIntFloor()
        val xr: Double = xPosProbe % gridSize

        debugShapesComponent?.let { debugSaveRatioPointY(it, xPosProbe, yPosProbe) }

        return worldMapData.getCollisionSlope(cx, cy, xr)
    }

    /**
     * Probes for slope collision height beneath the collision box along both left and right bottom corners.
     *
     * Evaluates slope heights at the left and right $X$-coordinates at the target $Y$-position, returning
     * the maximum slope value found between both corners.
     *
     * @param xCollisionRectLeft Left boundary $X$-coordinate of the bottom edge of the collision box.
     * @param xCollisionRectRight Right boundary $X$-coordinate of the bottom edge of the collision box.
     * @param yCollisionRectBottom Target $Y$-coordinate along the bottom edge to probe.
     * @return The maximum slope height value between the left and right probed bottom corners.
     */
    fun probeSlopeBeneath(
        xCollisionRectLeft: Double,
        xCollisionRectRight: Double,
        yCollisionRectBottom: Double,
        debugShapesComponent: DebugCollisionShapes? = null
    ): Double {
        val xCollisionRectMiddle = (xCollisionRectLeft + xCollisionRectRight) / 2.0

        val cx: Int = (xCollisionRectMiddle / gridSize).toIntFloor()
        val cy: Int = (yCollisionRectBottom / gridSize).toIntFloor()
        val xr: Double = xCollisionRectMiddle % gridSize

        debugShapesComponent?.let { debugSaveGridCell(it, cx, cy, worldMapData.tileSize)}

        return worldMapData.getCollisionSlope(cx, cy, xr)
    }
}
