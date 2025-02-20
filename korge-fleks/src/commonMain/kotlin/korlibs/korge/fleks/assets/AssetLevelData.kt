package korlibs.korge.fleks.assets

import korlibs.image.bitmap.*
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

    // Size of a level within the grid vania array in grid coordinates (index)
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
        worldData.gridWidth = (maxLevelOffsetX / gridVaniaWidth) + 1
        worldData.gridHeight = (maxLevelOffsetY / gridVaniaHeight) + 1

        // Set the size of the world
        worldData.width = (worldData.gridWidth * gridVaniaWidth).toFloat()
        worldData.height = (worldData.gridHeight * gridVaniaHeight).toFloat()

        if (maxLevelOffsetX == 0) println("WARNING: Level width is 0!")

        // Save TileMapData for each Level and layer combination from LDtk world
        ldtkWorld.ldtk.levels.forEach { ldtkLevel ->
            val globalLevelPosX = ldtkLevel.worldX
            val globalLevelPosY = ldtkLevel.worldY
            val levelX: Int = globalLevelPosX / gridVaniaWidth
            val levelY: Int = globalLevelPosY / gridVaniaHeight

            println("Loading level: ${ldtkLevel.identifier} at position: $levelX, $levelY")

            loadLevel(worldData, levelX, levelY, ldtkWorld, ldtkLevel, type)
        }
        println("Gridvania size: ${worldData.gridWidth} x ${worldData.gridHeight})")
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

                    // TODO
//                    storeTiles(worldData.levelGridVania[levelX][levelY], ldtkLayer, tilesetExt, ldtkLevel.identifier, ldtkLayer.identifier, type)
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

            // TODO - we do not need to create a level map for an entity layer!!!

            if (!worldData.layerlevelMaps.contains(layerName)) {
                worldData.layerlevelMaps[layerName] = LevelMap(
                    gridWidth = gridVaniaWidth / gridSize,
                    gridHeight = gridVaniaHeight / gridSize,
                    levelGridVania = List(worldData.gridWidth) { List(worldData.gridHeight) { LevelData() } }
                )
            }

            // Get level data from worldData
            val levelData = worldData.layerlevelMaps[layerName]!!.levelGridVania[levelX][levelY]

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
//                levelData.width = (ldtkLayer.cWid * gridSize).toFloat()
//                levelData.height = (ldtkLayer.cHei * gridSize).toFloat()

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
//        levelData.layerTileMaps[layer] = tileMapData
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
//                width = (ldtkLayer.cWid * gridSize).toFloat(),
//                height = (ldtkLayer.cHei * gridSize).toFloat(),
//                layerTileMaps = layerTileMaps,
                tileMapData = tileMapData
            )
            levelDataMaps[level] = levelData
//        } else {
//            levelDataMaps[level]!!.layerTileMaps[layer] = tileMapData
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

        // Size of the whole world (in pixels)
        var width: Float = 0f,
        var height: Float = 0f,
        var gridWidth: Int = 0,
        var gridHeight: Int = 0,

//        var levelWidth: Int = 0,  // TODO: Check if we need it outside of this class - level renderer
//        var levelHeight: Int = 0,

