package korlibs.korge.fleks.assets

import korlibs.audio.format.*
import korlibs.audio.sound.*
import korlibs.datastructure.*
import korlibs.image.atlas.MutableAtlasUnit
import korlibs.image.bitmap.*
import korlibs.image.font.Font
import korlibs.image.font.readBitmapFont
import korlibs.image.format.*
import korlibs.image.tiles.*
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.gameState.GameStateManager.assetStore
import korlibs.korge.fleks.utils.*
import korlibs.korge.ldtk.*
import korlibs.korge.ldtk.view.*
import korlibs.memory.*
import korlibs.time.Stopwatch
import korlibs.math.max
import kotlinx.serialization.*
import kotlin.collections.set
import kotlin.math.max

typealias LayerTileMaps = MutableMap<String, TileMapData>
typealias LayerEntityMaps = MutableMap<String, List<EntityConfig>>

/**
 * This class is responsible to load all kind of game data and make it usable / consumable by entities of Korge-Fleks.
 *
 * Assets are separated into [Common][AssetType.COMMON], [World][AssetType.WORLD], [Level][AssetType.LEVEL] and
 * [Special][AssetType.SPECIAL] types. The 'Common' type means that the asset is used throughout
 * the game. So it makes sense to not reload those assets on every level or world. The same applies also for 'World' type.
 * It means a world-asset is used in all levels of a world. An asset of type 'Level' means that it is really only used in
 * one level (e.g. level specific graphics or music). The 'Special' type of assets is meant to be used for loading assets
 * during a level which should be unloaded also within the level. This can be used for extensive graphics for a mid-level
 * boss. After the boss has been beaten the graphics can be unloaded since they are not needed anymore.
 */
class AssetStore {
    val commonAtlas: MutableAtlasUnit = MutableAtlasUnit(1024, 2048, border = 1)
    val worldAtlas: MutableAtlasUnit = MutableAtlasUnit(1024, 2048, border = 1)
    val levelAtlas: MutableAtlasUnit = MutableAtlasUnit(1024, 2048, border = 1)
    val specialAtlas: MutableAtlasUnit = MutableAtlasUnit(1024, 2048, border = 1)

    val configDeserializer = EntityConfigSerializer()

    //    @Volatile
    internal var commonAssetConfig: AssetModel = AssetModel()
    internal var currentWorldAssetConfig: AssetModel = AssetModel()
    internal var currentLevelAssetConfig: AssetModel = AssetModel()
    internal var specialAssetConfig: AssetModel = AssetModel()

    internal val levelMaps: MutableMap<String, Pair<AssetType, LayerTileMaps>> = mutableMapOf()           // 1st string: Level name, 2nd string: Layer name
    internal val entityConfigMaps: MutableMap<String, Pair<AssetType, LayerEntityMaps>> = mutableMapOf()  // 1st string: Level name, 2nd string: Layer name
    internal var backgrounds: MutableMap<String, Pair<AssetType, ParallaxDataContainer>> = mutableMapOf()
    internal var images: MutableMap<String, Pair<AssetType, ImageDataContainer>> = mutableMapOf()
    internal var fonts: MutableMap<String, Pair<AssetType, Font>> = mutableMapOf()
    internal var sounds: MutableMap<String, Pair<AssetType, SoundChannel>> = mutableMapOf()

    fun getSound(name: String) : SoundChannel =
        if (sounds.contains(name)) sounds[name]!!.second
        else error("AssetStore: Sound '$name' not found!")

    fun getImageData(name: String, slice: String? = null) : ImageData =
        if (images.contains(name)) {
            if (slice == null) {
                images[name]!!.second.default
            } else {
                if (images[name]!!.second[slice] != null) {
                    images[name]!!.second[slice]!!
                } else {
                    println("WARNING - AssetStore: Slice '$slice' of image '$name' not found!")
                    ImageData()
                }
            }
        } else {
            println("WARNING - AssetStore: Image '$name' not found!")
            ImageData()
        }

    fun getTileMapData(level: String, layer: String) : TileMapData =
        if (levelMaps.contains(level)) {
            if (levelMaps[level]!!.second.contains(layer)) levelMaps[level]!!.second[layer]!!
            else error("AssetStore: TileMap layer '$layer' for level '$level' not found!")
        }
        else error("AssetStore: Level map for level '$level' not found!")

