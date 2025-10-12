package korlibs.korge.fleks.assets

import korlibs.datastructure.Array2
import korlibs.datastructure.IntArray2
import korlibs.image.tiles.*
import korlibs.korge.fleks.assets.data.ldtk.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.fleks.prefab.data.LevelData.*
import korlibs.korge.fleks.prefab.Prefab
import korlibs.korge.fleks.prefab.data.LevelData
import korlibs.math.*
import korlibs.memory.*
import kotlinx.serialization.*
import kotlin.collections.get
import kotlin.collections.set
import kotlin.math.*



/**
 * Data class for storing level maps and entities for a game world.
 */
class AssetLevelDataLoader(
    private val assetStore: AssetStore
) {
    private var collisionLayerName: String = ""
    /**
     * Load level data from LDtk world file and store it in the worldData object.
     * The worldData object contains all level data and is used to render the levels.
     * The level data is stored in a 2D array where each element is a LevelData object.
     * The LevelData object contains the tile map data and entity data for each level.
     */
    fun loadLevelData(ldtkWorld: LdtkWorld, collisionLayerName: String, levelName: String, tileSetPaths: MutableList<String>) {
        this.collisionLayerName = collisionLayerName

        // TODO: Add sanity check for level data chunks and throw an error if the LDtk is not configured correctly

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

        // Create list of paths for tilesets - used for hot-reloading of tile maps
        ldtkWorld.ldtk.defs.tilesets.forEach { tileset ->
            tileset.relPath?.let { path ->
                tileSetPaths.add(path)
            }
        }
        
        // Calculate the size of the level grid vania array
        val gridVaniaWidth = (worldWidth / levelWidth) + 1  // +1 for guard needed in world map renderer
        val gridVaniaHeight = (worldHeight / levelHeight) + 1

        Prefab.levelName = levelName
        Prefab.levelData = LevelData(
            // Set the size of the world
            width = worldWidth.toFloat(),
            height = worldHeight.toFloat(),
            // Set the size of a level chunk in the grid vania array in tiles
            levelGridWidth = levelWidth / ldtkWorld.ldtk.defaultGridSize,
            levelGridHeight = levelHeight / ldtkWorld.ldtk.defaultGridSize,
            // Set the size of a grid cell (tile) in pixels
            tileSize = ldtkWorld.ldtk.defaultGridSize,
            // We store the level data config in a 2D array depending on its gridvania position in the world
            // Then later we will spawn the entities depending on the level which the player is currently in
            levelGridVania = Array2(gridVaniaWidth, gridVaniaHeight) { Chunk() },
            gridVaniaWidth = gridVaniaWidth,
            gridVaniaHeight = gridVaniaHeight
        )

        // Save TileMapData for each Level and layer combination from LDtk world
        ldtkWorld.ldtk.levels.forEach { ldtkLevel ->
            loadLevel(ldtkWorld, ldtkLevel, collisionLayerName)
        }
        println("Gridvania size: ${gridVaniaWidth} x ${gridVaniaHeight})")
    }

    /**
     * Reload all chunks (LDtk levels) from the level (LDtk world).
     * This is used when the LDtk world file has been changed (hot-reloading).
     */
    fun reloadAllLevelChunks(ldtkWorld: LdtkWorld) {
        // Reload all levels from ldtk world file
        ldtkWorld.ldtk.levels.forEach { ldtkLevel ->
            loadLevel(ldtkWorld, ldtkLevel, collisionLayerName)
        }
    }

    /**
     * Load a level from the LDtk world file and store it in the levelData object.
     * The level data is stored in a 2D array where each element is a LevelData object.
     * The LevelData object contains the tile map data and entity data for each level.
     */
    private fun loadLevel(ldtkWorld: LdtkWorld, ldtkLevel: Level, collisionLayerName: String) {
        val levelX: Int = ldtkLevel.worldX / (Prefab.levelData!!.levelGridWidth * Prefab.levelData!!.tileSize)
        val levelY: Int = ldtkLevel.worldY / (Prefab.levelData!!.levelGridHeight * Prefab.levelData!!.tileSize)

        val entities: MutableList<String> = mutableListOf()
        val tileMapData: MutableMap<String, TileMapData> = mutableMapOf()
        var collisionMap: IntArray? = null
        var gameObjectCnt = 0

        val chunkName = ldtkLevel.identifier
        ldtkLevel.layerInstances?.forEach { ldtkLayer ->
            val layerName = ldtkLayer.identifier
            val levelWidth = Prefab.levelData!!.levelGridWidth * Prefab.levelData!!.tileSize
            val levelHeight = Prefab.levelData!!.levelGridHeight * Prefab.levelData!!.tileSize
            val gridSize = Prefab.levelData!!.tileSize

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
                        //println("\n$yamlString")
                        //println("entity grid pos x: ${(levelWidth * levelX) + (entity.gridPos.x * gridSize)}")
                        //println("entity grid pos y: ${(levelWidth * levelX) + (entity.gridPos.y * gridSize)}")

                        try {
                            // By deserializing the YAML string we get an EntityConfig object which itself registers in the EntityFactory
                            val entityConfig: EntityConfig =
                                assetStore.loader.configSerializer.yaml().decodeFromString(yamlString.toString())

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

        val levelData = Prefab.levelData!!.levelGridVania[levelX, levelY]
        levelData.entityConfigNames = entities.ifEmpty { null }
        levelData.tileMapData = tileMapData
        levelData.collisionMap = if (collisionMap != null) IntArray2(Prefab.levelData!!.levelGridWidth, Prefab.levelData!!.levelGridWidth, collisionMap) else null
    }

    /**
     * Get the level data for a specific chunk in the grid vania array.
     * The chunk is identified by its X and Y position in the grid vania array.
     */
    private fun storeTiles(ldtkLayer: LayerInstance, tilesetExt: ExtTileset) : TileMapData {
        val tileMapData = TileMapData(
            width = ldtkLayer.cWid,
            height = ldtkLayer.cHei,
            tileSet = tilesetExt.tileset
        )
        val gridSize = tilesetExt.def.tileGridSize

        for (tile in ldtkLayer.autoLayerTiles + ldtkLayer.gridTiles) {
            val (px, py) = tile.px
            val x = px / gridSize
            val y = py / gridSize
            val dx = px % gridSize
            val dy = py % gridSize
            val tileId = tile.t
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
