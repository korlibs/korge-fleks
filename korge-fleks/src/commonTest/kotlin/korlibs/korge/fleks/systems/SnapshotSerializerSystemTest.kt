package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.configureWorld
import korlibs.korge.fleks.entity.config.MainCameraConfig
import korlibs.korge.fleks.entity.config.commonMainCamera
import korlibs.korge.fleks.utils.Pool
import korlibs.korge.fleks.utils.addKorgeFleksInjectables
import korlibs.korge.fleks.utils.addKorgeFleksSystems
import korlibs.korge.fleks.utils.createAndConfigureEntity
import korlibs.time.seconds
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SnapshotSerializerSystemTest {
//*
    private val gameWorld = configureWorld {
        addKorgeFleksInjectables()
        addKorgeFleksSystems()
    }

    @Test
    fun testSnapshotSystem() {
        println("\n\nUNIT TEST: testSnapshotSystem")

        // First create entity config for camera
        MainCameraConfig(name = commonMainCamera)
        // Then create camera entity from entity config
        gameWorld.createAndConfigureEntity(entityConfig = commonMainCamera)

        TestGameEntityConfig(name = "test_game_entity_config")
        gameWorld.createAndConfigureEntity(entityConfig = "test_game_entity_config")

        val frameTime = (1f / 60f)

        // Run update methods of fleks ECS world
        val times = (70 * (1f / frameTime)).toInt()
        println("\n\nUNIT TEST: Start updating game world for $times times with frame time of ${frameTime.seconds}.")
        repeat(times) { time ->
            gameWorld.update(duration = frameTime.seconds)

            if (time > (20 * (1f / frameTime).toInt()) && time < (22 * (1f / frameTime).toInt())) {
                // Emulate pressing the rewind button for 2 seconds at game time 20 to 22 seconds
                gameWorld.system<SnapshotSerializerSystem>().rewind()
            }

            if (time == (23 * (1f / frameTime).toInt())) {
                // Resume the game world after rewinding at game time second 23
                gameWorld.system<SnapshotSerializerSystem>().triggerPause()
            }
        }

        Pool.writeStatistics()

        println("\n\nUNIT TEST: Remove all entities from game world...\n\n")
        gameWorld.removeAll()
        gameWorld.system<SnapshotSerializerSystem>().removeAll()

        Pool.doPoolUsageCheckAfterUnloading()

        Pool.listOfAllPools.forEach { pool ->
            assertEquals(
                pool.value.totalGeneratedItems,
                pool.value.itemsInPool,
                "Items in pool after removing all entities should be equal to total generated items in pool: ${pool.key}"
            )
            assertEquals(
                pool.value.totalItemsInUse,
                0,
                "Items in use after removing all entities should be 0: ${pool.key}"
            )
        }
    }
//*/
}