//        var levelGridVania: List<List<LevelData>> = listOf(),  // TODO: Remove
        var layerlevelMaps: MutableMap<String, LevelMap> = mutableMapOf(),

    ) {

        fun getLevelMap(layerName: String) : LevelMap {
            if (!layerlevelMaps.contains(layerName)) println("WARNING: Level map for layer '$layerName' does not exist!")
            return layerlevelMaps[layerName] ?: LevelMap(1, 1)
        }

//        fun setRenderRect(x: Int, y: Int, width: Int, height: Int) {
//        }
    }

    data class LevelMap(
        // Size of a level inside the grid vania array (all levels have the same size)
        val gridWidth: Int,
        val gridHeight: Int,
        val entities: MutableList<String> = mutableListOf(),  // TODO change to list
        var levelGridVania: List<List<LevelData>> = listOf(),

        // Internal values used for rendering
//        var renderWidth: Int = 0,
//        var renderHeight: Int = 0
    ) {
        /**
         * Get the maximum stack level of all levels within the given view port area
         *
         * @param x - vertical index of top-left tile in grid coordinates
         * @param y - horizontal index of top-left tile in grid coordinates
         * @param width - vertical amount of tiles in view port in grid coordinates
         * @param height - horizontal amount of tiles in view port in grid coordinates
         */
        fun getMaxStackLevel(x: Int, y: Int, width: Int, height: Int): Int {
//            renderWidth = width
//            renderHeight = height
            // Get stack layer depth of all levels within the given view port area
            val xx = x / gridWidth
            val yy = y / gridHeight
            val xx2 = (x + width) / gridWidth
            val yy2 = (y + height) / gridHeight

            // Check position of top-left tile
            var maxStackLevel = levelGridVania[xx][yy].tileMapData?.maxLevel ?: 0
            // Check position of top-right tile
            maxStackLevel = max(maxStackLevel, levelGridVania[xx2][yy].tileMapData?.maxLevel ?: 0)
            // Check position of bottom-left tile
            maxStackLevel = max(maxStackLevel, levelGridVania[xx][yy2].tileMapData?.maxLevel ?: 0)
            // Check position of bottom-right tile
            maxStackLevel = max(maxStackLevel, levelGridVania[xx2][yy2].tileMapData?.maxLevel ?: 0)

//            maxStackLevel = 1
            return maxStackLevel
        }

        fun forEachTile(x: Int, y: Int, width: Int, height: Int, renderCall: (BmpSlice, Float, Float) -> Unit) {
            // Calculate the view port corners (top-left, top-right, bottom-left and bottom-right positions) in gridvania indexes
            // and check if the corners are in different level maps (tileMapData)
            val gridX = x / gridWidth
            val gridY = y / gridHeight
            val gridX2 = (x + width) / gridWidth
            val gridY2 = (y + height) / gridHeight

            val xStart = x % gridWidth
            val yStart = y % gridHeight

            // TODO: Iterate over all levels within the view port area

            levelGridVania[gridX][gridY].tileMapData?.let { tileMap ->
                val tileSet = tileMap.tileSet
                val tileWidth = tileSet.width
                val tileHeight = tileSet.height
                val offsetScale = tileMap.offsetScale

                for (l in 0 until tileMap.maxLevel) {
                    for (tx in xStart until xStart + width) {
                        for (ty in yStart until yStart + height) {
                            val tile = tileMap[tx, ty, l]
                            val tileInfo = tileSet.getInfo(tile.tile)
                            if (tileInfo != null) {
                                val px = (tx * tileWidth) + tile.offsetX
                                val py = (ty * tileHeight) + tile.offsetY
                                renderCall(tileInfo.slice, px.toFloat(), py.toFloat())
                            }
                        }
                    }
                }
            }

            if (gridX2 > gridWidth) {

            }
        }

        private fun

        /**
         * Get the tile at the given position in the virtual world map
         *
         * @param x - horizontal index of tile in grid coordinates
         * @param y - vertical index of tile in grid coordinates
         * @param stackLevel - stack level of the tile (when tiles are stacked on top of each other)
         */
        operator fun get(x: Int, y: Int, stackLevel: Int): Tile {
            val gridX = x / gridWidth
            val girdY = y / gridHeight
            val levelX = x % gridWidth
            val levelY = y % gridHeight

            if (gridX < 0 || gridX >= levelGridVania.size || girdY < 0 || girdY >= levelGridVania[0].size) {
                println("WARNING: Level map for grid vania does not exist!")
            }
            if (levelX < 0 || levelX >= gridWidth || levelY < 0 || levelY >= gridHeight) {
                println("WARNING: Tile position is out of bounds!")
            }

            if (levelGridVania[gridX][girdY].tileMapData == null) return Tile.ZERO
            if (stackLevel >= levelGridVania[gridX][girdY].tileMapData!!.maxLevel) return Tile.ZERO

//            println("levelGridVania: $gridX, $girdY, level: $levelX, $levelY, stack: $stackLayer")
            return levelGridVania[gridX][girdY].tileMapData!![levelX, levelY, stackLevel]
        }

        fun getTileInfo(x: Int, y: Int, tile: Int) : TileSetTileInfo? =
            levelGridVania[x / gridWidth][y / gridHeight].tileMapData?.tileSet?.getInfo(tile)
    }

    // Data class for storing level data like grizSize, width, height, entities, tileMapData
    data class LevelData(
        var type: AssetType = AssetType.COMMON,  // TODO: Remove
        val gridSize: Int = 16,  // TODO: Remove
//        var width: Float = 0f,
//        var height: Float = 0f,

        val entities: MutableList<String> = mutableListOf(),  // TODO remove
//        val layerTileMaps: MutableMap<String, TileMapData> = mutableMapOf(),  // TODO remove
        var tileMapData: TileMapData? = null
    )
}
