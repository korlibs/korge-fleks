package korlibs.korge.fleks.assets

import korlibs.audio.format.*
import korlibs.audio.sound.*
import korlibs.image.atlas.MutableAtlasUnit
import korlibs.image.bitmap.*
import korlibs.image.font.BitmapFont
import korlibs.image.font.Font
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.fleks.assets.data.AssetLoader
import korlibs.korge.fleks.assets.data.AssetType
import korlibs.korge.fleks.assets.data.GameObjectConfig
import korlibs.korge.fleks.assets.data.LayerTileMaps
import korlibs.korge.fleks.assets.data.ParallaxConfigNew
import korlibs.korge.fleks.assets.data.ParallaxDataContainer
import korlibs.korge.fleks.assets.data.SpriteFrames
import korlibs.korge.fleks.assets.data.TextureAtlasLoader
import korlibs.korge.ldtk.view.*
import korlibs.time.Stopwatch
import kotlin.collections.set


typealias SoundMapType = MutableMap<String, Pair<AssetType, SoundChannel>>
typealias TileMapType = MutableMap<String, Pair<AssetType, LayerTileMaps>>
typealias SpriteFramesMapType = MutableMap<String, Pair<AssetType, SpriteFrames>>
typealias NinePatchBmpSliceMapType = MutableMap<String, Pair<AssetType, NinePatchBmpSlice>>
typealias BitMapFontMapType = MutableMap<String, Pair<AssetType, BitmapFont>>
typealias ParallaxMapType = MutableMap<String, Pair<AssetType, ParallaxConfigNew>>

/**
 * This class is responsible to load all kind of game data and make it usable / consumable by entities of Korge-Fleks.
 *
 * Assets are separated into [Common][korlibs.korge.fleks.assets.data.AssetType.COMMON], [World][korlibs.korge.fleks.assets.data.AssetType.WORLD], [Level][korlibs.korge.fleks.assets.data.AssetType.LEVEL] and
 * [Special][korlibs.korge.fleks.assets.data.AssetType.SPECIAL] types. The 'Common' type means that the asset is used throughout
 * the game. So it makes sense to not reload those assets on every level or world. The same applies also for 'World' type.
 * It means a world-asset is used in all levels of a world. An asset of type 'Level' means that it is really only used in
 * one level (e.g. level specific graphics or music). The 'Special' type of assets is meant to be used for loading assets
 * during a level which should be unloaded also within the level. This can be used for extensive graphics for a mid-level
 * boss. After the boss has been beaten the graphics can be unloaded since they are not needed anymore.
 */
class AssetStore {
    // Handles loading of various asset types
    val loader = AssetLoader(this)
    internal val assetLevelDataLoader: AssetLevelDataLoader = AssetLevelDataLoader(this)
    private val textureAtlasLoader = TextureAtlasLoader()

    var testing: Boolean = false  // Set to true for unit tests on headless linux nodes on GitHub Actions runner

    val commonAtlas: MutableAtlasUnit = MutableAtlasUnit(1024, 2048, border = 1)
    val worldAtlas: MutableAtlasUnit = MutableAtlasUnit(1024, 2048, border = 1)
    val levelAtlas: MutableAtlasUnit = MutableAtlasUnit(1024, 2048, border = 1)
    val specialAtlas: MutableAtlasUnit = MutableAtlasUnit(1024, 2048, border = 1)

    //    @Volatile
    internal var commonAssetConfig: AssetModel = AssetModel()
    internal var currentWorldAssetConfig: AssetModel = AssetModel()
    internal var currentLevelAssetConfig: AssetModel = AssetModel()
    internal var specialAssetConfig: AssetModel = AssetModel()

    internal val gameObjectConfig: MutableMap<String, GameObjectConfig> = mutableMapOf()

    internal val sounds: SoundMapType = mutableMapOf()
    internal val tileMaps: TileMapType = mutableMapOf()
    internal val textures: SpriteFramesMapType = mutableMapOf()
    internal val ninePatchSlices: NinePatchBmpSliceMapType = mutableMapOf()
    internal val bitMapFonts: BitMapFontMapType = mutableMapOf()
    internal val parallaxLayers: ParallaxMapType = mutableMapOf()

