package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenEventPublishComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenEventResetComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenEventSubscribeComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenMotionVelocityXComponent
import korlibs.math.interpolation.*
import kotlin.test.*
import korlibs.korge.fleks.components.TweenProperty.TweenPropertyType.*
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenPositionOffsetXComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenPositionOffsetYComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenPositionXComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenPositionYComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenRgbaAlphaComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenRgbaTintComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSoundPositionComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSoundStartTriggerComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSoundStopTriggerComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSoundVolumeComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSpawnerIntervalComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSpawnerNumberOfObjectsComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSpawnerPositionVariationComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSpawnerTimeVariationComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSpriteAnimationComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSpriteDestroyOnPlayingFinishedComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSpriteDirectionComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSpriteRunningComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSwitchLayerVisibilityOffVarianceComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSwitchLayerVisibilityOnVarianceComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenTextFieldTextComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenTextFieldTextRangeEndComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenTextFieldTextRangeStartComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenTouchInputEnableComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.tweenPropertyComponent

internal class TweenPropertyComponentTest {

    private val expectedWorld = configureWorld {}
    private val recreatedWorld = configureWorld {}

    @Test
    fun testTweenPropertyComponentTypeIntegrity() {
        val testVector: List<Pair<TweenProperty.TweenPropertyType, ComponentType<*>>> = listOf(
            Pair(PositionOffsetX, TweenPositionOffsetXComponent),
            Pair(PositionOffsetY, TweenPositionOffsetYComponent),
            Pair(PositionX, TweenPositionXComponent),
            Pair(PositionY, TweenPositionYComponent),
            Pair(MotionVelocityX, TweenMotionVelocityXComponent),
            Pair(RgbaAlpha, TweenRgbaAlphaComponent),
            Pair(RgbaTint, TweenRgbaTintComponent),
            Pair(SpawnerInterval, TweenSpawnerIntervalComponent),
            Pair(SpawnerNumberOfObjects, TweenSpawnerNumberOfObjectsComponent),
            Pair(SpawnerPositionVariation, TweenSpawnerPositionVariationComponent),
            Pair(SpawnerTimeVariation, TweenSpawnerTimeVariationComponent),
            Pair(SpriteRunning, TweenSpriteRunningComponent),
            Pair(SpriteDirection, TweenSpriteDirectionComponent),
            Pair(SpriteDestroyOnPlayingFinished, TweenSpriteDestroyOnPlayingFinishedComponent),
            Pair(SpriteAnimation, TweenSpriteAnimationComponent),
            Pair(SwitchLayerVisibilityOnVariance, TweenSwitchLayerVisibilityOnVarianceComponent),
            Pair(SwitchLayerVisibilityOffVariance, TweenSwitchLayerVisibilityOffVarianceComponent),
            Pair(SoundStartTrigger, TweenSoundStartTriggerComponent),
            Pair(SoundStopTrigger, TweenSoundStopTriggerComponent),
            Pair(SoundPosition, TweenSoundPositionComponent),
            Pair(SoundVolume, TweenSoundVolumeComponent),
            Pair(TextFieldText, TweenTextFieldTextComponent),
            Pair(TextFieldTextRangeStart, TweenTextFieldTextRangeStartComponent),
            Pair(TextFieldTextRangeEnd, TweenTextFieldTextRangeEndComponent),
            Pair(EventPublish, TweenEventPublishComponent),
            Pair(EventReset, TweenEventResetComponent),
            Pair(EventSubscribe, TweenEventSubscribeComponent),
            Pair(TouchInputEnable, TweenTouchInputEnableComponent),
        )

        testVector.forEach { animateType ->
            assertEquals(
                animateType.first.type,
                animateType.second,
                "Check if AnimateComponentType setup is correct: '${animateType.first.type}' - '${animateType.second}'")
        }

        assertEquals(
            TweenProperty.TweenPropertyType.entries.size,
            testVector.size,
            "Check if all AnimateComponentType enum values have been tested"
        )
    }

    @Test
    fun testAnimateComponentSerialization() {

        val componentUnderTest = tweenPropertyComponent {
            property = RgbaAlpha
            change = Unit
            value = Unit
            duration = 1.2f
            timeProgress = 3.4f
            easing = Easing.EASE_IN
        }

        val entity = expectedWorld.entity {
            it += componentUnderTest
        }

        runAnimateComponentSerializationTest(entity, componentUnderTest, change = 42.43, value = 45.46)  // Double test
        runAnimateComponentSerializationTest(entity, componentUnderTest, change = 42, value = 43)  // Int test
        runAnimateComponentSerializationTest(entity, componentUnderTest, change = true, value = false)  // Boolean test
        runAnimateComponentSerializationTest(entity, componentUnderTest, change = "testString", value = "anotherString")  // String test
// TODO update to RGBA
//      runAnimateComponentSerializationTest(entity, componentUnderTest, change = Rgb.MIDDLE_GREY, value = Rgb(12, 34, 56))  // Rgba test
    }

    private fun <T> runAnimateComponentSerializationTest(
        entity: Entity, component: TweenProperty,
        change: T, value: T
    ) {
        component.change = change as Any
        component.value = value as Any

        CommonTestEnv.serializeDeserialize(expectedWorld, recreatedWorld)

        // get the component from entity with the same id from the new created world
        val newEntity = recreatedWorld.asEntityBag()[entity.id]
        val recreatedTestComponent = with (recreatedWorld) { newEntity[TweenRgbaAlphaComponent] }

        assertEquals(component.change, recreatedTestComponent.change, "Check 'change' property to be equal")
        assertEquals(component.value, recreatedTestComponent.value, "Check 'value' property to be equal")
    }
}
