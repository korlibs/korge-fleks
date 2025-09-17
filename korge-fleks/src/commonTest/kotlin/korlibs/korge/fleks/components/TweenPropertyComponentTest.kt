package korlibs.korge.fleks.components

import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.configureWorld
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenEventPublishComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenEventResetComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenEventSubscribeComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenMotionVelocityXComponent
import kotlin.test.*
import korlibs.korge.fleks.components.TweenProperty.TweenPropertyType.*
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenPositionOffsetXComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenPositionOffsetYComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenPositionXComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenPositionYComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenRgbaAlphaComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenRgbaBlueComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenRgbaGreenComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenRgbaRedComponent
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
import korlibs.korge.fleks.gameState.GameStateManager
import korlibs.korge.fleks.utils.Pool
import korlibs.korge.fleks.utils.addKorgeFleksInjectables
import korlibs.korge.fleks.utils.createEntity


internal class TweenPropertyComponentTest {
//*
private val assetStore = AssetStore().also { it.testing = true }
    private val gameState = GameStateManager()

    private val expectedWorld = configureWorld {
        addKorgeFleksInjectables(assetStore, gameState)
    }
    private val recreatedWorld = configureWorld {
        addKorgeFleksInjectables(assetStore, gameState)
    }

    @Test
    fun testTweenPropertyComponentTypeIntegrity() {
        println("TEST CASE: testTweenPropertyComponentTypeIntegrity")

        val testVector: List<Pair<TweenProperty.TweenPropertyType, ComponentType<*>>> = listOf(
            Pair(UnconfiguredType, UnconfiguredType.type),  // UnconfiguredType has no tween component type
            Pair(PositionOffsetX, TweenPositionOffsetXComponent),
            Pair(PositionOffsetY, TweenPositionOffsetYComponent),
            Pair(PositionX, TweenPositionXComponent),
            Pair(PositionY, TweenPositionYComponent),
            Pair(MotionVelocityX, TweenMotionVelocityXComponent),
            Pair(RgbaAlpha, TweenRgbaAlphaComponent),
            Pair(RgbaRed, TweenRgbaRedComponent),
            Pair(RgbaGreen, TweenRgbaGreenComponent),
            Pair(RgbaBlue, TweenRgbaBlueComponent),
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
            val expectedType = animateType.first.type
            val actualType = animateType.second
            assertEquals(
                expectedType,
                actualType,
                "Check if AnimateComponentType setup is correct: '${animateType.first.type}' - '${animateType.second}'"
            )
        }

        assertEquals(
            TweenProperty.TweenPropertyType.entries.size,
            testVector.size,
            "Check if all AnimateComponentType enum values have been tested"
        )
    }

    @Test
    fun testTweenPropertyComponentSerialization() {
        println("TEST CASE: testTweenPropertyComponentSerialization")

        val entityFloat = expectedWorld.createEntity("testTweenPropertyComponentSerializationFloat12") {
            it += tweenPropertyComponent {
                property = RgbaAlpha
                change = 12.34f
                value = 56.78f
            }
        }

        val entityInt = expectedWorld.createEntity("testTweenPropertyComponentSerializationInt") {
            it += tweenPropertyComponent {
                property = RgbaAlpha
                change = 12
                value = 34
            }
        }

        val entityBool = expectedWorld.createEntity("testTweenPropertyComponentSerializationBool") {
            it += tweenPropertyComponent {
                property = RgbaAlpha
                change = true
                value = false
            }
        }

        val entityString = expectedWorld.createEntity("testTweenPropertyComponentSerializationString") {
            it += tweenPropertyComponent {
                property = RgbaAlpha
                change = "testString"
                value = "anotherString"
            }
        }

// TODO update to RGBA
//      runAnimateComponentSerializationTest(entity, componentUnderTest, change = Rgb.MIDDLE_GREY, value = Rgb(12, 34, 56))  // Rgba test

        CommonTestEnv.serializeDeserialize(expectedWorld, recreatedWorld)

        // get the component from entity with the same id from the new created world
        val newEntityFloat = recreatedWorld.asEntityBag()[entityFloat.id]
        val recreatedTestComponentFloat = with (recreatedWorld) { newEntityFloat[TweenRgbaAlphaComponent] }
        val newEntityInt = recreatedWorld.asEntityBag()[entityInt.id]
        val recreatedTestComponentInt = with (recreatedWorld) { newEntityInt[TweenRgbaAlphaComponent] }
        val newEntityBool = recreatedWorld.asEntityBag()[entityBool.id]
        val recreatedTestComponentBool = with (recreatedWorld) { newEntityBool[TweenRgbaAlphaComponent] }
        val newEntityString = recreatedWorld.asEntityBag()[entityString.id]
        val recreatedTestComponentString = with (recreatedWorld) { newEntityString[TweenRgbaAlphaComponent] }

        assertEquals(12.34f, recreatedTestComponentFloat.change, "Check float 'change' property to be equal")
        assertEquals(56.78f, recreatedTestComponentFloat.value, "Check float 'value' property to be equal")
        assertEquals(12, recreatedTestComponentInt.change, "Check int 'change' property to be equal")
        assertEquals(34, recreatedTestComponentInt.value, "Check int 'value' property to be equal")
        assertEquals(true, recreatedTestComponentBool.change, "Check bool 'change' property to be equal")
        assertEquals(false, recreatedTestComponentBool.value, "Check bool 'value' property to be equal")
        assertEquals("testString", recreatedTestComponentString.change, "Check string 'change' property to be equal")
        assertEquals("anotherString", recreatedTestComponentString.value, "Check string 'value' property to be equal")

        // Delete the entity with the component from the expected world -> put component back to the pool
        expectedWorld.removeAll()

        Pool.doPoolUsageCheckAfterUnloading()
    }
//*/
}
