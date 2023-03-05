package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.world
import com.soywiz.korma.interpolation.Easing
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AnimationScriptTest {

    private val expectedWorld = world {}
    private val recreatedWorld = world {}

    @Test
    fun testAnimationScriptSerialization() {

        val compUnderTest = AnimationScript(
            tweens = listOf(
                TweenSequence(
                    tweens = listOf(
                        ParallelTweens(
                            tweens = listOf(
                                SpawnEntity(
                                    spawnFunction = "TestSprite",
                                    createNewEntity = true,
                                    x = 10.2,
                                    y = 20.3,
                                    config = TestConfig(
                                        logoName = "TestLogo"
                                    ),
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

        serializeDeserialize(expectedWorld, recreatedWorld)

        // get the component from entity with the same id from the new created world
        val newCompUnderTest = with (recreatedWorld) { recreatedWorld.asEntityBag()[entity.id][AnimationScript] }

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
        assertEquals(spawnEntity.spawnFunction, newSpawnEntity.spawnFunction, "Check 'spawnEntity.spawnFunction' property to be equal")
        assertEquals(spawnEntity.createNewEntity, newSpawnEntity.createNewEntity, "Check 'spawnEntity.createNewEntity' property to be equal")
        assertEquals(spawnEntity.x, newSpawnEntity.x, "Check 'spawnEntity.x' property to be equal")
        assertEquals(spawnEntity.y, newSpawnEntity.y, "Check 'spawnEntity.y' property to be equal")
        assertEquals(spawnEntity.entity, newSpawnEntity.entity, "Check 'spawnEntity.entity' property to be equal")
        val config = spawnEntity.config as TestConfig
        val newConfig = newSpawnEntity.config as TestConfig
        assertEquals(config.logoName, newConfig.logoName, "Check 'spawnEntity.config.logoName' property to be equal")
    }
}