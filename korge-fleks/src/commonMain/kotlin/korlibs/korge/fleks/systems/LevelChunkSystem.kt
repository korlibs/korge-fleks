package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.LevelMap.Companion.LevelMapComponent
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.tags.RenderLayerTag.MAIN_LEVELMAP
import korlibs.korge.fleks.utils.createAndConfigureEntity
import korlibs.korge.fleks.utils.getMainCamera


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
    override fun onTick() = with(world) {
        val assetStore = inject<AssetStore>(name = "AssetStore")
        val camera: Entity = getMainCamera()
        val cameraPosition = camera[PositionComponent]

        val levelFamily = family { all(MAIN_LEVELMAP, LevelMapComponent) }

        if (levelFamily.isNotEmpty) {
            val levelEntity = levelFamily.first()
            // Check where we are in the level gridvania
            val levelMapComponent = levelEntity[LevelMapComponent]
            val levelName = levelMapComponent.levelName
            val levelChunks = levelMapComponent.levelChunks
            val worldData = assetStore.getWorldData(levelName)
            val tileSize = worldData.tileSize

            // Calculate viewport position in world coordinates from Camera position (x,y) + offset
            val viewPortPosX: Float = cameraPosition.x  // - AppConfig.VIEW_PORT_WIDTH_HALF
            val viewPortPosY: Float = cameraPosition.y  // - AppConfig.VIEW_PORT_HEIGHT_HALF
            // Start and end indexes of viewport area (in tile coordinates)
            val viewPortMiddlePosX: Int = viewPortPosX.toInt() / tileSize  // x in positive direction
            val viewPortMiddlePosY: Int = viewPortPosY.toInt() / tileSize  // y in negative direction


            worldData.forEachEntityInChunk(viewPortMiddlePosX, viewPortMiddlePosY, levelChunks) { entityConfig ->
                createAndConfigureEntity(entityConfig)
            }
        }
    }
}