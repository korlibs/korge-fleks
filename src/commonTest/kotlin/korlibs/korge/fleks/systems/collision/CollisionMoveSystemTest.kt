package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.configureWorld
import korlibs.korge.fleks.components.assetStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CollisionMoveSystemTest {

    // Subclass CollisionMoveSystem to expose protected methods for testing
    private class TestCollisionMoveSystem : CollisionMoveSystem() {
        public fun testGetCollisionSteps(horizontalMovement: Float, verticalMovement: Float, gridSize: Float): Int =
            getCollisionSteps(horizontalMovement, verticalMovement, gridSize)

        public fun testProcessCollisionSteps(
            steps: Int,
            overallMovementX: Float,
            overallMovementY: Float,
            callback: (movementX: Float, movementY: Float) -> Boolean
        ): Boolean = processCollisionSteps(steps, overallMovementX, overallMovementY, callback)
    }

    private lateinit var collisionMoveSystem: TestCollisionMoveSystem

    @Suppress("unused")
    private val world = configureWorld {
        injectables {
            add("AssetStore", assetStore)
        }
        systems {
            add(TestCollisionMoveSystem().also { collisionMoveSystem = it })
        }
    }

    // -------------------------------------------------------------------------
    // Tests for getCollisionSteps()
    // -------------------------------------------------------------------------

    @Test
    fun testZeroMovementReturnsZeroSteps() {
        val steps = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 0f,
            verticalMovement = 0f,
            gridSize = 16f
        )
        assertEquals(0, steps, "Zero movement should result in 0 steps")
    }

    @Test
    fun testExactGridSizeMovementReturnsOneStep() {
        val stepsHorizontal = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 16f,
            verticalMovement = 0f,
            gridSize = 16f
        )
        assertEquals(1, stepsHorizontal, "Movement equal to grid size horizontally should result in 1 step")

        val stepsVertical = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 0f,
            verticalMovement = 16f,
            gridSize = 16f
        )
        assertEquals(1, stepsVertical, "Movement equal to grid size vertically should result in 1 step")
    }

    @Test
    fun testFractionalMovementBelowGridSizeRoundsUpToOneStep() {
        val stepsSmall = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 1f,
            verticalMovement = 0.5f,
            gridSize = 16f
        )
        assertEquals(1, stepsSmall, "Movement less than grid size should round up to 1 step")

        val stepsTiny = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 0.001f,
            verticalMovement = 0f,
            gridSize = 16f
        )
        assertEquals(1, stepsTiny, "Tiny non-zero movement should round up to 1 step")
    }

    @Test
    fun testMovementExceedingGridSizeRoundsUpToNextInteger() {
        val stepsSlightlyOverOneTile = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 16.1f,
            verticalMovement = 0f,
            gridSize = 16f
        )
        assertEquals(2, stepsSlightlyOverOneTile, "16.1px movement on 16px grid should round up to 2 steps")

        val stepsExactlyTwoTiles = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 32f,
            verticalMovement = 0f,
            gridSize = 16f
        )
        assertEquals(2, stepsExactlyTwoTiles, "32px movement on 16px grid should be exactly 2 steps")
    }

    @Test
    fun testNegativeMovementUsesAbsoluteValue() {
        val stepsNegativeHoriz = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = -16f,
            verticalMovement = 0f,
            gridSize = 16f
        )
        assertEquals(1, stepsNegativeHoriz, "Negative horizontal movement should produce positive steps")

        val stepsNegativeVert = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 0f,
            verticalMovement = -32f,
            gridSize = 16f
        )
        assertEquals(2, stepsNegativeVert, "Negative vertical movement should produce positive steps")

        val stepsBothNegative = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = -20f,
            verticalMovement = -10f,
            gridSize = 16f
        )
        assertEquals(2, stepsBothNegative, "Max absolute magnitude (-20f -> 20f) over 16f grid size should round up to 2 steps")
    }

    @Test
    fun testMaxOfHorizontalAndVerticalIsUsed() {
        val stepsVerticalDominant = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 5f,
            verticalMovement = 25f,
            gridSize = 16f
        )
        assertEquals(2, stepsVerticalDominant, "Vertical movement (25f) is max, 25/16 = 1.5625 -> 2 steps")

        val stepsHorizontalDominant = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 35f,
            verticalMovement = 10f,
            gridSize = 16f
        )
        assertEquals(3, stepsHorizontalDominant, "Horizontal movement (35f) is max, 35/16 = 2.1875 -> 3 steps")
    }

    @Test
    fun testVaryingGridSizes() {
        val stepsGrid8 = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 16f,
            verticalMovement = 0f,
            gridSize = 8f
        )
        assertEquals(2, stepsGrid8, "16px movement on 8px grid should be 2 steps")

        val stepsGrid32 = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 16f,
            verticalMovement = 0f,
            gridSize = 32f
        )
        assertEquals(1, stepsGrid32, "16px movement on 32px grid should round up to 1 step")

        val stepsGrid1 = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 3.2f,
            verticalMovement = 1f,
            gridSize = 1f
        )
        assertEquals(4, stepsGrid1, "3.2px max movement on 1px grid should round up to 4 steps")
    }

    @Test
    fun testLargeMovements() {
        val stepsLarge = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 160f,
            verticalMovement = 80f,
            gridSize = 16f
        )
        assertEquals(10, stepsLarge, "160px movement on 16px grid should be 10 steps")

        val stepsLargeSlightlyOver = collisionMoveSystem.testGetCollisionSteps(
            horizontalMovement = 160.001f,
            verticalMovement = 80f,
            gridSize = 16f
        )
        assertEquals(11, stepsLargeSlightlyOver, "160.001px movement on 16px grid should round up to 11 steps")
    }

    // -------------------------------------------------------------------------
    // Tests for processCollisionSteps()
    // -------------------------------------------------------------------------

    @Test
    fun testProcessCollisionStepsZeroOrNegativeStepsDoesNotInvokeCallback() {
        var callCount = 0
        val resultZero = collisionMoveSystem.testProcessCollisionSteps(0, 10f, 10f) { _, _ ->
            callCount++
            true
        }
        assertEquals(0, callCount, "Callback should not be invoked when steps is 0")
        assertFalse(resultZero, "processCollisionSteps should return false when steps is 0")

        callCount = 0
        val resultNeg = collisionMoveSystem.testProcessCollisionSteps(-5, 10f, 10f) { _, _ ->
            callCount++
            true
        }
        assertEquals(0, callCount, "Callback should not be invoked when steps is negative")
        assertFalse(resultNeg, "processCollisionSteps should return false when steps is negative")
    }

    @Test
    fun testProcessCollisionStepsSingleStepCalculation() {
        val invocations = mutableListOf<Pair<Float, Float>>()
        val result = collisionMoveSystem.testProcessCollisionSteps(1, 10f, 5f) { moveX, moveY ->
            invocations.add(Pair(moveX, moveY))
            false
        }

        assertFalse(result, "Should return false when callback returns false")
        assertEquals(1, invocations.size, "Callback should be invoked exactly once for 1 step")
        assertEquals(10f, invocations[0].first, "movementX should equal overallMovementX / 1")
        assertEquals(5f, invocations[0].second, "movementY should equal overallMovementY / 1")
    }

    @Test
    fun testProcessCollisionStepsSplitsMovementEquallyAcrossSteps() {
        val invocations = mutableListOf<Pair<Float, Float>>()
        val result = collisionMoveSystem.testProcessCollisionSteps(2, 20f, -10f) { moveX, moveY ->
            invocations.add(Pair(moveX, moveY))
            false
        }

        assertFalse(result)
        assertEquals(2, invocations.size, "Callback should be invoked twice for 2 steps")
        assertEquals(10f, invocations[0].first, "Step 1 movementX should be 20f / 2 = 10f")
        assertEquals(-5f, invocations[0].second, "Step 1 movementY should be -10f / 2 = -5f")
        assertEquals(10f, invocations[1].first, "Step 2 movementX should be 10f")
        assertEquals(-5f, invocations[1].second, "Step 2 movementY should be -5f")
    }

    @Test
    fun testProcessCollisionStepsStopsImmediatelyOnCallbackTrue() {
        val invocations = mutableListOf<Pair<Float, Float>>()
        val result = collisionMoveSystem.testProcessCollisionSteps(3, 30f, 15f) { moveX, moveY ->
            invocations.add(Pair(moveX, moveY))
            invocations.size == 1 // Return true on first step
        }

        assertTrue(result, "processCollisionSteps should return true when callback returns true")
        assertEquals(1, invocations.size, "Callback should stop after first step when returning true")
    }
}
