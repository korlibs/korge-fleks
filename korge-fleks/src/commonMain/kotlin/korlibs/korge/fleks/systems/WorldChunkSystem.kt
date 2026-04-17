package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.WorldMap
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.WorldChunk.Companion.WorldChunkComponent


/**
 * A system which does the following:
 * - loads new world chunks depending on the current camera position
 * - spawns entities from the newly loaded world chunks
 * - deactivates entities which are too far away from the camera (TODO)
 * - checks if all entities with a [WorldChunkComponent] have the correct chunk assigned depending on the entities position
 *
 * Hint: This system needs to be invoked with the same interval as the [PositionSystem] which moves the entities.
 *
 * This system needs to be invoked with the same interval as the [PositionSystem] which moves
 * the entities.
 */
class WorldChunkSystem : IntervalSystem(
    // Same interval as the game object move/position system
    interval = Fixed(1 / 60f)
) {
    private val systemRuntimeConfigs = world.inject<SystemRuntimeConfigs>("SystemRuntimeConfigs")
    private val worldMapData = world.inject<AssetStore>("AssetStore").worldMapData

    private val entityFamily = world.family { all(PositionComponent, WorldChunkComponent) }

    override fun onTick() = with(world) {
        // Get main camera position or exit if it does not exist
        val cameraPositionComponent = systemRuntimeConfigs.getCameraPositionComponent(world) ?: return
        val cameraWorldChunkComponent = systemRuntimeConfigs.getCameraWorldChunkComponent(world) ?: return
        val worldMapComponent: WorldMap = systemRuntimeConfigs.getWorldMapConfig(world) ?: return
        val tileSize = worldMapData.tileSize
        val chunkWidth = worldMapData.chunkPixelWidth
        val chunkHeight = worldMapData.chunkPixelHeight

        // Calculate viewport position in world coordinates from Camera position (x,y) + offset
        val viewPortPosX: Float = cameraPositionComponent.x  // - AppConfig.VIEW_PORT_WIDTH_HALF  // Camera position is middle point of view port
        val viewPortPosY: Float = cameraPositionComponent.y  // - AppConfig.VIEW_PORT_HEIGHT_HALF
        // Start and end indexes of viewport area (in tile coordinates)
        val viewPortMiddlePosX: Int = viewPortPosX.toInt() / tileSize  // x in positive direction, in world grid
        val viewPortMiddlePosY: Int = viewPortPosY.toInt() / tileSize  // y in negative direction, in world grid

        // Check if we need to load new chunks depending on the camera position
        // and if we need to spawn any new entities from those chunks
        worldMapData.run { world.loadNewChunksAndEntities(viewPortMiddlePosX, viewPortMiddlePosY, cameraWorldChunkComponent.chunk, worldMapComponent) }

        // TODO: Deactivate entities which are too far away from the camera

        // Check if all entities with a WorldChunkComponent have the correct chunk assigned depending on the entities position
        entityFamily.forEach { entity ->
            val positionComponent = entity[PositionComponent]
            val worldChunkComponent = entity[WorldChunkComponent]

            // Calculate the chunk position of the entity based on its position and the tile size
            val entityChunkPositionX = positionComponent.x.toInt() / chunkWidth
            val entityChunkPositionY = positionComponent.y.toInt() / chunkHeight

            // Force overwriting chunk with possible new chunk if entity is outside of current chunk in any direction (including diagonals)
            val currentChunk = worldMapData.chunkGridVania[entityChunkPositionX, entityChunkPositionY]
            if (currentChunk > 0) worldChunkComponent.chunk = currentChunk
        }
    }
}
