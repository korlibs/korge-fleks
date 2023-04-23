package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.world
import korlibs.korge.fleks.utils.InvokableSerializer
import korlibs.korge.fleks.components.PositionShape
import kotlin.test.Test
import kotlin.test.assertEquals


internal class PositionShapeTest {

    private val expectedWorld = world {}
    private val recreatedWorld = world {}

    @Test
    fun testPositionShapeSerialization() {

        InvokableSerializer.register(World::testFunction)

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