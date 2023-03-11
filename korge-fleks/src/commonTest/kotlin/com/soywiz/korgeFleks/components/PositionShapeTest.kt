package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.world
import com.soywiz.korgeFleks.entity.config.Config
import com.soywiz.korgeFleks.utils.InvokableSerializer
import com.soywiz.korma.interpolation.Easing
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.test.Test


internal class PositionShapeTest {

    private val expectedWorld = world {}
    private val recreatedWorld = world {}

    @Test
    fun testPositionShapeSerialization() {

        InvokableSerializer.register(World::testFunction)
        CommonTestEnv.snapshotSerializer.register(
            SerializersModule {
                polymorphic(Config::class) {
                    // List here all test specific config classes
                    subclass(TestConfig::class)
                }
            }
        )

        val compUnderTest = PositionShape(
            x = 5.2,
            y = 42.1,
            entity = Entity(42),
            string = "hello world!",
            notNullString = "hehe",
            entities = mutableMapOf("one" to Entity(3), "two" to Entity(1001)),
            componentProperty = AnimateComponentType.LifeCycleHealthCounter,
            changeDouble = 42.42,
            changeString = "changeString",
            changeRgb = Rgb(0x12, 0x34,0x56),
            easing = Easing.EASE_OUT, // Easing(Easing.EASE_OUT),
            config = TestConfig(
                id = 1234
            ),
            lambdaFunction = World::testFunction
        )

        val entity = expectedWorld.entity {
            it += compUnderTest
        }

//        with (expectedWorld) {
//            entity[PositionShape].lambdaFunction.invoke(Entity(4242), TestConfig())
//        }

        CommonTestEnv.serializeDeserialize(expectedWorld, recreatedWorld)

        // get the component from entity with the same id from the new created world
        val newEntity = recreatedWorld.asEntityBag()[entity.id]
        val newCompUnderTest = with (recreatedWorld) { newEntity[PositionShape] }

//        assertEquals(compUnderTest.index, newCompUnderTest.index, "Check 'index' property to be equal")

        newCompUnderTest.lambdaFunction.invoke(recreatedWorld, Entity(0), TestConfig(4243))

    }
}