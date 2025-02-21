package korlibs.korge.fleks.assets

import korlibs.image.tiles.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.fleks.assets.WorldData.*
import korlibs.korge.ldtk.*
import korlibs.korge.ldtk.view.*
import korlibs.math.*
import korlibs.memory.*
import kotlinx.serialization.*
import kotlin.math.*

/**
 * Data class for storing level maps and entities for a game world.
 */
class AssetLevelData {
    internal val worldData = WorldData()
    internal val configDeserializer = EntityConfigSerializer()
    private var gameObjectCnt = 0

    // Size of a level within the gridvania array in pixels
    private var gridVaniaWidth: Int = 0
    private var gridVaniaHeight: Int = 0

    /**
     * Load level data from LDtk world file and store it in the worldData object.
     * The worldData object contains all level data and is used to render the levels.
     * The level data is stored in a 2D array where each element is a LevelData object.
     * The LevelData object contains the tile map data and entity data for each level.
     *
     * @param ldtkWorld - LDtk world object containing all level data
     *
     * @see WorldData
     */
    fun loadLevelData(ldtkWorld: LDTKWorld) {
        gridVaniaWidth = ldtkWorld.ldtk.worldGridWidth ?: 1  // this is also the size of each sub-level
        gridVaniaHeight = ldtkWorld.ldtk.worldGridHeight ?: 1
        var maxLevelOffsetX = 0
        var maxLevelOffsetY = 0

        // Get the highest values for X and Y axis - this will be the size of the grid vania array
        ldtkWorld.ldtk.levels.forEach { ldtkLevel ->
            if (maxLevelOffsetX < ldtkLevel.worldX) maxLevelOffsetX = ldtkLevel.worldX
            if (maxLevelOffsetY < ldtkLevel.worldY) maxLevelOffsetY = ldtkLevel.worldY
        }

        // Create grid vania array
        val gridWidth = (maxLevelOffsetX / gridVaniaWidth) + 1
        val gridHeight = (maxLevelOffsetY / gridVaniaHeight) + 1

        // Set the size of the world
        worldData.width = (gridWidth * gridVaniaWidth).toFloat()
        worldData.height = (gridHeight * gridVaniaHeight).toFloat()

        // Set the size of a grid cell in pixels
        worldData.gridSize = ldtkWorld.ldtk.defaultGridSize

        // Set the size of a level in the grid vania array
        worldData.levelWidth = gridVaniaWidth / worldData.gridSize
        worldData.levelHeight = gridVaniaHeight / worldData.gridSize


        if (maxLevelOffsetX == 0) println("WARNING: Level width is 0!")

        // Save TileMapData for each Level and layer combination from LDtk world
        ldtkWorld.ldtk.levels.forEach { ldtkLevel ->
            val levelX: Int = ldtkLevel.worldX / gridVaniaWidth
            val levelY: Int = ldtkLevel.worldY / gridVaniaHeight
            loadLevel(worldData, levelX, levelY, ldtkWorld, ldtkLevel)
        }
        println("Gridvania size: ${gridWidth} x ${gridHeight})")
    }

    fun reloadAsset(ldtkWorld: LDTKWorld) {
        // Reload all levels from ldtk world file
        ldtkWorld.ldtk.levels.forEach { ldtkLevel ->
            val levelX: Int = ldtkLevel.worldX / gridVaniaWidth
            val levelY: Int = ldtkLevel.worldY / gridVaniaHeight
            loadLevel(worldData, levelX, levelY, ldtkWorld, ldtkLevel)
        }
    }

