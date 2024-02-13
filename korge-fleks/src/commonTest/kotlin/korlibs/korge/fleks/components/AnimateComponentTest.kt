package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.TweenProperty.*
import korlibs.korge.fleks.components.TweenComponent.*
import korlibs.math.interpolation.Easing
import kotlin.test.*

internal class AnimateComponentTest {

    private val expectedWorld = configureWorld {}
    private val recreatedWorld = configureWorld {}

    @Test
    fun testAnimateComponentTypeIntegrity() {
        val testVector: List<Pair<TweenProperty, ComponentType<*>>> = listOf(
            Pair(SpriteIsPlaying, Companion.AnimateSpriteIsPlaying),
            Pair(SpriteForwardDirection, Companion.AnimateSpriteForwardDirection),
            Pair(SpriteLoop, Companion.AnimateSpriteLoop),
            Pair(SpriteDestroyOnPlayingFinished, Companion.AnimateSpriteDestroyOnPlayingFinished),
            Pair(SpriteAnimName, Companion.AnimateSpriteAnimName),
            Pair(AppearanceAlpha, Companion.AnimateAppearanceAlpha),
            Pair(AppearanceTint, Companion.AnimateAppearanceTint),
            Pair(AppearanceVisible, Companion.AnimateAppearanceVisible),
            Pair(SpawnerNumberOfObjects, Companion.AnimateSpawnerNumberOfObjects),
            Pair(SpawnerInterval, Companion.AnimateSpawnerInterval),
            Pair(SpawnerTimeVariation, Companion.AnimateSpawnerTimeVariation),
            Pair(SpawnerPositionVariation, Companion.AnimateSpawnerPositionVariation),
            Pair(LifeCycleHealthCounter, Companion.AnimateLifeCycleHealthCounter),
            Pair(PositionShapeX, Companion.AnimatePositionShapeX),
            Pair(PositionShapeY, Companion.AnimatePositionShapeY),
            Pair(OffsetX, Companion.AnimateOffsetX),
            Pair(OffsetY, Companion.AnimateOffsetY),
            Pair(LayoutCenterX, Companion.AnimateLayoutCenterX),
            Pair(LayoutCenterY, Companion.AnimateLayoutCenterY),
            Pair(LayoutOffsetX, Companion.AnimateLayoutOffsetX),
            Pair(LayoutOffsetY, Companion.AnimateLayoutOffsetY),
            Pair(SwitchLayerVisibilityOnVariance, Companion.AnimateSwitchLayerVisibilityOnVariance),
            Pair(SwitchLayerVisibilityOffVariance, Companion.AnimateSwitchLayerVisibilityOffVariance),
            Pair(SoundStartTrigger, Companion.AnimateSoundStartTrigger),
            Pair(SoundStopTrigger, Companion.AnimateSoundStopTrigger),
            Pair(SoundPosition, Companion.AnimateSoundPosition),
            Pair(SoundVolume, Companion.AnimateSoundVolume),
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

        val componentUnderTest = TweenComponent(
            property = AppearanceAlpha,
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
        entity: Entity, component: TweenComponent,
        change: T, value: T
    ) {
        component.change = change as Any
        component.value = value as Any

        CommonTestEnv.serializeDeserialize(expectedWorld, recreatedWorld)

        // get the component from entity with the same id from the new created world
        val recreatedTestComponent = with (recreatedWorld) { recreatedWorld.asEntityBag()[entity.id][Companion.AnimateAppearanceAlpha] }

        assertEquals(component.change, recreatedTestComponent.change, "Check 'change' property to be equal")
        assertEquals(component.value, recreatedTestComponent.value, "Check 'value' property to be equal")
    }
}
