package korlibs.korge.fleks.assets

import korlibs.datastructure.Array2
import korlibs.image.tiles.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.fleks.assets.WorldData.*
import korlibs.korge.fleks.gameState.*
import korlibs.korge.ldtk.*
import korlibs.korge.ldtk.view.*
import korlibs.math.*
import korlibs.memory.*
import kotlinx.serialization.*
import kotlin.math.*

/**
 * Data class for storing level maps and entities for a game world.
 */
class AssetLevelData(
    val ldtkWorld: LDTKWorld,  // TODO: Get tileset names out and store separately for hot-reloading
    private val collisionLayerName: String
) {
    internal var worldData: WorldData
    private var gameObjectCnt = 0

    /**
     * Load level data from LDtk world file and store it in the worldData object.
     * The worldData object contains all level data and is used to render the levels.
     * The level data is stored in a 2D array where each element is a LevelData object.
     * The LevelData object contains the tile map data and entity data for each level.
     */
    init {
        val levelWidth = ldtkWorld.ldtk.worldGridWidth ?: 1  // this is also the size of each sub-level
        val levelHeight = ldtkWorld.ldtk.worldGridHeight ?: 1  // all levels have the same size
        var worldWidth = 0
        var worldHeight = 0

        // Get the highest values for X and Y axis - this will be the size of the world
        ldtkWorld.ldtk.levels.forEach { ldtkLevel ->
            if (worldWidth < ldtkLevel.worldX) worldWidth = ldtkLevel.worldX
            if (worldHeight < ldtkLevel.worldY) worldHeight = ldtkLevel.worldY
        }
        worldWidth += levelWidth
        worldHeight += levelHeight

        // Calculate the size of the level grid vania array
        val gridVaniaWidth = (worldWidth / levelWidth) + 1  // +1 for guard needed in world map renderer
        val gridVaniaHeight = (worldHeight / levelHeight) + 1

        worldData = WorldData(
            // Set the size of the world
            width = worldWidth.toFloat(),
            height = worldHeight.toFloat(),
            // Set the size of a level in the grid vania array in tiles
            levelGridWidth = levelWidth / ldtkWorld.ldtk.defaultGridSize,
            levelGridHeight = levelHeight / ldtkWorld.ldtk.defaultGridSize,
            // Set the size of a grid cell in pixels
            tileSize = ldtkWorld.ldtk.defaultGridSize,
            // We store the level data config in a 2D array depending on its gridvania position in the world
            // Then later we will spawn the entities depending on the level which the player is currently in
            levelGridVania = Array2(gridVaniaWidth, gridVaniaHeight) { Chunk() },
            gridVaniaWidth = gridVaniaWidth,
            gridVaniaHeight = gridVaniaHeight
        )

        // Save TileMapData for each Level and layer combination from LDtk world
        ldtkWorld.ldtk.levels.forEach { ldtkLevel ->
            loadLevel(ldtkWorld, ldtkLevel)
        }
        println("Gridvania size: ${gridVaniaWidth} x ${gridVaniaHeight})")
    }

    fun reloadAsset(ldtkWorld: LDTKWorld) {
        // Reload all levels from ldtk world file
        ldtkWorld.ldtk.levels.forEach { ldtkLevel ->
            loadLevel(ldtkWorld, ldtkLevel)
        }
    }

    private fun loadLevel(ldtkWorld: LDTKWorld, ldtkLevel: Level) {
        val levelX: Int = ldtkLevel.worldX / (worldData.levelGridWidth * worldData.tileSize)
        val levelY: Int = ldtkLevel.worldY / (worldData.levelGridHeight * worldData.tileSize)

        val entities: MutableList<String> = mutableListOf()
        val tileMapData: MutableMap<String, TileMapData> = mutableMapOf()
        var collisionMap: IntArray? = null

        val chunkName = ldtkLevel.identifier
        ldtkLevel.layerInstances?.forEach { ldtkLayer ->
            val layerName = ldtkLayer.identifier
            val levelWidth = worldData.levelGridWidth * worldData.tileSize
            val levelHeight = worldData.levelGridHeight * worldData.tileSize
            val gridSize = worldData.tileSize

            // TODO: Check if we want layer to be considered for platform collision

            // in ldtkLayer one of below data sets are defined:
            //   - Entity layer:
            //       entityInstances - List of entities (game objects) in the level
            //   - Highlight layer: gridTiles - List of manually placed tiles in the level
            //   - Playfield layer:
            //       autoLayerTiles - List of tiles set by auto-tile rules in the layer
            //       intGridCSV - List of all values in the IntGrid layer, stored in CSV format and used as collision input
            //   - Background layer:
            //       autoLayerTiles - List of tiles set by auto-tile rules in the layer


            // Check if layer contains entity data -> create EntityConfigs and store them fo
            if (ldtkLayer.entityInstances.isNotEmpty()) {
                ldtkLayer.entityInstances.forEach { entity ->
                    // Create YAML string of an entity config from LDtk
                    val yamlString = StringBuilder()
                    // Sanity check - entity needs to have a field 'entityConfig'
                    if (entity.fieldInstances.firstOrNull { it.identifier == "entityConfig" } != null) {
                        if (entity.tags.firstOrNull { it == "unique" } != null) {
                            // Add scripts without unique count value - they are unique by name because they exist only once
                            yamlString.append("name: ${entity.identifier}\n")
                        } else {
                            // Add other game objects with a unique name as identifier
                            yamlString.append("name: ${chunkName}_${entity.identifier}_${gameObjectCnt++}\n")
                        }

                        // Add position of entity = (chunk position in the level) + (position within the chunk) + (pivot point)
                        val entityPosX: Int = (levelWidth * levelX) + entity.pixelPos.x
                        val entityPosY: Int = (levelHeight * levelY) + entity.pixelPos.y

                        // Add position of entity
                        entity.tags.firstOrNull { it == "positionable" }?.let {
                            yamlString.append("x: $entityPosX\n")
                            yamlString.append("y: $entityPosY\n")
                            yamlString.append("anchorX: ${entity.pivotAnchor.sx * entity.width}\n")
                            yamlString.append("anchorY: ${entity.pivotAnchor.sy * entity.height}\n")
                        }

                        // Add all other fields of entity
                        entity.fieldInstances.forEach { field ->
                            if (field.identifier != "EntityConfig") yamlString.append("${field.identifier}: ${field.value}\n")
                        }
                        println("INFO: Game object '${entity.identifier}' loaded for '$chunkName'")
                        println("\n$yamlString")
                        println("entity grid pos x: ${(levelWidth * levelX) + (entity.gridPos.x * gridSize)}")
                        println("entity grid pos y: ${(levelWidth * levelX) + (entity.gridPos.y * gridSize)}")

                        try {
                            // By deserializing the YAML string we get an EntityConfig object which itself registers in the EntityFactory
                            val entityConfig: EntityConfig =
                                GameStateManager.configSerializer.yaml().decodeFromString(yamlString.toString())

                            // We need to store only the name of the entity config for later dynamically spawning of entities
                            if (entity.tags.firstOrNull { it == "unique" } == null) {
                                // Do not add unique entities to the list of entities - they are spawn separately
                                entities.add(entityConfig.name)
                            }
                            //println("INFO: Registering entity config '${entity.identifier}' for '$levelName'")
                        } catch (e: Throwable) {
                            println("ERROR: Loading entity config - $e")
                        }

                    } else println("ERROR: Game object with name '${entity.identifier}' has no field entityConfig!")
                }
            }

            // Store tiles into tileMapData for each layer
            if (ldtkWorld.tilesetDefsById[ldtkLayer.tilesetDefUid] != null) {
                // Layer has tile set -> store tile map data - no entity data
                tileMapData[layerName] = storeTiles(ldtkLayer, ldtkWorld.tilesetDefsById[ldtkLayer.tilesetDefUid]!!)
            }

            // Store collision data for Playfield layer
            if (layerName == collisionLayerName) collisionMap = ldtkLayer.intGridCSV
        }

        val levelData = worldData.levelGridVania[levelX, levelY]
        levelData.entityConfigNames = entities.ifEmpty { null }
        levelData.tileMapData = tileMapData
        levelData.collisionMap = collisionMap
    }

    private fun storeTiles(ldtkLayer: LayerInstance, tilesetExt: ExtTileset) : TileMapData {
        val tileMapData = TileMapData(
            width = ldtkLayer.cWid,
            height = ldtkLayer.cHei,
            tileSet = if (tilesetExt.tileset != null) tilesetExt.tileset!! else TileSet.EMPTY
        )
        val gridSize = tilesetExt.def.tileGridSize
        val tilesetWidth = tilesetExt.def.pxWid
        val cellsTilesPerRow = tilesetWidth / gridSize

        for (tile in ldtkLayer.autoLayerTiles + ldtkLayer.gridTiles) {
            val (px, py) = tile.px
            val x = px / gridSize
            val y = py / gridSize
            val (tileX, tileY) = tile.src
            val dx = px % gridSize
            val dy = py % gridSize
            val tx = tileX / gridSize
            val ty = tileY / gridSize
            val tileId = ty * cellsTilesPerRow + tx
            val flipX = tile.f.hasBitSet(0)
            val flipY = tile.f.hasBitSet(1)

            // Get stack level depending on if the tile overlaps its neighbour cells i.e. the tile has an offset (dx, dy)
            when {
                (dx == 0 && dy == 0) -> {
                    val stackLevel = tileMapData.data.getStackLevel(x, y)
                    tileMapData.data.set(x, y, stackLevel, value = Tile(tile = tileId, offsetX = dx, offsetY = dy, flipX = flipX, flipY = flipY, rotate = false).raw)
                }

                (dx == 0 && dy != 0) -> {
                    val stackLevel = max(tileMapData.data.getStackLevel(x, y), tileMapData.data.getStackLevel(x, y + 1))
                    tileMapData.data.set(x, y, stackLevel, value = Tile(tile = tileId, offsetX = dx, offsetY = dy, flipX = flipX, flipY = flipY, rotate = false).raw)
                    tileMapData.data.set(x, y + 1, stackLevel, value = Tile.ZERO.raw)
                }

                (dx != 0 && dy == 0) -> {
                    val stackLevel = max(tileMapData.data.getStackLevel(x, y), tileMapData.data.getStackLevel(x + 1, y))
                    tileMapData.data.set(x, y, stackLevel, value = Tile(tile = tileId, offsetX = dx, offsetY = dy, flipX = flipX, flipY = flipY, rotate = false).raw)
                    tileMapData.data.set(x + 1, y, stackLevel, value = Tile.ZERO.raw)
                }

                else -> {
                    val stackLevel = max(tileMapData.data.getStackLevel(x, y), tileMapData.data.getStackLevel(x, y + 1), tileMapData.data.getStackLevel(x + 1, y), tileMapData.data.getStackLevel(x + 1, y + 1))
                    tileMapData.data.set(x, y, stackLevel, value = Tile(tile = tileId, offsetX = dx, offsetY = dy, flipX = flipX, flipY = flipY, rotate = false).raw)
                    tileMapData.data.set(x, y + 1, stackLevel, value = Tile.ZERO.raw)
                    tileMapData.data.set(x + 1, y, stackLevel, value = Tile.ZERO.raw)
                    tileMapData.data.set(x + 1, y + 1, stackLevel, value = Tile.ZERO.raw)
                }
            }
        }
        return tileMapData
    }
}
