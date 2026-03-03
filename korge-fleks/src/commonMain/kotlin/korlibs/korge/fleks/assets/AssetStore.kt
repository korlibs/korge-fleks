package korlibs.korge.fleks.assets

import korlibs.audio.sound.*
import korlibs.image.bitmap.*
import korlibs.image.font.BitmapFont
import korlibs.image.font.Font
import korlibs.korge.fleks.assets.data.ClusterAssetInfo.*
import korlibs.korge.fleks.assets.data.AssetLoader
import korlibs.korge.fleks.assets.data.AssetType
import korlibs.korge.fleks.assets.data.ChunkAssetInfo
import korlibs.korge.fleks.assets.data.GameObjectConfig
import korlibs.korge.fleks.assets.data.SpriteFrames
import korlibs.korge.fleks.assets.data.SimpleTileSet
import korlibs.korge.fleks.assets.data.UNKNOWN
import korlibs.korge.fleks.assets.data.WorldMapData
import kotlin.collections.set


typealias SoundsAssetType = MutableMap<String, Pair<AssetType, SoundChannel>>

typealias SpriteFramesAssetType = MutableMap<String, Pair<AssetType, SpriteFrames>>
typealias NinePatchBmpSlicesAssetType = MutableMap<String, Pair<AssetType, NinePatchBmpSlice>>
typealias BitMapFontsAssetType = MutableMap<String, Pair<AssetType, BitmapFont>>
typealias ParallaxLayersAssetType = MutableMap<String, Pair<AssetType, ParallaxLayersInfo>>

typealias TileMapsAssetType = MutableMap<String, Pair<AssetType, TileMapInfo>>
typealias TileSetsAssetType = MutableMap<String, Pair<AssetType, SimpleTileSet>>


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
    val loader = AssetLoader(this)  // TODO check if we should put the loader into an extra class

    var testing: Boolean = false  // Set to true for unit tests on headless linux nodes on GitHub Actions runner

    internal val gameObjectConfig: MutableMap<String, GameObjectConfig> = mutableMapOf()

    // Sound related assets
    internal val sounds: SoundsAssetType = mutableMapOf()

    // Image related assets
    internal val textures: SpriteFramesAssetType = mutableMapOf()
    internal val ninePatchSlices: NinePatchBmpSlicesAssetType = mutableMapOf()
    internal val bitMapFonts: BitMapFontsAssetType = mutableMapOf()
    internal val parallaxLayers: ParallaxLayersAssetType = mutableMapOf()

    // tiles (tileset and tilemap) related assets
    internal val tileMaps: TileMapsAssetType = mutableMapOf()
    internal val tileSets: TileSetsAssetType = mutableMapOf()

    val worldMapData = WorldMapData()

    fun addWorldChunk(chunkIndex: Int, worldChunk: ChunkAssetInfo) {
        worldMapData.chunkMeshes[chunkIndex] = worldChunk
        worldMapData.levelGridVania[worldChunk.chunkX, worldChunk.chunkY] = chunkIndex
    }

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

    fun getTileSet(name: String) : SimpleTileSet =
        if (tileSets.contains(name)) {
            tileSets[name]!!.second
        } else error("AssetStore: Tile set '$name' not found!")

    fun getTileMap(name: String) : TileMapInfo =
        if (tileMaps.contains(name)) {
            tileMaps[name]!!.second
        }
        else error("AssetStore: Chunk tile map '$name' not found!")

    /**
     * Remove all assets which have a specific given [AssetType] string.
     */
    private fun removeAssets(clusterName: String) {
        sounds.values.removeAll { it.first == clusterName }

        textures.values.removeAll { it.first == clusterName }
        ninePatchSlices.values.removeAll { it.first == clusterName }
        bitMapFonts.values.removeAll { it.first == clusterName }
        parallaxLayers.values.removeAll { it.first == clusterName }

        tileSets.values.removeAll { it.first == clusterName }
        tileMaps.values.removeAll { it.first == clusterName }
    }
}
