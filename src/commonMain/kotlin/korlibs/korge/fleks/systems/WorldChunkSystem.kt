package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import korlibs.io.async.launchAsap
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.assets.data.WorldMapData.ViewPortPosition
import korlibs.korge.fleks.components.WorldMap
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.WorldChunk.Companion.WorldChunkComponent
import korlibs.korge.fleks.state.GameStateManager
import korlibs.korge.fleks.utils.createAndConfigureEntity
import korlibs.korge.view.Views


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
    private val gameStateManager = world.inject<GameStateManager>("GameState")
    private val systemRuntimeConfigs = world.inject<SystemRuntimeConfigs>("SystemRuntimeConfigs")
    private val worldMapData = world.inject<AssetStore>("AssetStore").worldMapData
    private val assetStore = world.inject<AssetStore>("AssetStore")
    private val views = world.inject<Views>("Views")

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
        loadNewChunksAndEntities(viewPortMiddlePosX, viewPortMiddlePosY, cameraWorldChunkComponent.chunk, worldMapComponent)

        // TODO: Deactivate entities which are too far away from the camera

        // Check if all entities with a WorldChunkComponent have the correct chunk assigned depending on the entities position
        entityFamily.forEach { entity ->
            val positionComponent = entity[PositionComponent]
            val worldChunkComponent = entity[WorldChunkComponent]

            // Calculate the chunk position of the entity based on its position and the tile size
            val entityChunkPositionX = positionComponent.x.toInt() / chunkWidth
            val entityChunkPositionY = positionComponent.y.toInt() / chunkHeight

            // Force overwriting chunk with possible new chunk if entity is outside of current chunk in any direction (including diagonals)
            val currentChunk = worldMapData.getChunkIndex(entityChunkPositionX, entityChunkPositionY)
            if (currentChunk > 0) worldChunkComponent.chunk = currentChunk
        }
    }

    /**
     * Iterate over all entities within the chunk, where the camera is currently located, and all
     * adjacent chunks. Call the callback function for each entity config.
     *
     * @param viewPortMiddlePosX Horizontal (middle) position of view port in world grid cells
     * @param viewPortMiddlePosY Vertical (middle) position of view port in world grid cells
     * @param currentChunk Chunk index of the chunk which contains the view port middle position
     * @param worldMapComponent WorldMap component of the current world which contains the set of already activated chunk indices
     *        to avoid spawning entities multiple times when the player is moving within the same chunks
     */
    private fun loadNewChunksAndEntities(
        viewPortMiddlePosX: Int,
        viewPortMiddlePosY: Int,
        currentChunk: Int,
        worldMapComponent: WorldMap
    ) {
        // Set of already activated chunk indices to avoid spawning entities multiple times when the
        // player is moving within the same chunks
        val activatedChunks = worldMapComponent.activatedChunks

        // Check in which quadrant of the grid the view port is located
        // and iterate over the adjacent chunks (2x2 grid)
        val localViewPortPosX: Int = viewPortMiddlePosX % worldMapData.chunkWidth
        val localViewPortPosY: Int = viewPortMiddlePosY % worldMapData.chunkHeight

        // Number of chunks which needs to be loaded (if not already loaded) depending on the quadrant of the view port
        // Chunk -1 means no chunk assigned to the grid cell, so we can ignore it when checking if the chunk needs to be loaded
        // TODO what does chunk == 0 mean ???

        // Sanity check - we should always have a chunk mesh for the current chunk index, otherwise we cannot determine the adjacent chunks and load them if needed
        if (!worldMapData.chunkLookUpTable.containsKey(currentChunk)) {
            // Calculate the grid position of the view port middle position
            val gridX: Int = viewPortMiddlePosX / worldMapData.chunkWidth
            val gridY: Int = viewPortMiddlePosY / worldMapData.chunkHeight
            println("WARNING: WorldMapData - No chunk mesh found for current chunk index '$currentChunk' in grid position ($gridX, $gridY)!")
            return
        }
        val currentChunkInfo = worldMapData.chunkLookUpTable[currentChunk]!!

        // Check in which quadrant of the grid-vania the view port middle position is located
        val viewPortPosition: ViewPortPosition = if (localViewPortPosX < worldMapData.levelMidPointX) {
            if (localViewPortPosY < worldMapData.levelMidPointY) ViewPortPosition.TOP_LEFT else ViewPortPosition.BOTTOM_LEFT
        } else {
            if (localViewPortPosY < worldMapData.levelMidPointY) ViewPortPosition.TOP_RIGHT else ViewPortPosition.BOTTOM_RIGHT
        }

        when (viewPortPosition) {
            // Check if any of the first 2 chunks needs to be loaded and if its entities needs to be spawned
            // Then check if the third chunk needs to be loaded and if its entities needs to be spawned
            ViewPortPosition.TOP_LEFT -> if (!worldMapData.loadingTopLeftChunk) {
                worldMapData.loadingTopLeftChunk = true
                views.launchAsap {
                    val topChunk = currentChunkInfo.chunkTop
                    val leftChunk = currentChunkInfo.chunkLeft
                    if (checkAndLoadChunk(topChunk, activatedChunks)) {
                        worldMapData.chunkLookUpTable[topChunk]?.let { topChunkInfo -> checkAndLoadChunk(topChunkInfo.chunkLeft, activatedChunks) }
                    }
                    if (checkAndLoadChunk(leftChunk, activatedChunks)) {
                        worldMapData.chunkLookUpTable[leftChunk]?.let { leftChunkInfo -> checkAndLoadChunk(leftChunkInfo.chunkTop, activatedChunks) }
                    }
                    worldMapData.loadingTopLeftChunk = false
                }
            }
            ViewPortPosition.TOP_RIGHT -> if (!worldMapData.loadingTopRightChunk) {
                worldMapData.loadingTopRightChunk = true
                views.launchAsap {
                    val topChunk = currentChunkInfo.chunkTop
                    val rightChunk = currentChunkInfo.chunkRight
                    if (checkAndLoadChunk(topChunk, activatedChunks)) {
                        worldMapData.chunkLookUpTable[topChunk]?.let { topChunkInfo -> checkAndLoadChunk(topChunkInfo.chunkRight, activatedChunks) }
                    }
                    if (checkAndLoadChunk(rightChunk, activatedChunks)) {
                        worldMapData.chunkLookUpTable[rightChunk]?.let { rightChunkInfo -> checkAndLoadChunk(rightChunkInfo.chunkTop, activatedChunks) }
                    }
                    worldMapData.loadingTopRightChunk = false
                }
            }
            ViewPortPosition.BOTTOM_LEFT -> if (!worldMapData.loadingBottomLeftChunk) {
                worldMapData.loadingBottomLeftChunk = true
                views.launchAsap {
                    val bottomChunk = currentChunkInfo.chunkBottom
                    val leftChunk = currentChunkInfo.chunkLeft
                    if (checkAndLoadChunk(bottomChunk, activatedChunks)) {
                        worldMapData.chunkLookUpTable[bottomChunk]?.let { bottomChunkInfo -> checkAndLoadChunk(bottomChunkInfo.chunkLeft, activatedChunks) }
                    }
                    if (checkAndLoadChunk(leftChunk, activatedChunks)) {
                        worldMapData.chunkLookUpTable[leftChunk]?.let { leftChunkInfo -> checkAndLoadChunk(leftChunkInfo.chunkBottom, activatedChunks) }
                    }
                    worldMapData.loadingBottomLeftChunk = false
                }
            }
            ViewPortPosition.BOTTOM_RIGHT -> if (!worldMapData.loadingBottomRightChunk) {
                worldMapData.loadingBottomRightChunk = true
                views.launchAsap {
                    val bottomChunk = currentChunkInfo.chunkBottom
                    val rightChunk = currentChunkInfo.chunkRight
                    if (checkAndLoadChunk(bottomChunk, activatedChunks)) {
                        worldMapData.chunkLookUpTable[bottomChunk]?.let { bottomChunkInfo -> checkAndLoadChunk(bottomChunkInfo.chunkRight, activatedChunks) }
                    }
                    if (checkAndLoadChunk(rightChunk, activatedChunks)) {
                        worldMapData.chunkLookUpTable[rightChunk]?.let { rightChunkInfo -> checkAndLoadChunk(rightChunkInfo.chunkBottom, activatedChunks) }
                    }
                    worldMapData.loadingBottomRightChunk = false
                }
            }
        }
    }

    private suspend fun checkAndLoadChunk(chunk: Int, activatedChunks: MutableSet<Int>) : Boolean =
        if (chunk > 0) {
            if (!worldMapData.chunkLookUpTable.contains(chunk)) {
                val worldName = gameStateManager.config.worldName
                assetStore.loader.loadWorldChunkAssets(worldName, chunk)
                // Spawn entities
                activatedChunks.add(chunk)
                worldMapData.chunkLookUpTable[chunk]?.let { chunkInfo ->
                    chunkInfo.entitiesToBeSpawned.forEach { entityConfigName ->
                        println("Chunk entity to create: $entityConfigName")
                        world.createAndConfigureEntity(entityConfigName)
                    }
                } ?: println("ERROR: WorldMapData - No chunk mesh found for chunk index '$chunk' for spawning entities!")
            }
            true
        } else false

}