    private fun loadLevel(worldData: WorldData, levelX: Int, levelY: Int, ldtkWorld: LDTKWorld, ldtkLevel: Level) {
        val levelName = ldtkLevel.identifier
        ldtkLevel.layerInstances?.forEach { ldtkLayer ->
            val layerName = ldtkLayer.identifier
            val gridSize = ldtkLayer.gridSize
            val entities: MutableList<String> = mutableListOf()


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
                            yamlString.append("name: ${levelName}_${entity.identifier}_${gameObjectCnt++}\n")
                        }

                        // Add position of entity = (level position in the world) + (position within the level) + (pivot point)
                        val entityPosX: Int = (gridVaniaWidth * levelX) + (entity.gridPos.x * gridSize) + (entity.pivot[0] * gridSize).toInt()
                        val entityPosY: Int = (gridVaniaHeight * levelY) + (entity.gridPos.y * gridSize) + (entity.pivot[1] * gridSize).toInt()

                        // Add position of entity
                        entity.tags.firstOrNull { it == "positionable" }?.let {
                            yamlString.append("x: $entityPosX\n")
                            yamlString.append("y: $entityPosY\n")
                        }

                        // Add all other fields of entity
                        entity.fieldInstances.forEach { field ->
                            if (field.identifier != "EntityConfig") yamlString.append("${field.identifier}: ${field.value}\n")
                        }
                        //println("INFO: Game object '${entity.identifier}' loaded for '$levelName'")
                        //println("\n$yamlString")

                        try {
                            // By deserializing the YAML string we get an EntityConfig object which itself registers in the EntityFactory
                            val entityConfig: EntityConfig =
                                configDeserializer.yaml().decodeFromString(yamlString.toString())

                            // We need to store only the name of the entity config for later dynamically spawning of entities
                            entities.add(entityConfig.name)
                            //println("INFO: Registering entity config '${entity.identifier}' for '$levelName'")
                        } catch (e: Throwable) {
                            println("ERROR: Loading entity config - $e")
                        }

                    } else println("ERROR: Game object with name '${entity.identifier}' has no field entityConfig!")
                }
            }

            if (!worldData.layerlevelMaps.contains(layerName)) {
                // We store the entity configs in a 2D array depending on its gridvania position in the world
                // Then later we will spawn the entities depending on the level which the player is currently in
                val gridWidth = (worldData.width.toInt() / gridVaniaWidth) + 1  // +1 for guard needed in world map renderer
                val gridHeight = (worldData.height.toInt() / gridVaniaHeight) + 1
                worldData.layerlevelMaps[layerName] = LevelMap(levelGridVania = List(gridWidth) { List(gridHeight) { LevelData() } })
            }

            val levelData = worldData.layerlevelMaps[layerName]!!.levelGridVania[levelX][levelY]

            if (entities.isNotEmpty()) {
                // Layer has entities -> store entity data - no tile data
                levelData.entities = entities
            } else if (ldtkWorld.tilesetDefsById[ldtkLayer.tilesetDefUid] != null) {
                // Layer has tile set -> store tile map data - no entity data
                storeTiles(levelData, ldtkLayer, ldtkWorld.tilesetDefsById[ldtkLayer.tilesetDefUid]!!)
            } else {
                println("WARNING: Layer '$layerName' of level '$levelName' has no tile set or entities!")
            }
        }
    }

    private fun storeTiles(levelData: LevelData, ldtkLayer: LayerInstance, tilesetExt: ExtTileset) {
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
                    tileMapData.data.set(
                        x,
                        y,
                        stackLevel,
                        value = Tile(
                            tile = tileId,
                            offsetX = dx,
                            offsetY = dy,
                            flipX = flipX,
                            flipY = flipY,
                            rotate = false
                        ).raw
                    )
                }

                (dx == 0 && dy != 0) -> {
                    val stackLevel = max(tileMapData.data.getStackLevel(x, y), tileMapData.data.getStackLevel(x, y + 1))
                    tileMapData.data.set(
                        x,
                        y,
                        stackLevel,
                        value = Tile(
                            tile = tileId,
                            offsetX = dx,
                            offsetY = dy,
                            flipX = flipX,
                            flipY = flipY,
                            rotate = false
                        ).raw
                    )
                    tileMapData.data.set(x, y + 1, stackLevel, value = Tile.ZERO.raw)
                }

                (dx != 0 && dy == 0) -> {
                    val stackLevel = max(tileMapData.data.getStackLevel(x, y), tileMapData.data.getStackLevel(x + 1, y))
                    tileMapData.data.set(
                        x,
                        y,
                        stackLevel,
                        value = Tile(
                            tile = tileId,
                            offsetX = dx,
                            offsetY = dy,
                            flipX = flipX,
                            flipY = flipY,
                            rotate = false
                        ).raw
                    )
                    tileMapData.data.set(x + 1, y, stackLevel, value = Tile.ZERO.raw)
                }

                else -> {
                    val stackLevel = max(
                        tileMapData.data.getStackLevel(x, y),
                        tileMapData.data.getStackLevel(x, y + 1),
                        tileMapData.data.getStackLevel(x + 1, y),
                        tileMapData.data.getStackLevel(x + 1, y + 1)
                    )
                    tileMapData.data.set(
                        x,
                        y,
                        stackLevel,
                        value = Tile(
                            tile = tileId,
                            offsetX = dx,
                            offsetY = dy,
                            flipX = flipX,
                            flipY = flipY,
                            rotate = false
                        ).raw
                    )
                    tileMapData.data.set(x, y + 1, stackLevel, value = Tile.ZERO.raw)
                    tileMapData.data.set(x + 1, y, stackLevel, value = Tile.ZERO.raw)
                    tileMapData.data.set(x + 1, y + 1, stackLevel, value = Tile.ZERO.raw)
                }
            }
        }
        levelData.tileMapData = tileMapData
    }
}
