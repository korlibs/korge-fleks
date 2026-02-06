package korlibs.korge.fleks.assets

import korlibs.audio.sound.*
import korlibs.image.bitmap.*
import korlibs.image.font.BitmapFont
import korlibs.image.font.Font
import korlibs.image.tiles.TileSet
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.fleks.assets.data.ClusterAssetInfo.*
import korlibs.korge.fleks.assets.data.AssetLoader
import korlibs.korge.fleks.assets.data.AssetType
import korlibs.korge.fleks.assets.data.GameObjectConfig
import korlibs.korge.fleks.assets.data.LayerTileMaps
import korlibs.korge.fleks.assets.data.SpriteFrames
import korlibs.korge.fleks.assets.data.TextureAtlasLoader
import korlibs.korge.fleks.assets.data.SimpleTileSet
import korlibs.korge.fleks.assets.data.readKorgeFleksAssets
import korlibs.korge.fleks.assets.data.UNKNOWN
import korlibs.time.Stopwatch
import kotlin.collections.set


typealias SoundMapType = MutableMap<String, Pair<AssetType, SoundChannel>>

typealias SpriteFramesMapType = MutableMap<String, Pair<AssetType, SpriteFrames>>
typealias NinePatchBmpSliceMapType = MutableMap<String, Pair<AssetType, NinePatchBmpSlice>>
typealias BitMapFontMapType = MutableMap<String, Pair<AssetType, BitmapFont>>
typealias ParallaxLayersMapType = MutableMap<String, Pair<AssetType, ParallaxLayersInfo>>

typealias TilesetMapType = MutableMap<String, Pair<AssetType, TileSet>>
typealias TileMapsType = MutableMap<String, Pair<AssetType, LayerTileMaps>>

// NEW
typealias TilesetMapType2 = MutableMap<String, Pair<AssetType, SimpleTileSet>>


