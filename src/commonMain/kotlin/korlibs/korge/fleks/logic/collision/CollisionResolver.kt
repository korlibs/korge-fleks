package korlibs.korge.fleks.logic.collision

import korlibs.korge.fleks.components.Collision
import korlibs.korge.fleks.components.Motion
import korlibs.korge.fleks.components.Motion.Companion.BOUNCE
import korlibs.korge.fleks.components.Motion.Companion.EXPLOSION_SLIDING
import korlibs.korge.fleks.components.Motion.Companion.GROUND_WALKING
import korlibs.korge.fleks.components.Position
import korlibs.math.toIntFloor

class CollisionResolver {
    // Check for divider of movement in X and Y direction
    private val guardMove = 0.01

    /**
     * Resolves a horizontal collision by adjusting horizontal velocity based on [Motion.surfaceReactionX],
     * snapping the entity's $X$-position outside the colliding tile cell, and updating collision flags (`right` or `left`).
     *
     * Surface reaction modes:
     * - [GROUND_WALKING]: Sets `velocityX = 0.0` (retains vertical velocity).
     * - [EXPLOSION_SLIDING]: Dampens `velocityX *= 0.07` and `velocityY *= 0.1` to slide along/into walls.
     * - [BOUNCE]: Inverts velocity `velocityX *= -1.0`.
     *
     * @param xMovement Horizontal movement delta for the sub-step in pixels.
     * @param xPosMovedLeadingEdge Target $X$-coordinate of the leading vertical edge after horizontal movement.
     * @param positionComponent The position component containing entity world coordinates.
     * @param motionComponent The motion component containing velocities and surface reaction modes.
     * @param collisionComponent The collision component containing rect dimensions and collision flags.
     * @param gridSize Size of a tile grid cell in pixels.
     */
    fun resolveCollisionHorizontally(
        xMovement: Double,
        xPosMovedLeadingEdge: Double,
        positionComponent: Position,
        motionComponent: Motion,
        collisionComponent: Collision,
        gridSize: Double
    ) {
        // Stop movement along the collision axis (X) or bounce off the surface
        when (motionComponent.surfaceReactionX) {
            GROUND_WALKING -> {
                motionComponent.velocityX = 0.0
                // velocityY not changed
            }
            EXPLOSION_SLIDING -> {
                motionComponent.velocityX *= 0.07  // Let the explosions move a bit into the wall
                motionComponent.velocityY *= 0.1   // and slide faster on the wall
            }
            BOUNCE -> {
                motionComponent.velocityX *= -1.0  // Bounce off the surface
            }
        }

        val cxMovedLeadingEdge = (xPosMovedLeadingEdge / gridSize).toIntFloor()

        // Resolve the collision - move the entity out of the collider cell horizontally and
        // move the entity vertically only by ratio of the horizontal movement
        val isMovingRight = xMovement > 0.0
        if (isMovingRight) {
            // Movement in right direction
            val collisionRectOffsetX = collisionComponent.rect.width + collisionComponent.rect.x
            val cxCell = cxMovedLeadingEdge
            // New position left to the collider cell
            positionComponent.x = cxCell * gridSize - guardMove - collisionRectOffsetX
            collisionComponent.right = true
        } else {
            // Movement in left direction
            val collisionRectOffsetX = -collisionComponent.rect.x
            val cxCell = cxMovedLeadingEdge + 1
            // New position right to the collider cell
            positionComponent.x = cxCell * gridSize + guardMove + collisionRectOffsetX
            collisionComponent.left = true
        }
    }

    /**
     * Resolves a vertical collision by adjusting vertical velocity based on [Motion.surfaceReactionY],
     * snapping the entity's $Y$-position outside the colliding tile cell, and updating collision flags (`isGrounded` or `isCollidingAbove`).
     *
     * Surface reaction modes:
     * - [GROUND_WALKING]: Sets `velocityY = 0.0` (retains horizontal velocity).
     * - [EXPLOSION_SLIDING]: Dampens `velocityX *= 0.1` and `velocityY *= 0.07`.
     * - [BOUNCE]: Inverts velocity `velocityY *= -1.0`.
     *
     * @param yMovement Vertical movement delta for the sub-step in pixels.
     * @param yPosMovedLeadingEdge Target $Y$-coordinate of the leading horizontal edge after vertical movement.
     * @param positionComponent The position component containing entity world coordinates.
     * @param motionComponent The motion component containing velocities and surface reaction modes.
     * @param collisionComponent The collision component containing rect dimensions and collision flags.
     * @param gridSize Size of a tile grid cell in pixels.
     */
    fun resolveCollisionVertically(
        yMovement: Double,
        yPosMovedLeadingEdge: Double,
        positionComponent: Position,
        motionComponent: Motion,
        collisionComponent: Collision,
        gridSize: Double,
        slopeValue: Double = NO_COLLISION_SLOPE
    ) {
        // Stop movement along the collision axis (Y) or bounce off the surface
        when (motionComponent.surfaceReactionY) {
            GROUND_WALKING -> {
                // velocityX not changed if no slope
                // TODO for slopes
                motionComponent.velocityY = 0.0
            }
            EXPLOSION_SLIDING -> {
                motionComponent.velocityX *= 0.1   // Let the explosions slide on the ground (or ceiling)
                motionComponent.velocityY *= 0.07  // and move them a bit into the ground
            }
            BOUNCE -> {
                motionComponent.velocityY *= -1.0  // Bounce off the surface
            }
        }

        val cyMovedLeadingEdge: Int = (yPosMovedLeadingEdge / gridSize).toIntFloor()

        // Resolve the collision - move the entity out of the collider cell vertically and
        // move the entity horizontally only by ratio of the vertical movement
        val isMovingDown = yMovement > 0.0
        if (isMovingDown) {  // Movement in bottom direction - Y Axis inverted !!!
            val collisionRectOffsetY = collisionComponent.rect.height + collisionComponent.rect.y
            val cyCell: Int = cyMovedLeadingEdge
            // New Position on top of the collider cell
            positionComponent.y = cyCell * gridSize - guardMove - collisionRectOffsetY - slopeValue  // Add slope value again --> Y-Axis inverted!
            collisionComponent.isGrounded = true
        } else {  // Movement in top direction
            val collisionRectOffsetY = -collisionComponent.rect.y  // Y Axis inverted - move the object downwards
            val cyCell: Int = cyMovedLeadingEdge + 1
            // New position bottom below the collider cell
            positionComponent.y = cyCell * gridSize + guardMove + collisionRectOffsetY
            collisionComponent.isCollidingAbove = true
        }
    }

}