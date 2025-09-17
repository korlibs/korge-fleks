package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.TweenSequence.Companion.TweenSequenceComponent
import korlibs.korge.fleks.components.TweenSequence.Companion.tweenSequenceComponent
import korlibs.korge.fleks.components.data.tweenSequence.DeleteEntity.Companion.deleteEntity
import korlibs.korge.fleks.components.data.tweenSequence.ParallelTweens
import korlibs.korge.fleks.components.data.tweenSequence.ParallelTweens.Companion.parallelTweens
import korlibs.korge.fleks.components.data.tweenSequence.SpawnEntity
import korlibs.korge.fleks.components.data.tweenSequence.SpawnEntity.Companion.spawnEntity
import korlibs.korge.fleks.components.data.tweenSequence.SpawnNewTweenSequence
import korlibs.korge.fleks.components.data.tweenSequence.SpawnNewTweenSequence.Companion.spawnNewTweenSequence
import korlibs.korge.fleks.components.data.tweenSequence.TweenPosition.Companion.tweenPosition
import korlibs.korge.fleks.components.data.tweenSequence.TweenRgba.Companion.tweenRgba
import korlibs.korge.fleks.components.data.tweenSequence.TweenSound.Companion.tweenSound
import korlibs.korge.fleks.components.data.tweenSequence.TweenSpawner.Companion.tweenSpawner
import korlibs.korge.fleks.components.data.tweenSequence.TweenSprite.Companion.tweenSprite
import korlibs.korge.fleks.components.data.tweenSequence.TweenSwitchLayerVisibility.Companion.tweenSwitchLayerVisibility
import korlibs.korge.fleks.components.data.tweenSequence.Wait.Companion.wait
import korlibs.korge.fleks.entity.EntityFactory
import korlibs.korge.fleks.gameState.GameStateManager
import korlibs.korge.fleks.utils.Pool
import korlibs.korge.fleks.utils.addKorgeFleksInjectables
import korlibs.korge.fleks.utils.createEntity
import korlibs.math.interpolation.*
import kotlin.test.Test
import kotlin.test.assertEquals


internal class TweenSequenceComponentTest {
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
    fun testTweenSequenceComponentSnapshotting() {
        println("TEST CASE: testTweenSequenceComponentSnapshotting")
        // TODO
    }

    @Test
    fun testTweenSequenceComponentSerialization() {
        println("TEST CASE: testTweenSequenceComponentSerialization")

        val testEntityConfig = TestEntityConfig("testEntityConfig")

        val compUnderTest = tweenSequenceComponent {
            spawnNewTweenSequence {
                delay = 1f
                duration = 2f
                easing = Easing.EASE_IN_BACK

                parallelTweens {
                    delay = 1.2f
                    duration = 3.4f
                    easing = Easing.EASE_CLAMP_END

                    spawnEntity {
                        entityConfig = testEntityConfig.name
                        x = 10.2f
                        y = 20.3f
                        target = Entity(43, 0u)
                    }
                    deleteEntity { target = Entity(44, 0u) }
                    tweenRgba { target = Entity(45, 0u) }
                    tweenPosition { target = Entity(46, 0u) }
                    tweenSprite { target = Entity(48, 0u) }
                    tweenSwitchLayerVisibility { target = Entity(49, 0u) }
                    tweenSpawner { target = Entity(51, 0u) }
                    tweenSound { target = Entity(52, 0u) }
                }
            }
            wait { duration = 3.2f }

            index = 1
            timeProgress = 3.4f
            waitTime = 5.6f
            executed = true
        }

        val entity = expectedWorld.createEntity(aName = "testEntity") {
            it += compUnderTest  // Hint: Do not pass this component to more than one entity otherwise the component will be freed twice!
        }

        CommonTestEnv.serializeDeserialize(expectedWorld, recreatedWorld)

        // get the component from entity with the same id from the new created world
        val newEntity = recreatedWorld.asEntityBag()[entity.id]
        val newCompUnderTest = with (recreatedWorld) { newEntity[TweenSequenceComponent] }

        assertEquals(compUnderTest.index, newCompUnderTest.index, "Check 'index' property to be equal")
        assertEquals(compUnderTest.timeProgress, newCompUnderTest.timeProgress, "Check 'timeProgress' property to be equal")
        assertEquals(compUnderTest.waitTime, newCompUnderTest.waitTime, "Check 'waitTime' property to be equal")
        assertEquals(compUnderTest.executed, newCompUnderTest.executed, "Check 'active' property to be equal")
        val sequenceOfTweens = compUnderTest.tweens.first() as SpawnNewTweenSequence
        val newSequenceOfTweens = compUnderTest.tweens.first() as SpawnNewTweenSequence
        assertEquals(sequenceOfTweens.delay, newSequenceOfTweens.delay, "Check 'tweenSequence.delay' property to be equal")
        assertEquals(sequenceOfTweens.duration, newSequenceOfTweens.duration, "Check 'tweenSequence.duration' property to be equal")
        assertEquals(sequenceOfTweens.easing, newSequenceOfTweens.easing, "Check 'tweenSequence.easing' property to be equal")
        val parallelTweens = sequenceOfTweens.tweens.first() as ParallelTweens
        val newParallelTweens = newSequenceOfTweens.tweens.first() as ParallelTweens
        assertEquals(parallelTweens.delay, newParallelTweens.delay, "Check 'parallelTweens.delay' property to be equal")
        assertEquals(parallelTweens.duration, newParallelTweens.duration, "Check 'parallelTweens.duration' property to be equal")
        assertEquals(parallelTweens.easing, newParallelTweens.easing, "Check 'parallelTweens.easing' property to be equal")
        val spawnEntity = parallelTweens.tweens.first() as SpawnEntity
        val newSpawnEntity = newParallelTweens.tweens.first() as SpawnEntity
        assertEquals(spawnEntity.entityConfig, newSpawnEntity.entityConfig, "spawnEntity.entityConfig' property to be equal")
        assertEquals(spawnEntity.x, newSpawnEntity.x, "Check 'spawnEntity.x' property to be equal")
        assertEquals(spawnEntity.y, newSpawnEntity.y, "Check 'spawnEntity.y' property to be equal")
        assertEquals(spawnEntity.target, newSpawnEntity.target, "Check 'spawnEntity.target' property to be equal")
        val spawnedEntity = EntityFactory.createAndConfigureEntity(expectedWorld, spawnEntity.entityConfig)
        assertEquals(spawnedEntity.id, 8080, "Check that configure function is invoked correctly")

        // Delete the entity with the component from the expected world -> put component back to the pool
        expectedWorld.removeAll()

        Pool.doPoolUsageCheckAfterUnloading()
    }
// */
}
