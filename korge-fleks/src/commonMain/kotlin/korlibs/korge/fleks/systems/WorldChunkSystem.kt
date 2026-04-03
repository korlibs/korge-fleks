package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.WorldMap
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.utils.createAndConfigureEntity
import kotlin.coroutines.CoroutineContext


/**
 * A system which spawns entities from the world chunks depending on the current camera position.
 * This system needs to be invoked with the same interval as the [PositionSystem] which moves
 * the entities.
 */
class WorldChunkSystem(
    private val coroutineContext: CoroutineContext
) : IntervalSystem(
    // Same interval as the game object move/position system
    interval = Fixed(1 / 60f)
) {
    private val systemRuntimeConfigs = world.inject<SystemRuntimeConfigs>("SystemRuntimeConfigs")
    private val levelData = world.inject<AssetStore>("AssetStore").worldMapData

    override fun onTick() = with(world) {
        // Get main camera position or exit if it does not exist
        val cameraPosition: Position = systemRuntimeConfigs.getCameraPosition(world) ?: return
        val worldMapComponent: WorldMap = systemRuntimeConfigs.getWorldChunkConfig(world) ?: return

        val activatedChunks = worldMapComponent.activatedChunks
        val tileSize = levelData.tileSize

        // Calculate viewport position in world coordinates from Camera position (x,y) + offset
        val viewPortPosX: Float = cameraPosition.x  // - AppConfig.VIEW_PORT_WIDTH_HALF  // Camera position is middle point of view port
        val viewPortPosY: Float = cameraPosition.y  // - AppConfig.VIEW_PORT_HEIGHT_HALF
        // Start and end indexes of viewport area (in tile coordinates)
        val viewPortMiddlePosX: Int = viewPortPosX.toInt() / tileSize  // x in positive direction, in world grid
        val viewPortMiddlePosY: Int = viewPortPosY.toInt() / tileSize  // y in negative direction, in world grid

        levelData.forEachEntityInChunk(viewPortMiddlePosX, viewPortMiddlePosY, activatedChunks, coroutineContext) { entityConfig ->
            println("Chunk entity to create: $entityConfig")
            createAndConfigureEntity(entityConfig)
        }

        // TODO: Deactivate entities which are too far away from the camera
    }
}