/**
 * This class is responsible to load all kind of game data and make it usable / consumable by entities of Korge-Fleks.
 *
 * TODO below description section needs rework
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

    internal val loadedClusterAssets: MutableSet<String> = mutableSetOf()

    internal val gameObjectConfig: MutableMap<String, GameObjectConfig> = mutableMapOf()

    // Sound related assets
    internal val sounds: SoundMapType = mutableMapOf()

    // Image related assets
    internal val textures: SpriteFramesMapType = mutableMapOf()
    internal val ninePatchSlices: NinePatchBmpSliceMapType = mutableMapOf()
    internal val bitMapFonts: BitMapFontMapType = mutableMapOf()
    internal val parallaxLayers: ParallaxLayersMapType = mutableMapOf()

    // tiles (tileset and tilemap) related assets
    internal val tileMaps: TileMapsType = mutableMapOf()
    internal val tilesets: TilesetMapType = mutableMapOf()

    // NEW
    internal val tilesets2: TilesetMapType2 = mutableMapOf()

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

    fun getSound(name: String) : SoundChannel =
        if (sounds.contains(name)) sounds[name]!!.second
        else error("AssetStore: Sound '$name' not found!")

    fun getSpriteTexture(name: String) : SpriteFrames =
        if (textures.contains(name)) {
            textures[name]!!.second
        } else {
            // Add transparent texture as fallback to avoid continuous error messages
            textures[name] = Pair(UNKNOWN, SpriteFrames.EMPTY)
            println("ERROR: AssetStore - Texture '$name' not found for Sprite!")
            SpriteFrames.EMPTY
        }

    fun getBitmapTexture(name: String) : Bitmap =
        if (textures.contains(name)) {
            textures[name]!!.second.firstFrameSlice.toBitmap()
        } else error("AssetStore: Texture '$name' not found for Bitmap!")

    fun getNinePatchSlice(name: String) : NinePatchBmpSlice =
        if (ninePatchSlices.contains(name)) {
            ninePatchSlices[name]!!.second
        } else error("AssetStore: NinePatchSlice '$name' not found!")

    fun getParallaxLayers(name: String) : ParallaxLayersInfo =
        if (parallaxLayers.contains(name)) {
            parallaxLayers[name]!!.second
        } else error("AssetStore: Parallax layers config '$name' not found!")

    fun getFont(name: String) : Font =
        bitMapFonts[name]?.second ?: error("AssetStore: Cannot find font '$name'!")

    fun getTileset(name: String) : TileSet =
        if (tilesets.contains(name)) {
            tilesets[name]!!.second
        } else error("AssetStore: Tileset '$name' not found!")

    fun getTileMapData(level: String) : LayerTileMaps =
        if (tileMaps.contains(level)) {
            tileMaps[level]!!.second
        }
        else error("AssetStore: Tile map for level '$level' not found!")

    suspend fun loadClusterAssets(clusterPath: String, hotReloading: Boolean = false) {

        if (loadedClusterAssets.contains(clusterPath)) {
            println("INFO: Asset cluster '$clusterPath' already loaded! No reload is happening!")
            return
        } else {
            loadedClusterAssets.add(clusterPath)
        }

        val sw = Stopwatch().start()
        println("INFO: AssetStore - Start loading asset cluster '$clusterPath'... ")

// TODO load sounds and music
//            // Update maps of music, images, ...
//            if (!testing) {
//                assetConfig.sounds.forEach { sound ->
//                    val soundFile = resourcesVfs[assetConfig.folder + "/" + sound.fileName].readSound(  //readMusic(
//                        props = AudioDecodingProps(exactTimings = true),
//                        streaming = true
//                    )
////                    val soundChannel = soundFile.decode().toWav().readMusic().play()  // -- convert to WAV
//                    val soundChannel = soundFile.play()
////                    val soundChannel2 = resourcesVfs[assetConfig.folder + "/" + sound.value].readSound().play()
//
//                    soundChannel.pause()
//                    sounds[sound.name] = Pair(type, soundChannel)
//                }
//            }

            resourcesVfs["${clusterPath}/assets.json"].readKorgeFleksAssets(
                clusterPath, textures, ninePatchSlices, bitMapFonts, parallaxLayers, tilesets2)

// TODO load tile maps from LDtk files
//            assetConfig.tileMaps.forEach { tileMap ->
//                val levelName = tileMap.name
//                val ldtkFile = tileMap.fileName
//                val collisionLayerName = tileMap.collisionLayerName
//                val tileSetPaths = mutableListOf("")
//                val ldtkWorld = resourcesVfs[assetConfig.folder + "/" + ldtkFile].readLdtkWorld()
//
//                when  (type) {
//                    AssetType.LEVEL -> {
//                        assetLevelDataLoader.loadLevelData(ldtkWorld, collisionLayerName, levelName, tileSetPaths)
//                    }
//                    else -> {
//                        // Load raw tile map data for tilemap object types
//                        ldtkWorld.ldtk.levels.forEach { ldtkLevel ->
//                            tileMaps[ldtkLevel.identifier] = Pair(type, LayerTileMaps(this, levelName, ldtkWorld, ldtkLevel))
//                        }
//                    }
//                }
//            }

        println("INFO: AssetStore - Resources loaded in ${sw.elapsed}")

        // TODO hot reloading needs rework!!!
        if (hotReloading) {
            configureResourceDirWatcher {
                addAssetWatcher(clusterPath) {}
            }
        }
    }

    /**
     * Remove all assets which have a specific given [AssetType].
     */
    private fun removeAssets(type: AssetType) {
        sounds.values.removeAll { it.first == type }

        textures.values.removeAll { it.first == type }
        ninePatchSlices.values.removeAll { it.first == type }
        bitMapFonts.values.removeAll { it.first == type }
        parallaxLayers.values.removeAll { it.first == type }

        tilesets.values.removeAll { it.first == type }
        tileMaps.values.removeAll { it.first == type }
    }
}