    fun addGameObjectConfig(name: String, config: GameObjectConfig) {
        if (gameObjectConfig.containsKey(name)) {
            println("WARNING - AssetStore: Game object config for '$name' already exists! Overwriting it!")
        }
        gameObjectConfig[name] = config
    }

    fun getGameObjectStateConfig(name: String) : GameObjectConfig =
        if (gameObjectConfig.containsKey(name)) {
            gameObjectConfig[name]!!
        } else error("AssetStore: Game object state config for '$name' not found!")

    fun getTileMapData(level: String) : LayerTileMaps =
        if (tileMaps.contains(level)) {
            tileMaps[level]!!.second
        }
        else error("AssetStore: Tile map for level '$level' not found!")

    fun getSound(name: String) : SoundChannel =
        if (sounds.contains(name)) sounds[name]!!.second
        else error("AssetStore: Sound '$name' not found!")

    fun getSpriteTexture(name: String) : SpriteFrames =
        if (textures.contains(name)) {
            textures[name]!!.second
        } else error("AssetStore: Texture '$name' not found for Sprite!")

    fun getBitmapTexture(name: String) : Bitmap =
        if (textures.contains(name)) {
            textures[name]!!.second.firstFrame.toBitmap()
        } else error("AssetStore: Texture '$name' not found for Bitmap!")

    fun getNinePatchSlice(name: String) : NinePatchBmpSlice =
        if (ninePatchSlices.contains(name)) {
            ninePatchSlices[name]!!.second
        } else error("AssetStore: NinePatchSlice '$name' not found!")

//    fun getParallaxLayerTexture(name: String) : ParallaxLayerTexture =
//        if (parallaxLayers.contains(name)) {
//            parallaxLayers[name]!!.second
//        } else error("AssetStore: Parallax layer texture '$name' not found!")

    fun getBackground(name: String) : ParallaxDataContainer =
        /*backgrounds[name]?.second ?:*/ error("AssetStore: Parallax background '$name' not found!")

    fun getFont(name: String) : Font =
        bitMapFonts[name]?.second ?: error("AssetStore: Cannot find font '$name'!")


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

            // Update maps of music, images, ...
            if (!testing) {
                assetConfig.sounds.forEach { sound ->
                    val soundFile = resourcesVfs[assetConfig.folder + "/" + sound.value].readSound(  //readMusic(
                        props = AudioDecodingProps(exactTimings = true),
                        streaming = true
                    )
//                    val soundChannel = soundFile.decode().toWav().readMusic().play()  // -- convert to WAV
                    val soundChannel = soundFile.play()
//                    val soundChannel2 = resourcesVfs[assetConfig.folder + "/" + sound.value].readSound().play()

                    soundChannel.pause()
                    sounds[sound.key] = Pair(type, soundChannel)
                }
            }

            assetConfig.tileMaps.forEach { tileMap ->
                val levelName = tileMap.key
                val ldtkFile = tileMap.value.fileName
                val collisionLayerName = tileMap.value.collisionLayerName
                val tileSetPaths = tileMap.value.tileSetPaths
                val ldtkWorld = resourcesVfs[assetConfig.folder + "/" + ldtkFile].readLDTKWorld(extrude = true)

                when  (type) {
                    AssetType.LEVEL -> {
                        assetLevelDataLoader.loadLevelData(ldtkWorld, collisionLayerName, levelName, tileSetPaths)
                    }
                    else -> {
                        // Load raw tile map data for tilemap object types
                        ldtkWorld.ldtk.levels.forEach { ldtkLevel ->
                            tileMaps[ldtkLevel.identifier] = Pair(type, LayerTileMaps(levelName, ldtkWorld, ldtkLevel))
                        }
                    }
                }
            }

            assetConfig.textureAtlas.forEach { config ->
                textureAtlasLoader.load(
                    assetConfig.folder,
                    config,
                    textures,
                    ninePatchSlices,
                    bitMapFonts,
                    parallaxLayers,
                    type)
            }

            println("Assets: Loaded resources in ${sw.elapsed}")

            if (assetConfig.hotReloading) {
                configureResourceDirWatcher {
                    addAssetWatcher(type) {}
                }
            }
        }
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
        sounds.values.removeAll { it.first == type }
        tileMaps.values.removeAll { it.first == type }
        textures.values.removeAll { it.first == type }
        ninePatchSlices.values.removeAll { it.first == type }
        bitMapFonts.values.removeAll { it.first == type }
    }
}
