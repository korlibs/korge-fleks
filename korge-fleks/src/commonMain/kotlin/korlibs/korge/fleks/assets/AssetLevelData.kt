package korlibs.korge.fleks.assets

import korlibs.image.tiles.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.ldtk.*
import korlibs.korge.ldtk.view.*
import korlibs.math.*
import korlibs.memory.*
import kotlinx.serialization.*
import kotlin.math.*

class AssetLevelData {

    internal val worldData = WorldData()
    // TODO: move into WorldData
    internal val levelDataMaps: MutableMap<String, LevelData> = mutableMapOf()
    internal val configDeserializer = EntityConfigSerializer()

    private var gameObjectCnt = 0

    private var sizeX: Int = 0
    private var sizeY: Int = 0
    private var gridVaniaWidth: Int = 0
    private var gridVaniaHeight: Int = 0
    /**
     * @param hasParallax - This level uses a parallax background, so we need to set the world size accordingly.
     *  TODO check if we need this also later - do we have only one active world level?
     */
    fun loadLevelData(ldtkWorld: LDTKWorld, type: AssetType, hasParallax: Boolean) {
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
        sizeX = (maxLevelOffsetX / gridVaniaWidth) + 1
        sizeY = (maxLevelOffsetY / gridVaniaHeight) + 1

        // Set the size of the world
        worldData.width = (sizeX * gridVaniaWidth).toFloat()
        worldData.height = (sizeY * gridVaniaHeight).toFloat()

        if (maxLevelOffsetX == 0) println("WARNING: Level width is 0!")

        // Save TileMapData for each Level and layer combination from LDtk world
        ldtkWorld.ldtk.levels.forEach { ldtkLevel ->
            val globalLevelPosX = ldtkLevel.worldX
            val globalLevelPosY = ldtkLevel.worldY
            val levelX: Int = globalLevelPosX / gridVaniaWidth
            val levelY: Int = globalLevelPosY / gridVaniaHeight

            loadLevel(worldData, levelX, levelY, ldtkWorld, ldtkLevel, type)
        }
        println("Gridvania size: $sizeX x $sizeY)")
    }

    fun reloadAsset(ldtkWorld: LDTKWorld, type: AssetType) {
        // Reload all levels from ldtk world file
        ldtkWorld.ldtk.levels.forEach { ldtkLevel ->
            ldtkLevel.layerInstances?.forEach { ldtkLayer ->
                val tilesetExt = ldtkWorld.tilesetDefsById[ldtkLayer.tilesetDefUid]

                if (tilesetExt != null) {
                    // Get index of level in the worldData Grid vania array
                    val levelX: Int = ldtkLevel.worldX / gridVaniaWidth
                    val levelY: Int = ldtkLevel.worldY / gridVaniaHeight

                    storeTiles(worldData.levelGridVania[levelX][levelY], ldtkLayer, tilesetExt, ldtkLevel.identifier, ldtkLayer.identifier, type)
                    println("\nTriggering asset change for LDtk level : ${ldtkLevel.identifier}_${ldtkLayer.identifier}")
                }
            }
        }
    }

    fun removeAssets(type: AssetType) {
        levelDataMaps.values.removeAll { it.type == type }
    }

