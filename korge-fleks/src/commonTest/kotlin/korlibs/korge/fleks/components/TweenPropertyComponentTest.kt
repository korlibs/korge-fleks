package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.RgbaComponent.Rgb
import korlibs.korge.fleks.components.TweenPropertyComponent.*
import korlibs.korge.fleks.components.TweenPropertyComponent.TweenProperty.*
import korlibs.math.interpolation.Easing
import kotlin.test.*

internal class TweenPropertyComponentTest {

    private val expectedWorld = configureWorld {}
    private val recreatedWorld = configureWorld {}

    @Test
    fun testTweenPropertyComponentTypeIntegrity() {
        val testVector: List<Pair<TweenProperty, ComponentType<*>>> = listOf(
            Pair(PositionOffsetX, Companion.TweenPositionOffsetXComponent),
            Pair(PositionOffsetY, Companion.TweenPositionOffsetYComponent),
            Pair(PositionX, Companion.TweenPositionXComponent),
            Pair(PositionY, Companion.TweenPositionYComponent),
            Pair(MotionVelocityX, Companion.TweenMotionVelocityXComponent),
            Pair(RgbaAlpha, Companion.TweenRgbaAlphaComponent),
            Pair(RgbaTint, Companion.TweenRgbaTintComponent),
            Pair(SpawnerInterval, Companion.TweenSpawnerIntervalComponent),
            Pair(SpawnerNumberOfObjects, Companion.TweenSpawnerNumberOfObjectsComponent),
            Pair(SpawnerPositionVariation, Companion.TweenSpawnerPositionVariationComponent),
            Pair(SpawnerTimeVariation, Companion.TweenSpawnerTimeVariationComponent),
            Pair(SpriteRunning, Companion.TweenSpriteRunningComponent),
            Pair(SpriteDirection, Companion.TweenSpriteDirectionComponent),
            Pair(SpriteDestroyOnPlayingFinished, Companion.TweenSpriteDestroyOnPlayingFinishedComponent),
            Pair(SpriteAnimation, Companion.TweenSpriteAnimationComponent),
            Pair(SwitchLayerVisibilityOnVariance, Companion.TweenSwitchLayerVisibilityOnVarianceComponent),
            Pair(SwitchLayerVisibilityOffVariance, Companion.TweenSwitchLayerVisibilityOffVarianceComponent),
            Pair(SoundStartTrigger, Companion.TweenSoundStartTriggerComponent),
            Pair(SoundStopTrigger, Companion.TweenSoundStopTriggerComponent),
            Pair(SoundPosition, Companion.TweenSoundPositionComponent),
            Pair(SoundVolume, Companion.TweenSoundVolumeComponent),
            Pair(TextFieldText, Companion.TweenTextFieldTextComponent),
            Pair(TextFieldTextRangeStart, Companion.TweenTextFieldTextRangeStartComponent),
            Pair(TextFieldTextRangeEnd, Companion.TweenTextFieldTextRangeEndComponent),
            Pair(EventPublish, Companion.TweenEventPublishComponent),
            Pair(EventReset, Companion.TweenEventResetComponent),
            Pair(EventSubscribe, Companion.TweenEventSubscribeComponent),
            Pair(TouchInputEnable, Companion.TweenTouchInputEnableComponent),
        )

        testVector.forEach { animateType ->
            assertEquals(
                animateType.first.type,
                animateType.second,
                "Check if AnimateComponentType setup is correct: '${animateType.first.type}' - '${animateType.second}'")
        }

        assertEquals(
            TweenProperty.entries.size,
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
