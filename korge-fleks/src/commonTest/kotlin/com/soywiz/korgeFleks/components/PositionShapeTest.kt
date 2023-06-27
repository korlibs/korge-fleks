package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import korlibs.korge.fleks.components.PositionShape
import korlibs.korge.fleks.utils.Invokables
import kotlin.test.Test
import kotlin.test.assertEquals


internal class PositionShapeTest {

    private val expectedWorld = configureWorld {}
    private val recreatedWorld = configureWorld {}

    @Test
    fun testPositionShapeSerialization() {

        val compUnderTest = PositionShape(
            x = 5.2f,
            y = 42.1f,
        )

        val entity = expectedWorld.entity {
            it += compUnderTest
        }

        CommonTestEnv.serializeDeserialize(expectedWorld, recreatedWorld)

        // get the component from entity with the same id from the new created world
        val newEntity = recreatedWorld.asEntityBag()[entity.id]
        val newCompUnderTest = with (recreatedWorld) { newEntity[PositionShape] }

        assertEquals(compUnderTest.x, newCompUnderTest.x, "Check 'x' property to be equal")
        assertEquals(compUnderTest.y, newCompUnderTest.y, "Check 'y' property to be equal")
    }
}