    private fun loadLevel(worldData: WorldData, levelX: Int, levelY: Int, ldtkWorld: LDTKWorld, ldtkLevel: Level, type: AssetType) {
        val levelName = ldtkLevel.identifier

        ldtkLevel.layerInstances?.forEach { ldtkLayer ->
            val layerName = ldtkLayer.identifier
            val gridSize = ldtkLayer.gridSize

            if (!worldData.levelMapsPerLayer.contains(layerName)) {
                worldData.levelMapsPerLayer[layerName] = LevelMap(
                    levelGridVania = List(sizeX) { List(sizeY) { LevelData() } }
                )
            }

            // Get level data from worldData
            val levelData = worldData.levelMapsPerLayer[layerName]!!.levelGridVania[levelX][levelY]

            // Check if layer contains entity data -> create EntityConfigs and store them fo
            if (ldtkLayer.entityInstances.isNotEmpty()) {
                ldtkLayer.entityInstances.forEach { entity ->
                    // Create YAML string of an entity config from LDtk
                    val yamlString = StringBuilder()
                    // Sanity check - entity needs to have a field 'entityConfig'
                    if (entity.fieldInstances.firstOrNull { it.identifier == "entityConfig" } != null) {

                        if (entity.tags.firstOrNull { it == "unique" } != null) {
                            // Add scripts without unique count value - they are unique by name because they exist only once
                            yamlString.append("name: ${levelName}_${entity.identifier}\n")
                        } else {
                            // Add other game objects with a unique name as identifier
                            yamlString.append("name: ${levelName}_${entity.identifier}_${gameObjectCnt++}\n")
                        }

                        // Add position of entity = (level position in the world) + (grid position within the level) + (pivot point)
                        // TODO: Take level position in world into account
                        val entityPosX: Int = /*levelPosX +*/
                            (entity.gridPos.x * gridSize) + (entity.pivot[0] * gridSize).toInt()
                        val entityPosY: Int = /*levelPosY +*/
                            (entity.gridPos.y * gridSize) + (entity.pivot[1] * gridSize).toInt()


                        // Add position of entity
                        entity.tags.firstOrNull { it == "positionable" }?.let {
                            yamlString.append("x: $entityPosX\n")
                            yamlString.append("y: $entityPosY\n")
                        }

                        // Add all other fields of entity
                        entity.fieldInstances.forEach { field ->
                            if (field.identifier != "EntityConfig") yamlString.append("${field.identifier}: ${field.value}\n")
                        }
                        println("INFO: Game object '${entity.identifier}' loaded for '$levelName'")
                        println("\n$yamlString")

                        try {
                            // By deserializing the YAML string we get an EntityConfig object which itself registers in the EntityFactory
                            val entityConfig: EntityConfig =
                                configDeserializer.yaml().decodeFromString(yamlString.toString())

                            // TODO: We need to store only the name of the entity config for later dynamically spawning of entities
                            //       We need to store the entity configs in a 2D array depending on its position in the level
                            //       Then later we will spawn the entities depending on the position in the level
                            levelData.entities.add(entityConfig.name)

                            println("INFO: Registering entity config '${entity.identifier}' for '$levelName'")
                        } catch (e: Throwable) {
                            println("ERROR: Loading entity config - $e")
                        }

                    } else println("ERROR: Game object with name '${entity.identifier}' has no field entityConfig!")
                }

                //
                levelData.width = (ldtkLayer.cWid * gridSize).toFloat()
                levelData.height = (ldtkLayer.cHei * gridSize).toFloat()

                // TODO remove later
                // Create new level data if it does not exist yet
                if (!levelDataMaps.contains(levelName)) {
                    levelDataMaps[levelName] = levelData
                } else {
                    levelDataMaps[levelName]!!.entities
                }
            }

            // Check if layer has tile set -> store tile map data
            val tilesetExt = ldtkWorld.tilesetDefsById[ldtkLayer.tilesetDefUid]
            if (tilesetExt != null) {
                storeTiles(levelData, ldtkLayer, tilesetExt, levelName, layerName, type)
            }
        }

    }

    // TODO: remove "level"
    private fun storeTiles(levelData: LevelData, ldtkLayer: LayerInstance, tilesetExt: ExtTileset, level: String, layer: String, type: AssetType) {
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
        levelData.layerTileMaps[layer] = tileMapData
        levelData.tileMapData = tileMapData

        // TODO: remove below lines
        // Create new map for level layers and store layer in it
        val layerTileMaps = mutableMapOf<String, TileMapData>()
        layerTileMaps[layer] = tileMapData
        // Add layer map to level Maps
        if (!levelDataMaps.contains(level)) {
            val levelData = LevelData(
                type = type,
                gridSize = gridSize,
                width = (ldtkLayer.cWid * gridSize).toFloat(),
                height = (ldtkLayer.cHei * gridSize).toFloat(),
                layerTileMaps = layerTileMaps,
                tileMapData = tileMapData
            )
            levelDataMaps[level] = levelData
        } else {
            levelDataMaps[level]!!.layerTileMaps[layer] = tileMapData
        }
    }

    /**
     *
     * @param tileSize - Size of a grid cell in pixels (e.g. 16 for 16x16 tile size)
     * @param width - Width of whole level in pixels
     * @param height - Height of whole level in pixels
     */
    data class WorldData(
        var tileSize: Int = 16,

        var width: Float = 0f,
        var height: Float = 0f,
        var levelWidth: Int = 0,  // TODO: Check if we need it outside of this class - level renderer
        var levelHeight: Int = 0,

        var levelGridVania: List<List<LevelData>> = listOf(),  // TODO: Remove
        var levelMapsPerLayer: MutableMap<String, LevelMap> = mutableMapOf()
        )

    data class LevelMap(
        val entities: MutableList<String> = mutableListOf(),  // TODO change to list
        var levelGridVania: List<List<LevelData>> = listOf()
    ) {
        fun get(x: Int, y: Int, stackLayer: Int): Tile {
            // TODO - getter function might not be enough since stack layer could be different between levels

            return levelGridVania[0][0].tileMapData!![x, y, stackLayer]  // TODO
        }
    }

    // Data class for storing level data like grizSize, width, height, entities, tileMapData
    data class LevelData(
        var type: AssetType = AssetType.COMMON,  // TODO: Remove
        val gridSize: Int = 16,  // TODO: Remove
        var width: Float = 0f,
        var height: Float = 0f,

        val entities: MutableList<String> = mutableListOf(),  // TODO remove
        val layerTileMaps: MutableMap<String, TileMapData> = mutableMapOf(),  // TODO remove
        var tileMapData: TileMapData? = null
    )
}
