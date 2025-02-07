package korlibs.korge.fleks.assets

import korlibs.image.tiles.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.ldtk.*
import korlibs.korge.ldtk.view.*
import korlibs.math.*
import korlibs.memory.*
import kotlinx.serialization.*
import kotlin.math.*

class LevelMapAssets {

    internal val worldData = WorldData()
    // TODO: move into WorldData
    internal val levelDataMaps: MutableMap<String, LevelData> = mutableMapOf()
    internal val configDeserializer = EntityConfigSerializer()

    private var gameObjectCnt = 0

    fun loadLevelData(ldtkWorld: LDTKWorld, type: AssetType, hasParallax: Boolean) {
        createWorldDataMap(ldtkWorld, hasParallax)

        // Save TileMapData for each Level and layer combination from LDtk world
        ldtkWorld.ldtk.levels.forEach { ldtkLevel ->

            val levelPosX = ldtkLevel.worldX
            val levelPosY = ldtkLevel.worldY

            val levelName = ldtkLevel.identifier
            ldtkLevel.layerInstances?.forEach { ldtkLayer ->
                val layerName = ldtkLayer.identifier
                val gridSize = ldtkLayer.gridSize

                // Check if layer has tile set -> store tile map data
                val tilesetExt = ldtkWorld.tilesetDefsById[ldtkLayer.tilesetDefUid]
                if (tilesetExt != null) {
                    storeTiles(ldtkLayer, tilesetExt, levelName, layerName, type)
                }
                // Check if layer contains entity data -> create EntityConfigs and store them fo
                if (ldtkLayer.entityInstances.isNotEmpty()) {
                    val entityNames = mutableListOf<String>()

                    ldtkLayer.entityInstances.forEach { entity ->
                        // Create YAML string of an entity config from LDtk
                        val yamlString = StringBuilder()
                        // Sanity check - entity needs to have a field 'entityConfig'
                        if (entity.fieldInstances.firstOrNull { it.identifier == "entityConfig" } != null) {

                            if (entity.tags.firstOrNull { it == "unique" } != null) {
                                // Add scripts without unique count value - they are unique by name because they exist only once
                                yamlString.append("name: ${levelName}_${entity.identifier}\n")
                            }
                            else {
                                // Add other game objects with a unique name as identifier
                                yamlString.append("name: ${levelName}_${entity.identifier}_${gameObjectCnt++}\n")
                            }

                            // Add position of entity = (level position in the world) + (grid position within the level) + (pivot point)
                            // TODO: Take level position in world into account
                            val entityPosX: Int = /*levelPosX +*/ (entity.gridPos.x * gridSize) + (entity.pivot[0] * gridSize).toInt()
                            val entityPosY: Int = /*levelPosY +*/ (entity.gridPos.y * gridSize) + (entity.pivot[1] * gridSize).toInt()


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
                                val entityConfig: EntityConfig = configDeserializer.yaml().decodeFromString(yamlString.toString())

                                // TODO: We need to store only the name of the entity config for later dynamically spawning of entities
                                //       We need to store the entity configs in a 2D array depending on its position in the level
                                //       Then later we will spawn the entities depending on the position in the level
                                entityNames.add(entityConfig.name)

                                println("INFO: Registering entity config '${entity.identifier}' for '$levelName'")
                            } catch (e: Throwable) {
                                println("ERROR: Loading entity config - $e")
                            }

                        } else println("ERROR: Game object with name '${entity.identifier}' has no field entityConfig!")
                    }

                    // Create new level data if it does not exist yet
                    if (!levelDataMaps.contains(levelName)) {
                        levelDataMaps[levelName] = LevelData(
                            type = type,
                            gridSize = gridSize,
                            width = (ldtkLayer.cWid * gridSize).toFloat(),
                            height = (ldtkLayer.cHei * gridSize).toFloat(),
                            entities = entityNames,
                            layerTileMaps = mutableMapOf()
                        )
                    } else {
                        levelDataMaps[levelName]!!.entities
                    }
                }
            }
        }
    }

    fun reloadAsset(ldtkWorld: LDTKWorld, type: AssetType) {
        // Reload all levels from ldtk world file
        ldtkWorld.ldtk.levels.forEach { ldtkLevel ->
            ldtkLevel.layerInstances?.forEach { ldtkLayer ->
                val tilesetExt = ldtkWorld.tilesetDefsById[ldtkLayer.tilesetDefUid]

                if (tilesetExt != null) {
                    storeTiles(ldtkLayer, tilesetExt, ldtkLevel.identifier, ldtkLayer.identifier, type)
                    println("\nTriggering asset change for LDtk level : ${ldtkLevel.identifier}_${ldtkLayer.identifier}")
                }
            }
        }
    }

    fun removeAssets(type: AssetType) {
        levelDataMaps.values.removeAll { it.type == type }
    }

    private fun storeTiles(ldtkLayer: LayerInstance, tilesetExt: ExtTileset, level: String, layer: String, type: AssetType) {
        val tileMapData = TileMapData(
            width = ldtkLayer.cWid,
            height = ldtkLayer.cHei,
            tileSet = if(tilesetExt.tileset != null) tilesetExt.tileset!! else TileSet.EMPTY
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
                entities = listOf(),
                layerTileMaps = layerTileMaps
            )
            levelDataMaps[level] = levelData
        } else {
            levelDataMaps[level]!!.layerTileMaps[layer] = tileMapData
        }
    }

    private fun createWorldDataMap(ldtkWorld: LDTKWorld, hasParallax: Boolean) {

        if (hasParallax) {
            // TODO: Get world height and width from WorldGridvania 2D array
            //       * 2 because level 1 has still double height
            worldData.width = 2 * (ldtkWorld.ldtk.worldGridWidth ?: 1)  // gridVaniaWidth
            worldData.height = 2 * (ldtkWorld.ldtk.worldGridHeight ?: 1)  // gridVaniaHeight
        }

        // TODO: fill WorldData class with levels

    }

    data class WorldData(
        var width: Int = 0,
        var height: Int = 0,
        val levelGridVania: MutableList<List<LevelData>> = mutableListOf()
    )

    // Data class for storing level data like grizSize, width, height, entities, tileMapData
    data class LevelData(
        val type: AssetType,
        val gridSize: Int,
        val width: Float,
        val height: Float,
        val entities: List<String>,
        val layerTileMaps: MutableMap<String, TileMapData>
    )
}
