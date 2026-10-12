package korlibs.korge.fleks.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class CollisionUtilsTest {

    // -------------------------------------------------------------------------
    // Tests for getCollisionSteps()
    // -------------------------------------------------------------------------

    @Test
    fun testZeroMovementReturnsZeroSteps() {
        val steps = CollisionUtils.getCollisionSteps(
            horizontalMovement = 0f,
            verticalMovement = 0f,
            gridSize = 16f
        )
        assertEquals(0, steps, "Zero movement should result in 0 steps")
    }

    @Test
    fun testExactGridSizeMovementReturnsOneStep() {
        val stepsHorizontal = CollisionUtils.getCollisionSteps(
            horizontalMovement = 16f,
            verticalMovement = 0f,
            gridSize = 16f
        )
        assertEquals(1, stepsHorizontal, "Movement equal to grid size horizontally should result in 1 step")

        val stepsVertical = CollisionUtils.getCollisionSteps(
            horizontalMovement = 0f,
            verticalMovement = 16f,
            gridSize = 16f
        )
        assertEquals(1, stepsVertical, "Movement equal to grid size vertically should result in 1 step")
    }

    @Test
    fun testFractionalMovementBelowGridSizeRoundsUpToOneStep() {
        val stepsSmall = CollisionUtils.getCollisionSteps(
            horizontalMovement = 1f,
            verticalMovement = 0.5f,
            gridSize = 16f
        )
        assertEquals(1, stepsSmall, "Movement less than grid size should round up to 1 step")

        val stepsTiny = CollisionUtils.getCollisionSteps(
            horizontalMovement = 0.001f,
            verticalMovement = 0f,
            gridSize = 16f
        )
        assertEquals(1, stepsTiny, "Tiny non-zero movement should round up to 1 step")
    }

    @Test
    fun testMovementExceedingGridSizeRoundsUpToNextInteger() {
        val stepsSlightlyOverOneTile = CollisionUtils.getCollisionSteps(
            horizontalMovement = 16.1f,
            verticalMovement = 0f,
            gridSize = 16f
        )
        assertEquals(2, stepsSlightlyOverOneTile, "16.1px movement on 16px grid should round up to 2 steps")

        val stepsExactlyTwoTiles = CollisionUtils.getCollisionSteps(
            horizontalMovement = 32f,
            verticalMovement = 0f,
            gridSize = 16f
        )
        assertEquals(2, stepsExactlyTwoTiles, "32px movement on 16px grid should be exactly 2 steps")
    }

    @Test
    fun testNegativeMovementUsesAbsoluteValue() {
        val stepsNegativeHoriz = CollisionUtils.getCollisionSteps(
            horizontalMovement = -16f,
            verticalMovement = 0f,
            gridSize = 16f
        )
        assertEquals(1, stepsNegativeHoriz, "Negative horizontal movement should produce positive steps")

        val stepsNegativeVert = CollisionUtils.getCollisionSteps(
            horizontalMovement = 0f,
            verticalMovement = -32f,
            gridSize = 16f
        )
        assertEquals(2, stepsNegativeVert, "Negative vertical movement should produce positive steps")

        val stepsBothNegative = CollisionUtils.getCollisionSteps(
            horizontalMovement = -20f,
            verticalMovement = -10f,
            gridSize = 16f
        )
        assertEquals(2, stepsBothNegative, "Max absolute magnitude (-20f -> 20f) over 16f grid size should round up to 2 steps")
    }

    @Test
    fun testMaxOfHorizontalAndVerticalIsUsed() {
        val stepsVerticalDominant = CollisionUtils.getCollisionSteps(
            horizontalMovement = 5f,
            verticalMovement = 25f,
            gridSize = 16f
        )
        assertEquals(2, stepsVerticalDominant, "Vertical movement (25f) is max, 25/16 = 1.5625 -> 2 steps")

        val stepsHorizontalDominant = CollisionUtils.getCollisionSteps(
            horizontalMovement = 35f,
            verticalMovement = 10f,
            gridSize = 16f
        )
        assertEquals(3, stepsHorizontalDominant, "Horizontal movement (35f) is max, 35/16 = 2.1875 -> 3 steps")
    }

    @Test
    fun testVaryingGridSizes() {
        val stepsGrid8 = CollisionUtils.getCollisionSteps(
            horizontalMovement = 16f,
            verticalMovement = 0f,
            gridSize = 8f
        )
        assertEquals(2, stepsGrid8, "16px movement on 8px grid should be 2 steps")

        val stepsGrid32 = CollisionUtils.getCollisionSteps(
            horizontalMovement = 16f,
            verticalMovement = 0f,
            gridSize = 32f
        )
        assertEquals(1, stepsGrid32, "16px movement on 32px grid should round up to 1 step")

        val stepsGrid1 = CollisionUtils.getCollisionSteps(
            horizontalMovement = 3.2f,
            verticalMovement = 1f,
            gridSize = 1f
        )
        assertEquals(4, stepsGrid1, "3.2px max movement on 1px grid should round up to 4 steps")
    }

    @Test
    fun testLargeMovements() {
        val stepsLarge = CollisionUtils.getCollisionSteps(
            horizontalMovement = 160f,
            verticalMovement = 80f,
            gridSize = 16f
        )
        assertEquals(10, stepsLarge, "160px movement on 16px grid should be 10 steps")

        val stepsLargeSlightlyOver = CollisionUtils.getCollisionSteps(
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
        CollisionUtils.processCollisionSteps(0, 10f, 10f) { _, _ ->
            callCount++
            true
        }
        assertEquals(0, callCount, "Callback should not be invoked when steps is 0")

        callCount = 0
        CollisionUtils.processCollisionSteps(-5, 10f, 10f) { _, _ ->
            callCount++
            true
        }
        assertEquals(0, callCount, "Callback should not be invoked when steps is negative")
    }

    @Test
    fun testProcessCollisionStepsSingleStepWithinMaxMovement() {
        val invocations = mutableListOf<Pair<Float, Float>>()
        CollisionUtils.processCollisionSteps(1, 10f, 5f) { moveX, moveY ->
            invocations.add(Pair(moveX, moveY))
        }

        assertEquals(1, invocations.size, "Callback should be invoked exactly once for 1 step")
        assertEquals(10f, invocations[0].first, "movementX should equal overallMovementX when <= 16f")
        assertEquals(5f, invocations[0].second, "movementY should equal overallMovementY when <= 16f")
    }

    @Test
    fun testProcessCollisionStepsClampsInitialMovementPositiveAndNegative() {
        val invocations = mutableListOf<Pair<Float, Float>>()
        // overallMovementX = 25f (> 16f) -> clamped to min(25f, 16f) = 16f
        // overallMovementY = -20f (< -16f) -> clamped to max(-20f, -16f) = -16f
        CollisionUtils.processCollisionSteps(1, 25f, -20f) { moveX, moveY ->
            invocations.add(Pair(moveX, moveY))
        }

        assertEquals(1, invocations.size)
        assertEquals(16f, invocations[0].first, "Positive movementX > 16f should be clamped to 16f")
        assertEquals(-16f, invocations[0].second, "Negative movementY < -16f should be clamped to -16f")
    }

    @Test
    fun testProcessCollisionStepsMultipleStepsInvocations() {
        val invocations = mutableListOf<Pair<Float, Float>>()
        CollisionUtils.processCollisionSteps(3, 16f, 8f) { moveX, moveY ->
            invocations.add(Pair(moveX, moveY))
        }

        assertEquals(3, invocations.size, "Callback should be invoked 3 times for 3 steps")
        // First step
        assertEquals(16f, invocations[0].first)
        assertEquals(8f, invocations[0].second)
        // Second step (movement decremented by 16f, 16-16=0, 8-16<=0 -> 0)
        assertEquals(0f, invocations[1].first)
        assertEquals(0f, invocations[1].second)
        // Third step
        assertEquals(0f, invocations[2].first)
        assertEquals(0f, invocations[2].second)
    }

    @Test
    fun testProcessCollisionStepsNegativeMovementsWithinBounds() {
        val invocations = mutableListOf<Pair<Float, Float>>()
        CollisionUtils.processCollisionSteps(1, -10f, -5f) { moveX, moveY ->
            invocations.add(Pair(moveX, moveY))
        }

        assertEquals(1, invocations.size)
        assertEquals(-10f, invocations[0].first, "Negative overallMovementX >= -16f should remain -10f")
        assertEquals(-5f, invocations[0].second, "Negative overallMovementY >= -16f should remain -5f")
    }
}
