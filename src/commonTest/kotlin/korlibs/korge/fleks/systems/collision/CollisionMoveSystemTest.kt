package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.configureWorld
import korlibs.image.bitmap.Bitmap32
import korlibs.image.bitmap.slice
import korlibs.korge.fleks.assets.data.gameObject.CollisionRect
import korlibs.korge.fleks.components.Collision.Companion.staticCollisionComponent
import korlibs.korge.fleks.components.DebugCollisionShapes
import korlibs.korge.fleks.components.Motion.Companion.BOUNCE
import korlibs.korge.fleks.components.Motion.Companion.EXPLOSION_SLIDING
import korlibs.korge.fleks.components.Motion.Companion.GROUND_WALKING
import korlibs.korge.fleks.components.Motion.Companion.staticMotionComponent
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.components.Position.Companion.staticPositionComponent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CollisionMoveSystemTest {

    // Subclass CollisionMoveSystem to expose protected methods for unit testing
    private class TestCollisionMoveSystem : CollisionMoveSystem() {
        fun testIsNotAlmostZero(value: Double): Boolean = value.isNotAlmostZero()

        fun testGetCollisionSteps(horizontalMovement: Double, verticalMovement: Double, gridSize: Double): Int =
            getCollisionSteps(horizontalMovement, verticalMovement, gridSize)

        fun testProcessCollisionSteps(
            steps: Int,
            overallMovementX: Double,
            overallMovementY: Double,
            callback: (movementX: Double, movementY: Double) -> Boolean,
        ) = processCollisionSteps(steps, overallMovementX, overallMovementY, callback)

        fun testCalculateRatio(
            movement: Double,
            posInitialLeadingEdge: Double,
            posMovedLeadingEdge: Double,
            gridSize: Double,
        ): Double = calculateRatio(movement, posInitialLeadingEdge, posMovedLeadingEdge, gridSize)

        fun testProbeCollisionHorizontally(
            xMovement: Double,
            yMovement: Double,
            yPosTop: Double,
            yPosBottom: Double,
            xPosMovedLeadingEdge: Double,
            verticalSteps: Int,
            gridSize: Double,
            debugShapesComponent: DebugCollisionShapes? = null,
        ): Boolean = probeCollisionHorizontally(
            xMovement, yMovement, yPosTop, yPosBottom, xPosMovedLeadingEdge, verticalSteps, gridSize, debugShapesComponent
        )

        fun testProbeCollisionVertically(
            xMovement: Double,
            yMovement: Double,
            xPosMovedLeft: Double,
            xPosMovedRight: Double,
            yPosMovedLeadingEdge: Double,
            horizontalSteps: Int,
            gridSize: Double,
            debugShapesComponent: DebugCollisionShapes? = null,
        ): Boolean = probeCollisionVertically(
            xMovement, yMovement, xPosMovedLeft, xPosMovedRight, yPosMovedLeadingEdge, horizontalSteps, gridSize, debugShapesComponent
        )

        fun testResolveCollisionHorizontally(
            xMovement: Double,
            xPosMovedLeadingEdge: Double,
            positionComponent: Position,
            motionComponent: korlibs.korge.fleks.components.Motion,
            collisionComponent: korlibs.korge.fleks.components.Collision,
            gridSize: Double,
        ) = resolveCollisionHorizontally(
            xMovement, xPosMovedLeadingEdge, positionComponent, motionComponent, collisionComponent, gridSize
        )

        fun testResolveCollisionVertically(
            yMovement: Double,
            yPosMovedLeadingEdge: Double,
            positionComponent: Position,
            motionComponent: korlibs.korge.fleks.components.Motion,
            collisionComponent: korlibs.korge.fleks.components.Collision,
            gridSize: Double,
        ) = resolveCollisionVertically(
            yMovement, yPosMovedLeadingEdge, positionComponent, motionComponent, collisionComponent, gridSize
        )

        fun testCheckPlayfieldBoundaries(positionComponent: Position) =
            checkPlayfieldBoundaries(positionComponent)

        fun testCheckCollision(
            xMovement: Double,
            yMovement: Double,
            positionComponent: Position,
            motionComponent: korlibs.korge.fleks.components.Motion,
            collisionComponent: korlibs.korge.fleks.components.Collision,
            gridSize: Double,
            debugShapesComponent: DebugCollisionShapes? = null,
        ): Boolean = checkCollision(
            xMovement, yMovement, positionComponent, motionComponent, collisionComponent, gridSize, debugShapesComponent
        )
    }

    private lateinit var collisionMoveSystem: TestCollisionMoveSystem
    private val assetStore = korlibs.korge.fleks.assets.AssetStore()

    @Suppress("unused")
    private val world = configureWorld {
        injectables {
            assetStore.worldMapData.init(
                worldWidth = 512.0,
                worldHeight = 512.0,
                gridVaniaWidth = 4,
                gridVaniaHeight = 4,
                chunkWidth = 16,
                chunkHeight = 16,
                tileSize = 16,
                collisionTiles = emptyList<List<Int>>(),
                collisionShapesBitmapSlice = Bitmap32(1, 1).slice()
            )
            add("AssetStore", assetStore)
        }
        systems {
            add(TestCollisionMoveSystem().also { collisionMoveSystem = it })
        }
    }

    /**
     * Test: Verify that `Double.isNotAlmostZero()` correctly identifies values outside the 0.01 guard threshold.
     */
    @Test
    fun testIsNotAlmostZero() {
        assertFalse(collisionMoveSystem.testIsNotAlmostZero(0.0), "0.0 should be almost zero")
        assertFalse(collisionMoveSystem.testIsNotAlmostZero(0.005), "0.005 <= guard (0.01) should be almost zero")
        assertFalse(collisionMoveSystem.testIsNotAlmostZero(-0.005), "-0.005 <= guard (0.01) should be almost zero")
        assertTrue(collisionMoveSystem.testIsNotAlmostZero(0.02), "0.02 > guard should not be almost zero")
        assertTrue(collisionMoveSystem.testIsNotAlmostZero(-0.02), "-0.02 > guard should not be almost zero")
    }

    // -------------------------------------------------------------------------
    // Tests for getCollisionSteps()
    // -------------------------------------------------------------------------

    @Test
    fun testZeroMovementReturnsZeroSteps() {
        val steps = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 0.0,
            verticalMovement = 0.0,
            gridSize = 16.0,
        )
        assertEquals(0, steps, "Zero movement should result in 0 steps")
    }

    @Test
    fun testExactGridSizeMovementReturnsOneStep() {
        val stepsHorizontal = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 16.0,
            verticalMovement = 0.0,
            gridSize = 16.0,
        )
        assertEquals(1, stepsHorizontal, "Movement equal to grid size horizontally should result in 1 step")

        val stepsVertical = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 0.0,
            verticalMovement = 16.0,
            gridSize = 16.0,
        )
        assertEquals(1, stepsVertical, "Movement equal to grid size vertically should result in 1 step")
    }

    @Test
    fun testFractionalMovementBelowGridSizeRoundsUpToOneStep() {
        val stepsSmall = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 1.0,
            verticalMovement = 0.5,
            gridSize = 16.0,
        )
        assertEquals(1, stepsSmall, "Movement less than grid size should round up to 1 step")

        val stepsTiny = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 0.001,
            verticalMovement = 0.0,
            gridSize = 16.0,
        )
        assertEquals(1, stepsTiny, "Tiny non-zero movement should round up to 1 step")
    }

    @Test
    fun testMovementExceedingGridSizeRoundsUpToNextInteger() {
        val stepsSlightlyOverOneTile = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 16.1,
            verticalMovement = 0.0,
            gridSize = 16.0,
        )
        assertEquals(2, stepsSlightlyOverOneTile, "16.1px movement on 16px grid should round up to 2 steps")

        val stepsExactlyTwoTiles = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 32.0,
            verticalMovement = 0.0,
            gridSize = 16.0,
        )
        assertEquals(2, stepsExactlyTwoTiles, "32px movement on 16px grid should be exactly 2 steps")
    }

    @Test
    fun testNegativeMovementUsesAbsoluteValue() {
        val stepsNegativeHoriz = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = -16.0,
            verticalMovement = 0.0,
            gridSize = 16.0,
        )
        assertEquals(1, stepsNegativeHoriz, "Negative horizontal movement should produce positive steps")

        val stepsNegativeVert = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 0.0,
            verticalMovement = -32.0,
            gridSize = 16.0,
        )
        assertEquals(2, stepsNegativeVert, "Negative vertical movement should produce positive steps")

        val stepsBothNegative = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = -20.0,
            verticalMovement = -10.0,
            gridSize = 16.0,
        )
        assertEquals(2, stepsBothNegative, "Max absolute magnitude (-20 -> 20) over 16 grid size should round up to 2 steps")
    }

    @Test
    fun testMaxOfHorizontalAndVerticalIsUsed() {
        val stepsVerticalDominant = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 5.0,
            verticalMovement = 25.0,
            gridSize = 16.0,
        )
        assertEquals(2, stepsVerticalDominant, "Vertical movement (25) is max, 25/16 = 1.5625 -> 2 steps")

        val stepsHorizontalDominant = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 35.0,
            verticalMovement = 10.0,
            gridSize = 16.0,
        )
        assertEquals(3, stepsHorizontalDominant, "Horizontal movement (35) is max, 35/16 = 2.1875 -> 3 steps")
    }

    @Test
    fun testVaryingGridSizes() {
        val stepsGrid8 = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 16.0,
            verticalMovement = 0.0,
            gridSize = 8.0,
        )
        assertEquals(2, stepsGrid8, "16px movement on 8px grid should be 2 steps")

        val stepsGrid32 = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 16.0,
            verticalMovement = 0.0,
            gridSize = 32.0,
        )
        assertEquals(1, stepsGrid32, "16px movement on 32px grid should round up to 1 step")

        val stepsGrid1 = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 3.2,
            verticalMovement = 1.0,
            gridSize = 1.0,
        )
        assertEquals(4, stepsGrid1, "3.2px max movement on 1px grid should round up to 4 steps")
    }

    @Test
    fun testLargeMovements() {
        val stepsLarge = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 160.0,
            verticalMovement = 80.0,
            gridSize = 16.0,
        )
        assertEquals(10, stepsLarge, "160px movement on 16px grid should be 10 steps")

        val stepsLargeSlightlyOver = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 160.001,
            verticalMovement = 80.0,
            gridSize = 16.0,
        )
        assertEquals(11, stepsLargeSlightlyOver, "160.001px movement on 16px grid should round up to 11 steps")
    }

    // -------------------------------------------------------------------------
    // Tests for processCollisionSteps()
    // -------------------------------------------------------------------------

    @Test
    fun testProcessCollisionStepsZeroOrNegativeStepsDoesNotInvokeCallback() {
        var callCount = 0
        collisionMoveSystem.testProcessCollisionSteps(0, 10.0, 10.0) { _, _ ->
            callCount++
            true
        }
        assertEquals(0, callCount, "Callback should not be invoked when steps is 0")

        callCount = 0
        collisionMoveSystem.testProcessCollisionSteps(-5, 10.0, 10.0) { _, _ ->
            callCount++
            true
        }
        assertEquals(0, callCount, "Callback should not be invoked when steps is negative")
    }

    @Test
    fun testProcessCollisionStepsSingleStepCalculation() {
        val invocations = mutableListOf<Pair<Double, Double>>()
        collisionMoveSystem.testProcessCollisionSteps(1, 10.0, 5.0) { moveX, moveY ->
            invocations.add(Pair(moveX, moveY))
            false
        }

        assertEquals(1, invocations.size, "Callback should be invoked exactly once for 1 step")
        assertEquals(10.0, invocations[0].first, "movementX should equal overallMovementX / 1")
        assertEquals(5.0, invocations[0].second, "movementY should equal overallMovementY / 1")
    }

    @Test
    fun testProcessCollisionStepsSplitsMovementEquallyAcrossSteps() {
        val invocations = mutableListOf<Pair<Double, Double>>()
        collisionMoveSystem.testProcessCollisionSteps(2, 20.0, -10.0) { moveX, moveY ->
            invocations.add(Pair(moveX, moveY))
            false
        }

        assertEquals(2, invocations.size, "Callback should be invoked twice for 2 steps")
        assertEquals(10.0, invocations[0].first, "Step 1 movementX should be 20 / 2 = 10")
        assertEquals(-5.0, invocations[0].second, "Step 1 movementY should be -10 / 2 = -5")
        assertEquals(10.0, invocations[1].first, "Step 2 movementX should be 10")
        assertEquals(-5.0, invocations[1].second, "Step 2 movementY should be -5")
    }

    @Test
    fun testProcessCollisionStepsStopsImmediatelyOnCallbackTrue() {
        val invocations = mutableListOf<Pair<Double, Double>>()
        collisionMoveSystem.testProcessCollisionSteps(3, 30.0, 15.0) { moveX, moveY ->
            invocations.add(Pair(moveX, moveY))
            invocations.size == 1 // Return true on first step
        }

        assertEquals(1, invocations.size, "Callback should stop after first step when returning true")
    }

    // -------------------------------------------------------------------------
    // Tests for calculateRatio()
    // -------------------------------------------------------------------------

    @Test
    fun testCalculateRatioRightwardMovement() {
        val ratio = collisionMoveSystem.testCalculateRatio(
            movement = 10.0,
            posInitialLeadingEdge = 39.0,
            posMovedLeadingEdge = 49.0,
            gridSize = 16.0,
        )
        assertEquals(0.9, ratio, 0.001, "Ratio for 9px distance over 10px movement should be 0.9")
    }

    @Test
    fun testCalculateRatioLeftwardMovement() {
        val ratio = collisionMoveSystem.testCalculateRatio(
            movement = -10.0,
            posInitialLeadingEdge = 36.0,
            posMovedLeadingEdge = 26.0,
            gridSize = 16.0,
        )
        assertEquals(0.4, ratio, 0.001, "Ratio for 4px distance over -10px movement should be 0.4")
    }

    @Test
    fun testCalculateRatioZeroMovementReturnsZero() {
        val ratio = collisionMoveSystem.testCalculateRatio(
            movement = 0.0,
            posInitialLeadingEdge = 30.0,
            posMovedLeadingEdge = 30.0,
            gridSize = 16.0,
        )
        assertEquals(0.0, ratio, "Zero movement should produce 0 ratio")
    }

    // -------------------------------------------------------------------------
    // Tests for probeCollisionHorizontally() & probeCollisionVertically()
    // -------------------------------------------------------------------------

    @Test
    fun testProbeCollisionHorizontallyZeroMovementReturnsFalse() {
        val result = collisionMoveSystem.testProbeCollisionHorizontally(
            xMovement = 0.0,
            yMovement = 5.0,
            yPosTop = 100.0,
            yPosBottom = 129.0,
            xPosMovedLeadingEdge = 50.0,
            verticalSteps = 2,
            gridSize = 16.0,
        )
        assertFalse(result, "probeCollisionHorizontally should return false when xMovement is zero")
    }

    @Test
    fun testProbeCollisionVerticallyZeroMovementReturnsFalse() {
        val result = collisionMoveSystem.testProbeCollisionVertically(
            xMovement = 5.0,
            yMovement = 0.0,
            xPosMovedLeft = 100.0,
            xPosMovedRight = 117.0,
            yPosMovedLeadingEdge = 2785.0,
            horizontalSteps = 1,
            gridSize = 16.0,
        )
        assertFalse(result, "probeCollisionVertically should return false when yMovement is zero")
    }

    // -------------------------------------------------------------------------
    // Tests for resolveCollisionHorizontally()
    // -------------------------------------------------------------------------

    @Test
    fun testResolveCollisionHorizontallyMovingRight() {
        val pos = staticPositionComponent { x = 30.0; y = 100.0 }
        val motion = staticMotionComponent { velocityX = 100.0; velocityY = 50.0; surfaceReactionY = GROUND_WALKING }
        val collision = staticCollisionComponent {
            rect = CollisionRect(-8, -28, 17.0, 29.0)
        }

        collisionMoveSystem.testResolveCollisionHorizontally(
            xMovement = 10.0,
            xPosMovedLeadingEdge = 49.0, // cell 3 starts at 48px
            positionComponent = pos,
            motionComponent = motion,
            collisionComponent = collision,
            gridSize = 16.0,
        )

        assertEquals(0.0, motion.velocityX, "velocityX should be zeroed out on horizontal collision")
        assertEquals(50.0, motion.velocityY, "velocityY should remain unchanged when surfaceReactionY is GROUND_WALKING")
        assertTrue(collision.right, "collision.right should be set to true")
        assertEquals(38.999, pos.x, 0.001, "Position X should snap left of cell 3")
    }

    @Test
    fun testResolveCollisionHorizontallyExplosionSlidingDampensVelocity() {
        val pos = staticPositionComponent { x = 30.0; y = 100.0 }
        val motion = staticMotionComponent { velocityX = 100.0; velocityY = 50.0; surfaceReactionX = EXPLOSION_SLIDING }
        val collision = staticCollisionComponent {
            rect = CollisionRect(-8, -28, 17.0, 29.0)
        }

        collisionMoveSystem.testResolveCollisionHorizontally(
            xMovement = 10.0,
            xPosMovedLeadingEdge = 49.0,
            positionComponent = pos,
            motionComponent = motion,
            collisionComponent = collision,
            gridSize = 16.0,
        )

        assertEquals(7.0, motion.velocityX, 0.001, "velocityX should be multiplied by 0.07 for EXPLOSION_SLIDING")
        assertEquals(5.0, motion.velocityY, 0.001, "velocityY should be multiplied by 0.1 for EXPLOSION_SLIDING")
    }

    @Test
    fun testResolveCollisionHorizontallyBounceInvertsVelocity() {
        val pos = staticPositionComponent { x = 30.0; y = 100.0 }
        val motion = staticMotionComponent { velocityX = 100.0; velocityY = 50.0; surfaceReactionX = BOUNCE }
        val collision = staticCollisionComponent {
            rect = CollisionRect(-8, -28, 17.0, 29.0)
        }

        collisionMoveSystem.testResolveCollisionHorizontally(
            xMovement = 10.0,
            xPosMovedLeadingEdge = 49.0,
            positionComponent = pos,
            motionComponent = motion,
            collisionComponent = collision,
            gridSize = 16.0,
        )

        assertEquals(-100.0, motion.velocityX, 0.001, "velocityX should be inverted (-100) for BOUNCE")
        assertEquals(50.0, motion.velocityY, 0.001, "velocityY should remain unchanged for BOUNCE")
    }

    @Test
    fun testResolveCollisionHorizontallyMovingLeft() {
        val pos = staticPositionComponent { x = 50.0; y = 100.0 }
        val motion = staticMotionComponent { velocityX = -100.0; velocityY = 0.0 }
        val collision = staticCollisionComponent {
            rect = CollisionRect(-8, -28, 17.0, 29.0)
        }

        collisionMoveSystem.testResolveCollisionHorizontally(
            xMovement = -10.0,
            xPosMovedLeadingEdge = 31.0, // cell 1
            positionComponent = pos,
            motionComponent = motion,
            collisionComponent = collision,
            gridSize = 16.0,
        )

        assertEquals(0.0, motion.velocityX)
        assertEquals(0.0, motion.velocityY)
        assertTrue(collision.left, "collision.left should be set to true")
        assertEquals(40.001, pos.x, 0.001, "Position X should snap right of cell 1")
    }

    // -------------------------------------------------------------------------
    // Tests for resolveCollisionVertically()
    // -------------------------------------------------------------------------

    @Test
    fun testResolveCollisionVerticallyMovingDown() {
        val pos = staticPositionComponent { x = 100.0; y = 2771.0 }
        val motion = staticMotionComponent { velocityX = 0.0; velocityY = 100.0 }
        val collision = staticCollisionComponent {
            rect = CollisionRect(-8, -28, 17.0, 29.0)
        }

        collisionMoveSystem.testResolveCollisionVertically(
            yMovement = 10.0,
            yPosMovedLeadingEdge = 2785.0, // cell 174
            positionComponent = pos,
            motionComponent = motion,
            collisionComponent = collision,
            gridSize = 16.0,
        )

        assertEquals(0.0, motion.velocityX)
        assertEquals(0.0, motion.velocityY)
        assertTrue(collision.isGrounded, "collision.isGrounded should be set to true")
        assertEquals(2782.999, pos.y, 0.001, "Position Y should snap on top of ground cell 174")
    }

    @Test
    fun testResolveCollisionVerticallyZeroFrictionStopsHorizontalMotion() {
        val pos = staticPositionComponent { x = 100.0; y = 2771.0 }
        val motion = staticMotionComponent { velocityX = 50.0; velocityY = 100.0; surfaceReactionX = GROUND_WALKING }
        val collision = staticCollisionComponent {
            rect = CollisionRect(-8, -28, 17.0, 29.0)
        }

        collisionMoveSystem.testResolveCollisionVertically(
            yMovement = 10.0,
            yPosMovedLeadingEdge = 2785.0,
            positionComponent = pos,
            motionComponent = motion,
            collisionComponent = collision,
            gridSize = 16.0,
        )

        assertEquals(0.0, motion.velocityY, "velocityY should be zeroed out")
        assertEquals(50.0, motion.velocityX, "velocityX should be retained when surfaceReactionX is GROUND_WALKING")
    }

    @Test
    fun testResolveCollisionVerticallyMovingUp() {
        val pos = staticPositionComponent { x = 100.0; y = 100.0 }
        val motion = staticMotionComponent { velocityX = 0.0; velocityY = -100.0 }
        val collision = staticCollisionComponent {
            rect = CollisionRect(-8, -28, 17.0, 29.0)
        }

        collisionMoveSystem.testResolveCollisionVertically(
            yMovement = -10.0,
            yPosMovedLeadingEdge = 40.0, // cell 2
            positionComponent = pos,
            motionComponent = motion,
            collisionComponent = collision,
            gridSize = 16.0,
        )

        assertEquals(0.0, motion.velocityX)
        assertEquals(0.0, motion.velocityY)
        assertTrue(collision.isCollidingAbove, "collision.isCollidingAbove should be set to true")
        assertEquals(76.001, pos.y, 0.001, "Position Y should snap below ceiling cell 2")
    }

    // -------------------------------------------------------------------------
    // Tests for checkPlayfieldBoundaries()
    // -------------------------------------------------------------------------

    @Test
    fun testCheckPlayfieldBoundariesLeftBoundaryClamping() {
        val pos = staticPositionComponent { x = 5.0; offsetX = 0.0 }
        collisionMoveSystem.testCheckPlayfieldBoundaries(pos)

        assertEquals(16.0, pos.x, "Position X should be clamped to left guard border (16)")
    }

    @Test
    fun testCheckPlayfieldBoundariesNormalPositionUnchanged() {
        val pos = staticPositionComponent { x = 100.0; y = 100.0; offsetX = 0.0; offsetY = 0.0 }
        collisionMoveSystem.testCheckPlayfieldBoundaries(pos)

        assertEquals(100.0, pos.x, "Position X inside level should remain unchanged")
        assertEquals(100.0, pos.y, "Position Y inside level should remain unchanged")
    }

    // -------------------------------------------------------------------------
    // Tests for checkCollision()
    // -------------------------------------------------------------------------

    @Test
    fun testCheckCollisionNoCollision() {
        val pos = staticPositionComponent { x = 100.0; y = 100.0 }
        val motion = staticMotionComponent { velocityX = 100.0; velocityY = 50.0 }
        val collision = staticCollisionComponent {
            rect = CollisionRect(-8, -28, 17.0, 29.0)
        }

        val isColliding = collisionMoveSystem.testCheckCollision(
            xMovement = 10.0,
            yMovement = 5.0,
            positionComponent = pos,
            motionComponent = motion,
            collisionComponent = collision,
            gridSize = 16.0,
        )

        assertFalse(isColliding, "checkCollision should return false when no collision is detected")
        assertEquals(110.0, pos.x, 0.001, "Position X should be moved by xMovement")
        assertEquals(105.0, pos.y, 0.001, "Position Y should be moved by yMovement")
    }
}