    fun getEntityConfigs(level: String, layer: String) : List<EntityConfig> =
        if (entityConfigMaps.contains(level)) {
            if (entityConfigMaps[level]!!.second.contains(layer)) entityConfigMaps[level]!!.second[layer]!!
            else error("AssetStore: Entity layer '$layer' for level '$level' not found!")
        }
        else error("AssetStore: EntityConfig for level '$level' not found!")

    fun getNinePatch(name: String) : NinePatchBmpSlice =
        if (images.contains(name)) {
            val layerData = images[name]!!.second.imageDatas.first().frames.first().first
            if (layerData != null) {
                val ninePatch = layerData.ninePatchSlice
                ninePatch ?: error("AssetStore: Image '$name' does not contain nine-patch data!")
            } else error("AssetStore: Image layer of '$name' not found!")
        } else error("AssetStore: Image '$name' not found!")

    fun getBackground(name: String) : ParallaxDataContainer =
        if (backgrounds.contains(name)) backgrounds[name]!!.second
        else error("AssetStore: Parallax background '$name' not found!")

    fun getFont(name: String) : Font {
        return if (fonts.contains(name)) fonts[name]!!.second
        else error("AssetStore: Cannot find font '$name'!")
    }

    suspend fun loadAssets(type: AssetType, assetConfig: AssetModel) {
        var assetLoaded = false
        val atlas = when (type) {
            AssetType.COMMON -> {
                prepareCurrentAssets(type, assetConfig, commonAssetConfig)?.also { config ->
                    commonAssetConfig = config
                    assetLoaded = true
                }
                commonAtlas
            }
            AssetType.WORLD -> {
                prepareCurrentAssets(type, assetConfig, currentWorldAssetConfig)?.also { config ->
                    currentWorldAssetConfig = config
                    assetLoaded = true
                }
                worldAtlas
            }
            AssetType.LEVEL -> {
                prepareCurrentAssets(type, assetConfig, currentLevelAssetConfig)?.also { config ->
                    currentLevelAssetConfig = config
                    assetLoaded = true
                }
                levelAtlas
            }
            AssetType.SPECIAL -> {
                prepareCurrentAssets(type, assetConfig, specialAssetConfig)?.also { config ->
                    specialAssetConfig = config
                    assetLoaded = true
                }
                specialAtlas
            }
        }

        if (assetLoaded) {

            val sw = Stopwatch().start()
            println("AssetStore: Start loading [${type.name}] resources from '${assetConfig.folder}'...")

            var gameObjectCnt = 0

            // Update maps of music, images, ...
            assetConfig.tileMaps.forEach { tileMap ->
                val ldtkWorld = resourcesVfs[assetConfig.folder + "/" + tileMap.fileName].readLDTKWorld(extrude = true)
                val gridSize = ldtkWorld.ldtk.defaultGridSize
                // Save TileMapData for each Level and layer combination from LDtk world
                ldtkWorld.ldtk.levels.forEach { ldtkLevel ->
                    val levelName = ldtkLevel.identifier
                    ldtkLevel.layerInstances?.forEach { ldtkLayer ->
                        val layerName = ldtkLayer.identifier
                        // Check if layer has tile set -> store tile map data
                        val tilesetExt = ldtkWorld.tilesetDefsById[ldtkLayer.tilesetDefUid]
                        if (tilesetExt != null) {
                            storeTiles(ldtkLayer, tilesetExt, levelName, layerName, type)
                        }
                        // Check if layer contains entity data -> create EntityConfigs and store them fo
                        if (ldtkLayer.entityInstances.isNotEmpty()) {
                            val entityList = mutableListOf<EntityConfig>()

                            ldtkLayer.entityInstances.forEach { entity ->
                                // Create YAML string of an entity config from LDtk
                                val yamlString = StringBuilder()
                                // Sanity check - entity needs to have a field 'entityConfig'
                                if (entity.fieldInstances.firstOrNull { it.identifier == "entityConfig" } != null) {

                                    // Add scripts with their original name
                                    if (entity.tags.firstOrNull { it == "script" } != null) yamlString.append("name: ${entity.identifier}\n")
                                    // Add other game objects with a unique name as identifier
                                    else yamlString.append("name: ${levelName}_${entity.identifier}_${gameObjectCnt++}\n")

                                    // Add position of entity
                                    entity.tags.firstOrNull { it == "positionable" }?.let {
                                        yamlString.append("x: ${entity.gridPos.x * gridSize}\n")
                                        yamlString.append("y: ${entity.gridPos.y * gridSize}\n")
                                    }

                                    // Add all other fields of entity
                                    entity.fieldInstances.forEach { field ->
                                        if (field.identifier != "EntityConfig") yamlString.append("${field.identifier}: ${field.value}\n")
                                    }
                                    println("INFO: Game object '${entity.identifier}' loaded for '$levelName'")
                                    println("\n$yamlString")

                                    try {
                                        val entityConfig: EntityConfig = configDeserializer.yaml().decodeFromString(yamlString.toString())
                                        entityList.add(entityConfig)

                                        println("INFO: Entity config '${entity.identifier}' loaded for '$levelName'")
                                    } catch (e: Throwable) {
                                        println("ERROR: Loading entity config - $e")
                                    }

                                } else println("ERROR: Game object with name '${entity.identifier}' has no field entityConfig!")
                            }

                            // Create new map for Entity layer if it does not exist yet
                            if (!entityConfigMaps.contains(levelName)) {
                                val layerEntityMaps: LayerEntityMaps = mutableMapOf()
                                entityConfigMaps[levelName] = Pair(type, layerEntityMaps)
                            }
                            // Finally store entity config for level and entity layer
                            entityConfigMaps[levelName]!!.second[layerName] = entityList
                        }
                    }
                }
            }

            assetConfig.sounds.forEach { sound ->
                val soundFile = resourcesVfs[assetConfig.folder + "/" + sound.value].readMusic()
                val soundChannel = soundFile.decode().toWav().readMusic().play()  // -- convert to WAV
//                val soundChannel = soundFile.play()
//            val soundChannel = resourcesVfs[assetConfig.assetFolderName + "/" + sound.value].readSound().play()

                soundChannel.pause()
                sounds[sound.key] = Pair(type, soundChannel)
            }
            assetConfig.backgrounds.forEach { background ->
                backgrounds[background.key] = Pair(type, resourcesVfs[assetConfig.folder + "/" + background.value.aseName].readParallaxDataContainer(background.value, ASE, atlas = atlas))
            }
            assetConfig.images.forEach { image ->
                images[image.key] = Pair(
                    type,
                    if (image.value.layers == null) {
                        resourcesVfs[assetConfig.folder + "/" + image.value.fileName].readImageDataContainer(ASE.toProps(), atlas = atlas)
                    } else {
                        val props = ASE.toProps() // TODO check -- ImageDecodingProps(it.value.fileName, extra = ExtraTypeCreate())
                        props.setExtra("layers", image.value.layers)
                        resourcesVfs[assetConfig.folder + "/" + image.value.fileName].readImageDataContainer(props, atlas)
                    }
                )
            }
            assetConfig.fonts.forEach { font ->
                fonts[font.key] = Pair(type, resourcesVfs[assetConfig.folder + "/" + font.value].readBitmapFont(atlas = atlas))
            }

            println("Assets: Loaded resources in ${sw.elapsed}")

            if (assetConfig.hotReloading) {
                configureResourceDirWatcher {
                    addAssetWatcher(type) {}
                }
            }
        }
    }

