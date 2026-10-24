package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.configureWorld
import korlibs.image.bitmap.Bitmap32
import korlibs.image.bitmap.slice
import korlibs.korge.fleks.assets.data.gameObject.CollisionRect
import korlibs.korge.fleks.components.Collision.Companion.staticCollisionComponent
import korlibs.korge.fleks.components.DebugCollisionShapes
import korlibs.korge.fleks.components.Motion.Companion.staticMotionComponent
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.components.Position.Companion.staticPositionComponent
import korlibs.korge.fleks.components.assetStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CollisionMoveSystemTest {

    // Subclass CollisionMoveSystem to expose protected methods for unit testing
    private class TestCollisionMoveSystem : CollisionMoveSystem() {
        fun testIsNotAlmostZero(value: Float): Boolean = value.isNotAlmostZero()

        fun testGetCollisionSteps(horizontalMovement: Float, verticalMovement: Float, gridSize: Float): Int =
            getCollisionSteps(horizontalMovement, verticalMovement, gridSize)

        fun testProcessCollisionSteps(
            steps: Int,
            overallMovementX: Float,
            overallMovementY: Float,
            callback: (movementX: Float, movementY: Float) -> Boolean,
        ) = processCollisionSteps(steps, overallMovementX, overallMovementY, callback)

        fun testCalculateRatio(
            movement: Float,
            posInitialLeadingEdge: Float,
            posMovedLeadingEdge: Float,
            gridSize: Float,
        ): Float = calculateRatio(movement, posInitialLeadingEdge, posMovedLeadingEdge, gridSize)

        fun testProbeCollisionHorizontally(
            xMovement: Float,
            yMovement: Float,
            yPosTop: Float,
            yPosBottom: Float,
            xPosMovedLeadingEdge: Float,
            verticalSteps: Int,
            gridSize: Float,
            debugShapesComponent: DebugCollisionShapes? = null,
        ): Boolean = probeCollisionHorizontally(
            xMovement, yMovement, yPosTop, yPosBottom, xPosMovedLeadingEdge, verticalSteps, gridSize, debugShapesComponent
        )

        fun testProbeCollisionVertically(
            xMovement: Float,
            yMovement: Float,
            xPosMovedLeft: Float,
            xPosMovedRight: Float,
            yPosMovedLeadingEdge: Float,
            horizontalSteps: Int,
            gridSize: Float,
            debugShapesComponent: DebugCollisionShapes? = null,
        ): Boolean = probeCollisionVertically(
            xMovement, yMovement, xPosMovedLeft, xPosMovedRight, yPosMovedLeadingEdge, horizontalSteps, gridSize, debugShapesComponent
        )

        fun testResolveCollisionHorizontally(
            xMovement: Float,
            xPosMovedLeadingEdge: Float,
            positionComponent: Position,
            motionComponent: korlibs.korge.fleks.components.Motion,
            collisionComponent: korlibs.korge.fleks.components.Collision,
            gridSize: Float,
        ) = resolveCollisionHorizontally(
            xMovement, xPosMovedLeadingEdge, positionComponent, motionComponent, collisionComponent, gridSize
        )

        fun testResolveCollisionVertically(
            yMovement: Float,
            yPosMovedLeadingEdge: Float,
            positionComponent: Position,
            motionComponent: korlibs.korge.fleks.components.Motion,
            collisionComponent: korlibs.korge.fleks.components.Collision,
            gridSize: Float,
        ) = resolveCollisionVertically(
            yMovement, yPosMovedLeadingEdge, positionComponent, motionComponent, collisionComponent, gridSize
        )

        fun testCheckPlayfieldBoundaries(positionComponent: Position) =
            checkPlayfieldBoundaries(positionComponent)

        fun testCheckCollision(
            xMovement: Float,
            yMovement: Float,
            positionComponent: Position,
            motionComponent: korlibs.korge.fleks.components.Motion,
            collisionComponent: korlibs.korge.fleks.components.Collision,
            gridSize: Float,
            debugShapesComponent: DebugCollisionShapes? = null,
        ): Boolean = checkCollision(
            xMovement, yMovement, positionComponent, motionComponent, collisionComponent, gridSize, debugShapesComponent
        )
    }

    private lateinit var collisionMoveSystem: TestCollisionMoveSystem

    @Suppress("unused")
    private val world = configureWorld {
        injectables {
            assetStore.worldMapData.init(
                worldWidth = 512f,
                worldHeight = 512f,
                gridVaniaWidth = 4,
                gridVaniaHeight = 4,
                chunkWidth = 16,
                chunkHeight = 16,
                tileSize = 16,
                collisionTiles = emptyList(),
                collisionShapesBitmapSlice = Bitmap32(1, 1).slice()
            )
            add("AssetStore", assetStore)
        }
        systems {
            add(TestCollisionMoveSystem().also { collisionMoveSystem = it })
        }
    }

    // -------------------------------------------------------------------------
    // Tests for isNotAlmostZero()
    // -------------------------------------------------------------------------

    /**
     * Test: Verify that `Float.isNotAlmostZero()` correctly identifies float values outside the 0.01f guard threshold.
     *
     * Inputs:
     * - `0f`, `0.005f`, `-0.005f` (within 0.01f threshold)
     * - `0.02f`, `-0.02f` (exceeding 0.01f threshold)
     *
     * Expected Output:
     * - `false` for values <= 0.01f
     * - `true` for values > 0.01f
     */
    @Test
    fun testIsNotAlmostZero() {
        assertFalse(collisionMoveSystem.testIsNotAlmostZero(0f), "0f should be almost zero")
        assertFalse(collisionMoveSystem.testIsNotAlmostZero(0.005f), "0.005f <= guard (0.01f) should be almost zero")
        assertFalse(collisionMoveSystem.testIsNotAlmostZero(-0.005f), "-0.005f <= guard (0.01f) should be almost zero")
        assertTrue(collisionMoveSystem.testIsNotAlmostZero(0.02f), "0.02f > guard should not be almost zero")
        assertTrue(collisionMoveSystem.testIsNotAlmostZero(-0.02f), "-0.02f > guard should not be almost zero")
    }

    // -------------------------------------------------------------------------
    // Tests for getCollisionSteps()
    // -------------------------------------------------------------------------

    /**
     * Test: Verify that `getCollisionSteps()` returns 0 steps when there is no horizontal or vertical movement.
     *
     * Inputs:
     * - `horizontalMovement = 0f`, `verticalMovement = 0f`, `gridSize = 16f`
     *
     * Expected Output: `0`
     */
    @Test
    fun testZeroMovementReturnsZeroSteps() {
        val steps = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 0f,
            verticalMovement = 0f,
            gridSize = 16f,
        )
        assertEquals(0, steps, "Zero movement should result in 0 steps")
    }

    /**
     * Test: Verify that movement exactly equal to grid size results in 1 step.
     *
     * Inputs:
     * - `horizontalMovement = 16f`, `verticalMovement = 0f`, `gridSize = 16f`
     * - `horizontalMovement = 0f`, `verticalMovement = 16f`, `gridSize = 16f`
     *
     * Expected Output: `1` for both horizontal and vertical tests.
     */
    @Test
    fun testExactGridSizeMovementReturnsOneStep() {
        val stepsHorizontal = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 16f,
            verticalMovement = 0f,
            gridSize = 16f,
        )
        assertEquals(1, stepsHorizontal, "Movement equal to grid size horizontally should result in 1 step")

        val stepsVertical = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 0f,
            verticalMovement = 16f,
            gridSize = 16f,
        )
        assertEquals(1, stepsVertical, "Movement equal to grid size vertically should result in 1 step")
    }

    /**
     * Test: Verify that small or fractional non-zero movement below tile size rounds up to at least 1 step.
     *
     * Inputs:
     * - `horizontalMovement = 1f`, `verticalMovement = 0.5f`, `gridSize = 16f`
     * - `horizontalMovement = 0.001f`, `verticalMovement = 0f`, `gridSize = 16f`
     *
     * Expected Output: `1`
     */
    @Test
    fun testFractionalMovementBelowGridSizeRoundsUpToOneStep() {
        val stepsSmall = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 1f,
            verticalMovement = 0.5f,
            gridSize = 16f,
        )
        assertEquals(1, stepsSmall, "Movement less than grid size should round up to 1 step")

        val stepsTiny = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 0.001f,
            verticalMovement = 0f,
            gridSize = 16f,
        )
        assertEquals(1, stepsTiny, "Tiny non-zero movement should round up to 1 step")
    }

    /**
     * Test: Verify that movement exceeding tile size is rounded up to the next integer step count.
     *
     * Inputs:
     * - `horizontalMovement = 16.1f`, `verticalMovement = 0f`, `gridSize = 16f`
     * - `horizontalMovement = 32f`, `verticalMovement = 0f`, `gridSize = 16f`
     *
     * Expected Output: `2`
     */
    @Test
    fun testMovementExceedingGridSizeRoundsUpToNextInteger() {
        val stepsSlightlyOverOneTile = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 16.1f,
            verticalMovement = 0f,
            gridSize = 16f,
        )
        assertEquals(2, stepsSlightlyOverOneTile, "16.1px movement on 16px grid should round up to 2 steps")

        val stepsExactlyTwoTiles = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 32f,
            verticalMovement = 0f,
            gridSize = 16f,
        )
        assertEquals(2, stepsExactlyTwoTiles, "32px movement on 16px grid should be exactly 2 steps")
    }

    /**
     * Test: Verify that negative movement values use absolute magnitude when calculating steps.
     *
     * Inputs:
     * - `horizontalMovement = -16f`, `verticalMovement = 0f`, `gridSize = 16f`
     * - `horizontalMovement = -20f`, `verticalMovement = -10f`, `gridSize = 16f`
     *
     * Expected Output: `1` and `2` respectively.
     */
    @Test
    fun testNegativeMovementUsesAbsoluteValue() {
        val stepsNegativeHoriz = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = -16f,
            verticalMovement = 0f,
            gridSize = 16f,
        )
        assertEquals(1, stepsNegativeHoriz, "Negative horizontal movement should produce positive steps")

        val stepsNegativeVert = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 0f,
            verticalMovement = -32f,
            gridSize = 16f,
        )
        assertEquals(2, stepsNegativeVert, "Negative vertical movement should produce positive steps")

        val stepsBothNegative = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = -20f,
            verticalMovement = -10f,
            gridSize = 16f,
        )
        assertEquals(2, stepsBothNegative, "Max absolute magnitude (-20f -> 20f) over 16f grid size should round up to 2 steps")
    }

    /**
     * Test: Verify that step calculation uses the maximum displacement magnitude between horizontal and vertical axes.
     *
     * Inputs:
     * - `horizontalMovement = 5f`, `verticalMovement = 25f`, `gridSize = 16f` (vertical dominant)
     * - `horizontalMovement = 35f`, `verticalMovement = 10f`, `gridSize = 16f` (horizontal dominant)
     *
     * Expected Output: `2` for vertical dominant, `3` for horizontal dominant.
     */
    @Test
    fun testMaxOfHorizontalAndVerticalIsUsed() {
        val stepsVerticalDominant = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 5f,
            verticalMovement = 25f,
            gridSize = 16f,
        )
        assertEquals(2, stepsVerticalDominant, "Vertical movement (25f) is max, 25/16 = 1.5625 -> 2 steps")

        val stepsHorizontalDominant = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 35f,
            verticalMovement = 10f,
            gridSize = 16f,
        )
        assertEquals(3, stepsHorizontalDominant, "Horizontal movement (35f) is max, 35/16 = 2.1875 -> 3 steps")
    }

    /**
     * Test: Verify that `getCollisionSteps()` correctly scales step count based on custom grid sizes.
     *
     * Inputs:
     * - `gridSize = 8f` with `horizontalMovement = 16f`
     * - `gridSize = 32f` with `horizontalMovement = 16f`
     * - `gridSize = 1f` with `horizontalMovement = 3.2f`
     *
     * Expected Output: `2` for 8px grid, `1` for 32px grid, `4` for 1px grid.
     */
    @Test
    fun testVaryingGridSizes() {
        val stepsGrid8 = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 16f,
            verticalMovement = 0f,
            gridSize = 8f,
        )
        assertEquals(2, stepsGrid8, "16px movement on 8px grid should be 2 steps")

        val stepsGrid32 = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 16f,
            verticalMovement = 0f,
            gridSize = 32f,
        )
        assertEquals(1, stepsGrid32, "16px movement on 32px grid should round up to 1 step")

        val stepsGrid1 = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 3.2f,
            verticalMovement = 1f,
            gridSize = 1f,
        )
        assertEquals(4, stepsGrid1, "3.2px max movement on 1px grid should round up to 4 steps")
    }

    /**
     * Test: Verify that large high-speed movement deltas are properly split into proportionally large step counts.
     *
     * Inputs:
     * - `horizontalMovement = 160f`, `verticalMovement = 80f`, `gridSize = 16f`
     * - `horizontalMovement = 160.001f`, `verticalMovement = 80f`, `gridSize = 16f`
     *
     * Expected Output: `10` and `11` respectively.
     */
    @Test
    fun testLargeMovements() {
        val stepsLarge = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 160f,
            verticalMovement = 80f,
            gridSize = 16f,
        )
        assertEquals(10, stepsLarge, "160px movement on 16px grid should be 10 steps")

        val stepsLargeSlightlyOver = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 160.001f,
            verticalMovement = 80f,
            gridSize = 16f,
        )
        assertEquals(11, stepsLargeSlightlyOver, "160.001px movement on 16px grid should round up to 11 steps")
    }

    // -------------------------------------------------------------------------
    // Tests for processCollisionSteps()
    // -------------------------------------------------------------------------

    /**
     * Test: Verify that `processCollisionSteps()` does not execute callback when step count is <= 0.
     *
     * Inputs:
     * - `steps = 0` or `steps = -5`
     * - `overallMovementX = 10f`, `overallMovementY = 10f`
     *
     * Expected Output: Callback count = 0.
     */
    @Test
    fun testProcessCollisionStepsZeroOrNegativeStepsDoesNotInvokeCallback() {
        var callCount = 0
        collisionMoveSystem.testProcessCollisionSteps(0, 10f, 10f) { _, _ ->
            callCount++
            true
        }
        assertEquals(0, callCount, "Callback should not be invoked when steps is 0")

        callCount = 0
        collisionMoveSystem.testProcessCollisionSteps(-5, 10f, 10f) { _, _ ->
            callCount++
            true
        }
        assertEquals(0, callCount, "Callback should not be invoked when steps is negative")
    }

    /**
     * Test: Verify single step calculation where total displacement is passed directly to the callback.
     *
     * Inputs:
     * - `steps = 1`, `overallMovementX = 10f`, `overallMovementY = 5f`
     *
     * Expected Output: Callback invoked 1 time with `(10f, 5f)`.
     */
    @Test
    fun testProcessCollisionStepsSingleStepCalculation() {
        val invocations = mutableListOf<Pair<Float, Float>>()
        collisionMoveSystem.testProcessCollisionSteps(1, 10f, 5f) { moveX, moveY ->
            invocations.add(Pair(moveX, moveY))
            false
        }

        assertEquals(1, invocations.size, "Callback should be invoked exactly once for 1 step")
        assertEquals(10f, invocations[0].first, "movementX should equal overallMovementX / 1")
        assertEquals(5f, invocations[0].second, "movementY should equal overallMovementY / 1")
    }

    /**
     * Test: Verify multi-step calculation where total movement is divided equally among sub-step callbacks.
     *
     * Inputs:
     * - `steps = 2`, `overallMovementX = 20f`, `overallMovementY = -10f`
     *
     * Expected Output: Callback invoked twice, both with `(10f, -5f)`.
     */
    @Test
    fun testProcessCollisionStepsSplitsMovementEquallyAcrossSteps() {
        val invocations = mutableListOf<Pair<Float, Float>>()
        collisionMoveSystem.testProcessCollisionSteps(2, 20f, -10f) { moveX, moveY ->
            invocations.add(Pair(moveX, moveY))
            false
        }

        assertEquals(2, invocations.size, "Callback should be invoked twice for 2 steps")
        assertEquals(10f, invocations[0].first, "Step 1 movementX should be 20f / 2 = 10f")
        assertEquals(-5f, invocations[0].second, "Step 1 movementY should be -10f / 2 = -5f")
        assertEquals(10f, invocations[1].first, "Step 2 movementX should be 10f")
        assertEquals(-5f, invocations[1].second, "Step 2 movementY should be -5f")
    }

    /**
     * Test: Verify sub-step loop termination as soon as a step callback detects a collision and returns `true`.
     *
     * Inputs:
     * - `steps = 3`, `overallMovementX = 30f`, `overallMovementY = 15f`
     * - Callback returns `true` on iteration 1.
     *
     * Expected Output: Callback invoked 1 time only.
     */
    @Test
    fun testProcessCollisionStepsStopsImmediatelyOnCallbackTrue() {
        val invocations = mutableListOf<Pair<Float, Float>>()
        collisionMoveSystem.testProcessCollisionSteps(3, 30f, 15f) { moveX, moveY ->
            invocations.add(Pair(moveX, moveY))
            invocations.size == 1 // Return true on first step
        }

        assertEquals(1, invocations.size, "Callback should stop after first step when returning true")
    }

    // -------------------------------------------------------------------------
    // Tests for calculateRatio()
    // -------------------------------------------------------------------------

    /**
     * Test: Verify `calculateRatio()` for rightward movement towards a cell boundary.
     *
     * Inputs:
     * - `movement = 10f`, `posInitialLeadingEdge = 39f`, `posMovedLeadingEdge = 49f` (cell 3 at 48px), `gridSize = 16f`
     *
     * Expected Output: `0.9f` (`(3 * 16 - 39) / 10 = 9 / 10`)
     */
    @Test
    fun testCalculateRatioRightwardMovement() {
        val ratio = collisionMoveSystem.testCalculateRatio(
            movement = 10f,
            posInitialLeadingEdge = 39f,
            posMovedLeadingEdge = 49f,
            gridSize = 16f,
        )
        assertEquals(0.9f, ratio, 0.001f, "Ratio for 9px distance over 10px movement should be 0.9f")
    }

    /**
     * Test: Verify `calculateRatio()` for leftward movement towards a cell boundary.
     *
     * Inputs:
     * - `movement = -10f`, `posInitialLeadingEdge = 36f`, `posMovedLeadingEdge = 26f` (cell 1 ends at 32px), `gridSize = 16f`
     *
     * Expected Output: `0.4f` (`((1 + 1) * 16 - 36) / -10 = -4 / -10`)
     */
    @Test
    fun testCalculateRatioLeftwardMovement() {
        val ratio = collisionMoveSystem.testCalculateRatio(
            movement = -10f,
            posInitialLeadingEdge = 36f,
            posMovedLeadingEdge = 26f,
            gridSize = 16f,
        )
        assertEquals(0.4f, ratio, 0.001f, "Ratio for 4px distance over -10px movement should be 0.4f")
    }

    /**
     * Test: Verify `calculateRatio()` returns `0f` when movement is zero.
     *
     * Inputs:
     * - `movement = 0f`, `posInitialLeadingEdge = 30f`, `posMovedLeadingEdge = 30f`, `gridSize = 16f`
     *
     * Expected Output: `0f`
     */
    @Test
    fun testCalculateRatioZeroMovementReturnsZero() {
        val ratio = collisionMoveSystem.testCalculateRatio(
            movement = 0f,
            posInitialLeadingEdge = 30f,
            posMovedLeadingEdge = 30f,
            gridSize = 16f,
        )
        assertEquals(0f, ratio, "Zero movement should produce 0 ratio")
    }

    // -------------------------------------------------------------------------
    // Tests for probeCollisionHorizontally() & probeCollisionVertically()
    // -------------------------------------------------------------------------

    /**
     * Test: Verify `probeCollisionHorizontally()` returns `false` when xMovement is almost zero.
     *
     * Inputs: `xMovement = 0f`, `yMovement = 5f`, `yPosTop = 100f`, `yPosBottom = 129f`, `xPosMovedLeadingEdge = 50f`
     *
     * Expected Output: `false`
     */
    @Test
    fun testProbeCollisionHorizontallyZeroMovementReturnsFalse() {
        val result = collisionMoveSystem.testProbeCollisionHorizontally(
            xMovement = 0f,
            yMovement = 5f,
            yPosTop = 100f,
            yPosBottom = 129f,
            xPosMovedLeadingEdge = 50f,
            verticalSteps = 2,
            gridSize = 16f,
        )
        assertFalse(result, "probeCollisionHorizontally should return false when xMovement is zero")
    }

    /**
     * Test: Verify `probeCollisionVertically()` returns `false` when yMovement is almost zero.
     *
     * Inputs: `xMovement = 5f`, `yMovement = 0f`, `xPosMovedLeft = 100f`, `xPosMovedRight = 117f`, `yPosMovedLeadingEdge = 2785f`
     *
     * Expected Output: `false`
     */
    @Test
    fun testProbeCollisionVerticallyZeroMovementReturnsFalse() {
        val result = collisionMoveSystem.testProbeCollisionVertically(
            xMovement = 5f,
            yMovement = 0f,
            xPosMovedLeft = 100f,
            xPosMovedRight = 117f,
            yPosMovedLeadingEdge = 2785f,
            horizontalSteps = 1,
            gridSize = 16f,
        )
        assertFalse(result, "probeCollisionVertically should return false when yMovement is zero")
    }

    // -------------------------------------------------------------------------
    // Tests for resolveCollisionHorizontally()
    // -------------------------------------------------------------------------

    /**
     * Test: Verify horizontal collision resolution when moving right into a wall cell.
     *
     * Inputs:
     * - `xMovement = 10f`, `xPosMovedLeadingEdge = 49f` (cell 3 at 48px)
     * - `pos`: `x = 30f, y = 100f`
     * - `collision.rect`: `(-8, -28, 17f, 29f)`
     * - `gridSize = 16f`
     *
     * Expected Output:
     * - `velocityX = 0f`, `velocityY = 50f`
     * - `collision.right = true`
     * - `pos.x` snapped to `38.999f` (`48.0 - 0.001 - (17 + -8)`).
     */
    @Test
    fun testResolveCollisionHorizontallyMovingRight() {
        val pos = staticPositionComponent { x = 30f; y = 100f }
        val motion = staticMotionComponent { velocityX = 100f; velocityY = 50f; surfaceReactionY = 1f }
        val collision = staticCollisionComponent {
            rect = CollisionRect(-8, -28, 17f, 29f)
        }

        collisionMoveSystem.testResolveCollisionHorizontally(
            xMovement = 10f,
            xPosMovedLeadingEdge = 49f, // cell 3 starts at 48px
            positionComponent = pos,
            motionComponent = motion,
            collisionComponent = collision,
            gridSize = 16f,
        )

        assertEquals(0f, motion.velocityX, "velocityX should be zeroed out on horizontal collision")
        assertEquals(50f, motion.velocityY, "velocityY should remain unchanged when frictionY is 1f")
        assertTrue(collision.right, "collision.right should be set to true")
        // Position x = cxCell * 16 - 0.001 - (17 + -8) = 48 - 0.001 - 9 = 38.999f
        assertEquals(38.999f, pos.x, 0.001f, "Position X should snap left of cell 3")
    }

    /**
     * Test: Verify horizontal collision resolution when frictionY = 0f (stopping vertical motion).
     *
     * Inputs:
     * - `xMovement = 10f`, `xPosMovedLeadingEdge = 49f`
     * - `pos`: `x = 30f, y = 100f`
     * - `motion`: `velocityX = 100f, velocityY = 50f, frictionY = 0f`
     *
     * Expected Output:
     * - `velocityX = 0f`
     * - `velocityY = 0f` (stopped due to 0f frictionY)
     */
    @Test
    fun testResolveCollisionHorizontallyZeroFrictionStopsVerticalMotion() {
        val pos = staticPositionComponent { x = 30f; y = 100f }
        val motion = staticMotionComponent { velocityX = 100f; velocityY = 50f; surfaceReactionY = 0f }
        val collision = staticCollisionComponent {
            rect = CollisionRect(-8, -28, 17f, 29f)
        }

        collisionMoveSystem.testResolveCollisionHorizontally(
            xMovement = 10f,
            xPosMovedLeadingEdge = 49f,
            positionComponent = pos,
            motionComponent = motion,
            collisionComponent = collision,
            gridSize = 16f,
        )

        assertEquals(0f, motion.velocityX, "velocityX should be zeroed out")
        assertEquals(0f, motion.velocityY, "velocityY should be zeroed out when frictionY = 0f")
    }

    /**
     * Test: Verify horizontal collision resolution when moving left into a wall cell.
     *
     * Inputs:
     * - `xMovement = -10f`, `xPosMovedLeadingEdge = 31f` (cell 1 at 16..32px)
     * - `pos`: `x = 50f, y = 100f`
     * - `collision.rect`: `(-8, -28, 17f, 29f)`
     * - `gridSize = 16f`
     *
     * Expected Output:
     * - `velocityX = 0f`, `velocityY = 0f`
     * - `collision.left = true`
     * - `pos.x` snapped to `40.001f` (`(1 + 1) * 16 + 0.001 + 8`).
     */
    @Test
    fun testResolveCollisionHorizontallyMovingLeft() {
        val pos = staticPositionComponent { x = 50f; y = 100f }
        val motion = staticMotionComponent { velocityX = -100f; velocityY = 0f }
        val collision = staticCollisionComponent {
            rect = CollisionRect(-8, -28, 17f, 29f)
        }

        collisionMoveSystem.testResolveCollisionHorizontally(
            xMovement = -10f,
            xPosMovedLeadingEdge = 31f, // cell 1
            positionComponent = pos,
            motionComponent = motion,
            collisionComponent = collision,
            gridSize = 16f,
        )

        assertEquals(0f, motion.velocityX)
        assertEquals(0f, motion.velocityY)
        assertTrue(collision.left, "collision.left should be set to true")
        // Position x = (cxCell + 1) * 16 + 0.001 + (-(-8)) = 32 + 0.001 + 8 = 40.001f
        assertEquals(40.001f, pos.x, 0.001f, "Position X should snap right of cell 1")
    }

    // -------------------------------------------------------------------------
    // Tests for resolveCollisionVertically()
    // -------------------------------------------------------------------------

    /**
     * Test: Verify vertical collision resolution when landing on top of a ground cell.
     *
     * Inputs:
     * - `yMovement = 10f`, `yPosMovedLeadingEdge = 2785f` (cell 174 starts at 2784px)
     * - `pos`: `x = 100f, y = 2771f`
     * - `collision.rect`: `(-8, -28, 17f, 29f)`
     * - `gridSize = 16f`
     *
     * Expected Output:
     * - `velocityX = 0f`, `velocityY = 0f`
     * - `collision.isGrounded = true`
     * - `pos.y` snapped to `2782.999f` (`174 * 16 - 0.001 - (29 + -28)`).
     */
    @Test
    fun testResolveCollisionVerticallyMovingDown() {
        val pos = staticPositionComponent { x = 100f; y = 2771f }
        val motion = staticMotionComponent { velocityX = 0f; velocityY = 100f }
        val collision = staticCollisionComponent {
            rect = CollisionRect(-8, -28, 17f, 29f)
        }

        collisionMoveSystem.testResolveCollisionVertically(
            yMovement = 10f,
            yPosMovedLeadingEdge = 2785f, // cell 174
            positionComponent = pos,
            motionComponent = motion,
            collisionComponent = collision,
            gridSize = 16f,
        )

        assertEquals(0f, motion.velocityX)
        assertEquals(0f, motion.velocityY)
        assertTrue(collision.isGrounded, "collision.isGrounded should be set to true")
        // Position y = cyCell * 16 - 0.001 - (29 + -28) = 2784 - 0.001 - 1 = 2782.999f
        assertEquals(2782.999f, pos.y, 0.001f, "Position Y should snap on top of ground cell 174")
    }

    /**
     * Test: Verify vertical collision resolution when frictionX = 0f (stopping horizontal motion).
     *
     * Inputs:
     * - `yMovement = 10f`, `yPosMovedLeadingEdge = 2785f`
     * - `pos`: `x = 100f, y = 2771f`
     * - `motion`: `velocityX = 50f, velocityY = 100f, frictionX = 0f`
     *
     * Expected Output:
     * - `velocityY = 0f`
     * - `velocityX = 0f` (stopped due to 0f frictionX)
     */
    @Test
    fun testResolveCollisionVerticallyZeroFrictionStopsHorizontalMotion() {
        val pos = staticPositionComponent { x = 100f; y = 2771f }
        val motion = staticMotionComponent { velocityX = 50f; velocityY = 100f; surfaceReactionX = 0f }
        val collision = staticCollisionComponent {
            rect = CollisionRect(-8, -28, 17f, 29f)
        }

        collisionMoveSystem.testResolveCollisionVertically(
            yMovement = 10f,
            yPosMovedLeadingEdge = 2785f,
            positionComponent = pos,
            motionComponent = motion,
            collisionComponent = collision,
            gridSize = 16f,
        )

        assertEquals(0f, motion.velocityY, "velocityY should be zeroed out")
        assertEquals(0f, motion.velocityX, "velocityX should be zeroed out when frictionX = 0f")
    }

    /**
     * Test: Verify vertical collision resolution when hitting a ceiling cell above.
     *
     * Inputs:
     * - `yMovement = -10f`, `yPosMovedLeadingEdge = 40f` (cell 2 at 32px)
     * - `pos`: `x = 100f, y = 100f`
     * - `collision.rect`: `(-8, -28, 17f, 29f)`
     * - `gridSize = 16f`
     *
     * Expected Output:
     * - `velocityX = 0f`, `velocityY = 0f`
     * - `collision.isCollidingAbove = true`
     * - `pos.y` snapped to `76.001f` (`(2 + 1) * 16 + 0.001 + 28`).
     */
    @Test
    fun testResolveCollisionVerticallyMovingUp() {
        val pos = staticPositionComponent { x = 100f; y = 100f }
        val motion = staticMotionComponent { velocityX = 0f; velocityY = -100f }
        val collision = staticCollisionComponent {
            rect = CollisionRect(-8, -28, 17f, 29f)
        }

        collisionMoveSystem.testResolveCollisionVertically(
            yMovement = -10f,
            yPosMovedLeadingEdge = 40f, // cell 2
            positionComponent = pos,
            motionComponent = motion,
            collisionComponent = collision,
            gridSize = 16f,
        )

        assertEquals(0f, motion.velocityX)
        assertEquals(0f, motion.velocityY)
        assertTrue(collision.isCollidingAbove, "collision.isCollidingAbove should be set to true")
        // Position y = (2 + 1) * 16 + 0.001 + (-(-28)) = 48 + 0.001 + 28 = 76.001f
        assertEquals(76.001f, pos.y, 0.001f, "Position Y should snap below ceiling cell 2")
    }

    // -------------------------------------------------------------------------
    // Tests for checkPlayfieldBoundaries()
    // -------------------------------------------------------------------------

    /**
     * Test: Verify playfield boundary clamping when entity position exceeds left boundary guard border.
     *
     * Inputs: `pos`: `x = 5f, offsetX = 0f`, guardBorder = `16f`
     *
     * Expected Output: `pos.x` clamped to `16.0f`.
     */
    @Test
    fun testCheckPlayfieldBoundariesLeftBoundaryClamping() {
        val pos = staticPositionComponent { x = 5f; offsetX = 0f }
        collisionMoveSystem.testCheckPlayfieldBoundaries(pos)

        // Guard border = 16f. x + offsetX <= 16f -> x = 16f - offsetX = 16f
        assertEquals(16f, pos.x, "Position X should be clamped to left guard border (16f)")
    }

    /**
     * Test: Verify that playfield boundary checking leaves positions inside valid boundaries untouched.
     *
     * Inputs: `pos`: `x = 100f, y = 100f, offsetX = 0f, offsetY = 0f`
     *
     * Expected Output: `pos.x = 100f`, `pos.y = 100f`.
     */
    @Test
    fun testCheckPlayfieldBoundariesNormalPositionUnchanged() {
        val pos = staticPositionComponent { x = 100f; y = 100f; offsetX = 0f; offsetY = 0f }
        collisionMoveSystem.testCheckPlayfieldBoundaries(pos)

        assertEquals(100f, pos.x, "Position X inside level should remain unchanged")
        assertEquals(100f, pos.y, "Position Y inside level should remain unchanged")
    }

    // -------------------------------------------------------------------------
    // Tests for checkCollision()
    // -------------------------------------------------------------------------

    /**
     * Test: Verify that `checkCollision()` moves entity directly when no collision is detected.
     *
     * Inputs:
     * - `xMovement = 10f`, `yMovement = 5f`
     * - `pos`: `x = 100f, y = 100f`
     *
     * Expected Output:
     * - Returns `false`.
     * - `pos.x` increased by `10f` (110f), `pos.y` increased by `5f` (105f).
     */
    @Test
    fun testCheckCollisionNoCollision() {
        val pos = staticPositionComponent { x = 100f; y = 100f }
        val motion = staticMotionComponent { velocityX = 100f; velocityY = 50f }
        val collision = staticCollisionComponent {
            rect = CollisionRect(-8, -28, 17f, 29f)
        }

        val isColliding = collisionMoveSystem.testCheckCollision(
            xMovement = 10f,
            yMovement = 5f,
            positionComponent = pos,
            motionComponent = motion,
            collisionComponent = collision,
            gridSize = 16f,
        )

        assertFalse(isColliding, "checkCollision should return false when no collision is detected")
        assertEquals(110f, pos.x, 0.001f, "Position X should be moved by xMovement")
        assertEquals(105f, pos.y, 0.001f, "Position Y should be moved by yMovement")
    }
}
