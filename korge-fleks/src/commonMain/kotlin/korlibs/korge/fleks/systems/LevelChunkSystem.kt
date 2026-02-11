package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.inject
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.LevelMap.Companion.LevelMapComponent
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.prefab.SystemRuntimeConfigs
import korlibs.korge.fleks.tags.RenderLayerTag.MAIN_LEVELMAP


/**
 * A system which spawns entities from the level chunks depending on the current camera position.
 * This system needs to be invoked with the same interval as the [PositionSystem] which moves
 * the entities.
 */
class LevelChunkSystem(

) : IntervalSystem(
    // Same interval as the game object move/position system
    interval = Fixed(1 / 60f)
) {
    val levelFamily = world.family { all(MAIN_LEVELMAP, LevelMapComponent) }
    private val systemRuntimeConfigs = world.inject<SystemRuntimeConfigs>("SystemRuntimeConfigs")
    private val levelData = inject<AssetStore>("AssetStore").levelData


    override fun onTick() = with(world) {
        // Get main camera position or exit if it does not exist
        val cameraPosition: Position = systemRuntimeConfigs.getCameraPosition(world) ?: return

        if (levelFamily.isNotEmpty) {
            val levelEntity = levelFamily.first()
            // Check where we are in the level gridvania
            val levelMapComponent = levelEntity[LevelMapComponent]
//            val levelChunks = levelMapComponent.levelChunks
            val tileSize = levelData.tileSize

            // Calculate viewport position in world coordinates from Camera position (x,y) + offset
            val viewPortPosX: Float = cameraPosition.x  // - AppConfig.VIEW_PORT_WIDTH_HALF
            val viewPortPosY: Float = cameraPosition.y  // - AppConfig.VIEW_PORT_HEIGHT_HALF
            // Start and end indexes of viewport area (in tile coordinates)
            val viewPortMiddlePosX: Int = viewPortPosX.toInt() / tileSize  // x in positive direction
            val viewPortMiddlePosY: Int = viewPortPosY.toInt() / tileSize  // y in negative direction


//            Prefab.levelData.forEachEntityInChunk(viewPortMiddlePosX, viewPortMiddlePosY, levelChunks) { entityConfig ->
//                println("Chunk entity to create: $entityConfig")
//                createAndConfigureEntity(entityConfig)
//            }

            // TODO: Deactivate entities which are too far away from the camera
        }
    }
}
