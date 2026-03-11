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
 * This class is responsible to store all kind of game data and make it usable / consumable by entities of a Korge-fleks game.
 *
 * Assets are separated by two types:
 *   - CommonAssets
 *   - WorldClusterAssets
 *
 * CommonAssets are assets which are used throughout the whole game. They are not specific to a world or level.
 * Examples for common assets are the player character's sprite frames, the player character's sound effects, the font for the UI, ...
 *
 * WorldClusterAssets are assets which are specific to a world and a cluster/section within a world. A world cluster can be
 * seen as a section of a world which has its own specific assets. For example, a world cluster can be a part of the world map
 * which has its own specific graphics (e.g. background, foreground, tilesets) and music. A world cluster can be used for example to
 * separate the intro part of a world from the rest of the world. The intro part can have its own specific graphics and
 * music which are not needed for the rest of the world. So it makes sense to load those assets only for the intro part
 * and unload them after the intro part is finished. This can help to save memory and loading times for the rest of the world.
 *
 * Good practices for world cluster assets are:
 * - Define a "common" cluster for each world which contains assets which are likely used in most clusters of the world.
 *   This can help to avoid loading and unloading of assets which are used in most clusters of the world and to keep the
 *   loading and unloading of assets for the specific clusters more efficient. For example, the common cluster can contain the
 *   tilesets for the level maps of the world and the parallax background images for the world. The specific clusters can then contain
 *   the specific assets for the different sections of the world (e.g. intro, boss fight section, ...).
 * - Take care to use specific asset clusters in an area of chunks and mark this area by color in LDtk or the used
 *   level editor. This can help to keep an overview of which assets are used in which area of the world and to avoid loading
 *   and unloading of assets.
 */
class AssetStore {
    // Handles loading of common and world cluster assets
    val loader = AssetLoader(this)
    // Data structure to keep track of loaded world map data (e.g. chunk meshes, level maps, grid vania, ...)
    val worldMapData = WorldMapData()

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
