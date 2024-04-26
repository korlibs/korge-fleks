package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.RgbaComponent.Rgb
import korlibs.korge.fleks.components.TweenPropertyComponent.*
import korlibs.korge.fleks.components.TweenPropertyComponent.TweenProperty.*
import korlibs.math.interpolation.Easing
import kotlin.test.*

internal class AnimateComponentTest {

    private val expectedWorld = configureWorld {}
    private val recreatedWorld = configureWorld {}

    @Test
    fun testAnimateComponentTypeIntegrity() {
        val testVector: List<Pair<TweenProperty, ComponentType<*>>> = listOf(
            Pair(SpriteIsPlaying, Companion.TweenSpriteIsPlaying),
            Pair(SpriteForwardDirection, Companion.TweenSpriteForwardDirection),
            Pair(SpriteLoop, Companion.TweenSpriteLoop),
            Pair(SpriteDestroyOnPlayingFinished, Companion.TweenSpriteDestroyOnPlayingFinished),
            Pair(SpriteAnimName, Companion.TweenSpriteAnimName),
            Pair(RgbaAlpha, Companion.TweenRgbaAlphaComponent),
            Pair(RgbaTint, Companion.TweenRgbaTint),
            Pair(AppearanceVisible, Companion.TweenAppearanceVisible),
            Pair(SpawnerNumberOfObjects, Companion.TweenSpawnerNumberOfObjects),
            Pair(SpawnerInterval, Companion.TweenSpawnerInterval),
            Pair(SpawnerTimeVariation, Companion.TweenSpawnerTimeVariation),
            Pair(SpawnerPositionVariation, Companion.TweenSpawnerPositionVariation),
            Pair(LifeCycleHealthCounter, Companion.TweenLifeCycleHealthCounter),
            Pair(PositionX, Companion.TweenPositionXComponent),
            Pair(PositionY, Companion.TweenPositionYComponent),
            Pair(OffsetX, Companion.TweenOffsetX),
            Pair(OffsetY, Companion.TweenOffsetY),
            Pair(LayoutCenterX, Companion.TweenLayoutCenterX),
            Pair(LayoutCenterY, Companion.TweenLayoutCenterY),
            Pair(LayoutOffsetX, Companion.TweenLayoutOffsetX),
            Pair(LayoutOffsetY, Companion.TweenLayoutOffsetY),
            Pair(SwitchLayerVisibilityOnVariance, Companion.TweenSwitchLayerVisibilityOnVariance),
            Pair(SwitchLayerVisibilityOffVariance, Companion.TweenSwitchLayerVisibilityOffVariance),
            Pair(SoundStartTrigger, Companion.TweenSoundStartTrigger),
            Pair(SoundStopTrigger, Companion.TweenSoundStopTrigger),
            Pair(SoundPosition, Companion.TweenSoundPosition),
            Pair(SoundVolume, Companion.TweenSoundVolume),
            Pair(ConfigureFunction, Companion.ExecuteConfigureFunction)
        )

        testVector.forEach { animateType ->
            assertEquals(
                animateType.first.type,
                animateType.second,
                "Check if AnimateComponentType setup is correct: '${animateType.first.type}' - '${animateType.second}'")
        }

        assertEquals(
            TweenProperty.values().size,
            testVector.size,
            "Check if all AnimateComponentType enum values have been tested"
        )
    }

    @Test
    fun testAnimateComponentSerialization() {

        val componentUnderTest = TweenPropertyComponent(
            property = RgbaAlpha,
            change = Unit,
            value = Unit,
            duration = 1.2f,
            timeProgress = 3.4f,
            easing = Easing.EASE_IN
        )

        val entity = expectedWorld.entity {
            it += componentUnderTest
        }

        runAnimateComponentSerializationTest(entity, componentUnderTest, change = 42.43, value = 45.46)  // Double test
        runAnimateComponentSerializationTest(entity, componentUnderTest, change = 42, value = 43)  // Int test
        runAnimateComponentSerializationTest(entity, componentUnderTest, change = true, value = false)  // Boolean test
        runAnimateComponentSerializationTest(entity, componentUnderTest, change = "testString", value = "anotherString")  // String test
        runAnimateComponentSerializationTest(entity, componentUnderTest, change = Rgb.MIDDLE_GREY, value = Rgb(12, 34, 56))  // Rgb test
    }

    private fun <T> runAnimateComponentSerializationTest(
        entity: Entity, component: TweenPropertyComponent,
        change: T, value: T
    ) {
        component.change = change as Any
        component.value = value as Any

        CommonTestEnv.serializeDeserialize(expectedWorld, recreatedWorld)

        // get the component from entity with the same id from the new created world
        val recreatedTestComponent = with (recreatedWorld) { recreatedWorld.asEntityBag()[entity.id][Companion.TweenRgbaAlphaComponent] }

        assertEquals(component.change, recreatedTestComponent.change, "Check 'change' property to be equal")
        assertEquals(component.value, recreatedTestComponent.value, "Check 'value' property to be equal")
    }
}