    internal fun storeTiles(ldtkLayer: LayerInstance, tilesetExt: ExtTileset, level: String, layer: String, type: AssetType) {
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
        val layerTileMaps: LayerTileMaps = mutableMapOf()
        layerTileMaps[layer] = tileMapData
        // Add layer map to level Maps
        levelMaps[level] = Pair(type, layerTileMaps)
    }

    private fun prepareCurrentAssets(type: AssetType, newAssetConfig: AssetModel, currentAssetConfig: AssetModel): AssetModel? =
        when (currentAssetConfig.folder) {
            "" -> {
                // Just load new assets
                newAssetConfig
            }
            newAssetConfig.folder -> {
                if (newAssetConfig.hotReloading) {
                    println("INFO: Reload $type assets '${newAssetConfig.folder}'.")
                    newAssetConfig
                } else {
                    println("INFO: $type assets '${newAssetConfig.folder}' already loaded! No reload is happening!")
                    null
                }
            }
            else -> {
                println("INFO: Remove old $type assets and load new ones!")
                removeAssets(type)
                newAssetConfig
            }
        }

    /**
     * Remove all assets which have a specific given [AssetType].
     */
    private fun removeAssets(type: AssetType) {
        backgrounds.values.removeAll { it.first == type }
        images.values.removeAll { it.first == type }
        fonts.values.removeAll { it.first == type }
        sounds.values.removeAll { it.first == type }
        levelMaps.values.removeAll { it.first == type }
    }
}
