package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.world
import com.soywiz.korgeFleks.utils.InvokableSerializer
import kotlin.test.Test
import kotlin.test.assertEquals


internal class PositionShapeTest {

    private val expectedWorld = world {}
    private val recreatedWorld = world {}

    @Test
    fun testPositionShapeSerialization() {

        InvokableSerializer.register(World::testFunction)

        val compUnderTest = PositionShape(
            x = 5.2,
            y = 42.1,
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