package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.configureWorld
import korlibs.math.interpolation.Easing
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.entity.config.Invokables
import korlibs.korge.fleks.utils.Identifier
import kotlin.test.Test
import kotlin.test.assertEquals


internal class AnimationScriptTest {

    private val expectedWorld = configureWorld {}
    private val recreatedWorld = configureWorld {}

    @Test
    fun testAnimationScriptSerialization() {

        Invokables.register(testConfigure, testConfigureFct)

        val testIdentifier = Identifier("testEntityConfig")

        val compUnderTest = AnimationScript(
            tweens = listOf(
                TweenSequence(
                    tweens = listOf(
                        ParallelTweens(
                            tweens = listOf(
                                SpawnEntity(
                                    config = testIdentifier,
                                    function = testConfigure,
                                    x = 10.2f,
                                    y = 20.3f,
                                    entity = Entity(43)
                                ),
                                DeleteEntity(entity = Entity(44)),
                                TweenAppearance(entity = Entity(45)),
                                TweenPositionShape(entity = Entity(46)),
                                TweenOffset(entity = Entity(47)),
                                TweenSprite(entity = Entity(48)),
                                TweenSwitchLayerVisibility(entity = Entity(49)),
                                TweenSpawner(entity = Entity(51)),
                                TweenSound(entity = Entity(52))
                            ),
                            delay = 1.2f,
                            duration = 3.4f,
                            easing = Easing.EASE_CLAMP_END
                        )
                    ),
                delay = 1f,
                duration = 2f,
                easing = Easing.EASE_IN_BACK
                ),
                Wait(duration = 3.2f)
            ),
            index = 42,
            timeProgress = 3.4f,
            waitTime = 5.6f,
            active = true
        )

        val entity = expectedWorld.entity {
            it += compUnderTest
        }

        CommonTestEnv.serializeDeserialize(expectedWorld, recreatedWorld)

        // get the component from entity with the same id from the new created world
        val newEntity = recreatedWorld.asEntityBag()[entity.id]
        val newCompUnderTest = with (recreatedWorld) { newEntity[AnimationScript] }

        assertEquals(compUnderTest.index, newCompUnderTest.index, "Check 'index' property to be equal")
        assertEquals(compUnderTest.timeProgress, newCompUnderTest.timeProgress, "Check 'timeProgress' property to be equal")
        assertEquals(compUnderTest.waitTime, newCompUnderTest.waitTime, "Check 'waitTime' property to be equal")
        assertEquals(compUnderTest.active, newCompUnderTest.active, "Check 'active' property to be equal")
        val tweenSequence = compUnderTest.tweens.first() as TweenSequence
        val newTweenSequence = compUnderTest.tweens.first() as TweenSequence
        assertEquals(tweenSequence.delay, newTweenSequence.delay, "Check 'tweenSequence.delay' property to be equal")
        assertEquals(tweenSequence.duration, newTweenSequence.duration, "Check 'tweenSequence.duration' property to be equal")
        assertEquals(tweenSequence.easing, newTweenSequence.easing, "Check 'tweenSequence.easing' property to be equal")
        val parallelTweens = tweenSequence.tweens.first() as ParallelTweens
        val newParallelTweens = newTweenSequence.tweens.first() as ParallelTweens
        assertEquals(parallelTweens.delay, newParallelTweens.delay, "Check 'parallelTweens.delay' property to be equal")
        assertEquals(parallelTweens.duration, newParallelTweens.duration, "Check 'parallelTweens.duration' property to be equal")
        assertEquals(parallelTweens.easing, newParallelTweens.easing, "Check 'parallelTweens.easing' property to be equal")
        val spawnEntity = parallelTweens.tweens.first() as SpawnEntity
        val newSpawnEntity = newParallelTweens.tweens.first() as SpawnEntity
        assertEquals(spawnEntity.config, newSpawnEntity.config, "spawnEntity.config' property to be equal")
        assertEquals(spawnEntity.function, newSpawnEntity.function, "Check 'spawnEntity.function' property to be equal")
        assertEquals(spawnEntity.x, newSpawnEntity.x, "Check 'spawnEntity.x' property to be equal")
        assertEquals(spawnEntity.y, newSpawnEntity.y, "Check 'spawnEntity.y' property to be equal")
        assertEquals(spawnEntity.entity, newSpawnEntity.entity, "Check 'spawnEntity.entity' property to be equal")
        val spawnedEntity = Invokables.invoke(spawnEntity.function, recreatedWorld, Entity(4242), spawnEntity.config)
        assertEquals(spawnedEntity.id, 8080, "Check that configure function is invoked correctly")
    }
}