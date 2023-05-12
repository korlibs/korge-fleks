package korlibs.korge.fleks.assets

import com.github.quillraven.fleks.World
import korlibs.audio.sound.SoundChannel
import korlibs.audio.sound.readMusic
import korlibs.datastructure.setExtra
import korlibs.image.atlas.MutableAtlasUnit
import korlibs.image.font.Font
import korlibs.image.font.readBitmapFont
import korlibs.image.format.*
import korlibs.image.tiles.tiled.TiledMap
import korlibs.image.tiles.tiled.readTiledMap
import korlibs.io.async.launchImmediately
import korlibs.io.file.Vfs
import korlibs.io.file.VfsFile
import korlibs.io.file.fullName
import korlibs.io.file.std.resourcesVfs
import korlibs.io.lang.Closeable
import korlibs.korge.fleks.components.AssetReload
import korlibs.korge.fleks.components.ConfigName
import korlibs.korge.fleks.entity.config.TextAndLogos
import korlibs.korge.fleks.utils.AssetReloadCache
import korlibs.korge.fleks.utils.SerializableConfig
import korlibs.korge.parallax.ParallaxDataContainer
import korlibs.korge.parallax.readParallaxDataContainer
import korlibs.time.Stopwatch
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext

/**
 * This class is responsible to load all kind of game data and make it usable / consumable by entities of Korge-Fleks.
 *
 * Assets are separated into 'common', 'world' and 'level' types. The common type means that the asset is used throughout
 * the game. So it makes sense to not reload those assets on every level or world. The same applies also for world type.
 * It means a world-asset is used in all levels of a world. An asset of type 'level' means that it is really only used in
 * one level (e.g. a level-end boss or other level specific graphics).
 */
class AssetStore {
    val commonAtlas: MutableAtlasUnit = MutableAtlasUnit(1024, 2048, border = 1)
    val worldAtlas: MutableAtlasUnit = MutableAtlasUnit(1024, 2048, border = 1)
    val levelAtlas: MutableAtlasUnit = MutableAtlasUnit(1024, 2048, border = 1)

    private var commonAssetConfig: AssetModel = AssetModel()
    private var currentWorldAssetConfig: AssetModel = AssetModel()
    private var currentLevelAssetConfig: AssetModel = AssetModel()

    private var entityConfigs: MutableMap<String, SerializableConfig> = mutableMapOf()
    private var tiledMaps: MutableMap<String, Pair<AssetType, TiledMap>> = mutableMapOf()
    private var backgrounds: MutableMap<String, Pair<AssetType, ParallaxDataContainer>> = mutableMapOf()
    private var images: MutableMap<String, Pair<AssetType, ImageDataContainer>> = mutableMapOf()
    private var fonts: MutableMap<String, Pair<AssetType, Font>> = mutableMapOf()
    private var sounds: MutableMap<String, Pair<AssetType, SoundChannel>> = mutableMapOf()

    private var reloading: Boolean = false  // Used for debouncing reload of config (in case the modification message comes twice from the system)
    private lateinit var commonResourcesWatcher: Closeable
    private lateinit var currentWorldResourcesWatcher: Closeable
    private lateinit var currentLevelResourcesWatcher: Closeable

    enum class AssetType{ None, Common, World, Level }

    fun <T : SerializableConfig> addEntityConfig(name: ConfigName, config: T) {
        entityConfigs[name.value()] = config
    }

    fun <T : SerializableConfig> getConfig(name: ConfigName) : T {
        if (!entityConfigs.containsKey(name.value())) error("AssetStore - getConfig: No config found for name '${name.value()}'!")
        return entityConfigs[name.value()]!! as T
    }

    fun getSound(name: String) : SoundChannel {
        return if (sounds.contains(name)) sounds[name]!!.second
        else error("GameAssets: Sound '$name' not found!")
    }

    fun getImage(name: String, slice: String = "") : ImageData {
        return if (images.contains(name)) {
            if (slice.isEmpty()) {
                images[name]!!.second.default
            } else {
                if (images[name]!!.second[slice] != null) {
                    images[name]!!.second[slice]!!
                } else error("GameAssets: Slice '$slice' of image '$name' not found!")
            }
        } else error("GameAssets: Image '$name' not found!")
    }

    fun getBackground(name: String) : ParallaxDataContainer {
        return if (backgrounds.contains(name)) backgrounds[name]!!.second
        else error("GameAssets: Parallax background '$name' not found!")
    }

    fun getTiledMap(name: String) : TiledMap {
        return if (tiledMaps.contains(name)) tiledMaps[name]!!.second
        else error("GameAssets: TiledMap '$name' not found!")
    }

    fun getFont(name: String) : Font {
        return if (fonts.contains(name)) fonts[name]!!.second
        else error("GameAssets: Cannot find font '$name'!")
    }

    private fun removeAssets(type: AssetType) {
        tiledMaps.entries.iterator().let { while (it.hasNext()) if (it.next().value.first == type) it.remove() }
        backgrounds.entries.iterator().let { while (it.hasNext()) if (it.next().value.first == type) it.remove() }
        images.entries.iterator().let { while (it.hasNext()) if (it.next().value.first == type) it.remove() }
        fonts.entries.iterator().let { while (it.hasNext()) if (it.next().value.first == type) it.remove() }
        sounds.entries.iterator().let { while (it.hasNext()) if (it.next().value.first == type) it.remove() }
    }

    suspend fun loadAssets(assetConfig: AssetModel) {
        val type: AssetType
        val atlas = when {
            assetConfig.assetFolderName.contains(Regex("^common$")) -> {
                if (commonAssetConfig.assetFolderName == "common") {
                    println("INFO: Common assets already loaded! No reload is happening!")
                    return
                }
                type = AssetType.Common
                commonAssetConfig = assetConfig
                commonAtlas
            }
            assetConfig.assetFolderName.contains(Regex("^world[0-9]+$")) -> {
                when (currentWorldAssetConfig.assetFolderName) {
                    "none" -> { /* Just load assets */ }
                    assetConfig.assetFolderName -> {
                        println("INFO: World assets already loaded! No reload is happening!")
                        return
                    }
                    else -> {
                        println("INFO: Remove old world assets and load new ones!")
                        removeAssets(AssetType.World)
                    }
                }
                type = AssetType.World
                currentWorldAssetConfig = assetConfig
                worldAtlas
            }
            assetConfig.assetFolderName.contains(Regex("^world[0-9]+\\/(intro|extro/level[0-9]+)\$")) -> {
                when (currentLevelAssetConfig.assetFolderName) {
                    "none" -> { /* Just load assets */ }
                    assetConfig.assetFolderName -> {
                        println("INFO: World assets already loaded! No reload is happening!")
                        return
                    }
                    else -> {
                        println("INFO: Remove old world assets and load new ones!")
                        removeAssets(AssetType.Level)
                    }
                }
                type = AssetType.Level
                currentLevelAssetConfig = assetConfig
                levelAtlas
            }
            else -> error("LoadAssets: Given asset directory name '${assetConfig.assetFolderName}' is not inline with GameAsset specification!")
        }

        val sw = Stopwatch().start()
        println("GameAssets: Start loading [${type.name}] resources from '${assetConfig.assetFolderName}'...")

        // Update maps of music, images, ...
        assetConfig.tiledMaps.forEach { tiledMap ->
            tiledMaps[tiledMap.key] = Pair(type, resourcesVfs[assetConfig.assetFolderName + "/" + tiledMap.value].readTiledMap(atlas = atlas))
        }
        assetConfig.sounds.forEach { sound ->
            val soundFile = resourcesVfs[assetConfig.assetFolderName + "/" + sound.value].readMusic()
            val soundChannel = soundFile.play()
//            val soundChannel = resourcesVfs[assetConfig.assetFolderName + "/" + sound.value].readSound().play()
            soundChannel.pause()
            sounds[sound.key] = Pair(type, soundChannel)
        }
        assetConfig.backgrounds.forEach { background ->
            backgrounds[background.key] = Pair(type, resourcesVfs[assetConfig.assetFolderName + "/" + background.value.aseName].readParallaxDataContainer(background.value, ASE, atlas = atlas))
        }
        assetConfig.images.forEach { image ->
            images[image.key] = Pair(type,
                if (image.value.layers == null) {
                    resourcesVfs[assetConfig.assetFolderName + "/" + image.value.fileName].readImageDataContainer(ASE.toProps(), atlas = atlas)
                } else {
                    val props = ASE.toProps() // TODO check -- ImageDecodingProps(it.value.fileName, extra = ExtraTypeCreate())
                    props.setExtra("layers", image.value.layers)
                    resourcesVfs[assetConfig.assetFolderName + "/" + image.value.fileName].readImageDataContainer(props, atlas)
                }
            )
        }
        assetConfig.fonts.forEach { font ->
            fonts[font.key] = Pair(type, resourcesVfs[assetConfig.assetFolderName + "/" + font.value].readBitmapFont(atlas = atlas))
        }

        println("Assets: Loaded resources in ${sw.elapsed}")
    }

    suspend fun watchAssetsForChanges(world: World, assetReloadContext: CoroutineContext, assetReloadCache: AssetReloadCache) {
        // This resource watcher will check if one asset file was changed. If yes then it will reload the asset.
        commonResourcesWatcher = resourcesVfs[commonAssetConfig.assetFolderName].watch {
            if (it.kind == Vfs.FileEvent.Kind.MODIFIED) { checkAssetFolders(world, it.file,
                AssetType.Common, commonAssetConfig, assetReloadContext, assetReloadCache) }
        }
        currentWorldResourcesWatcher = resourcesVfs[currentWorldAssetConfig.assetFolderName].watch {
            if (it.kind == Vfs.FileEvent.Kind.MODIFIED) { checkAssetFolders(world, it.file,
                AssetType.World, currentWorldAssetConfig, assetReloadContext, assetReloadCache) }
        }
        currentLevelResourcesWatcher = resourcesVfs[currentLevelAssetConfig.assetFolderName].watch {
            if (it.kind == Vfs.FileEvent.Kind.MODIFIED) { checkAssetFolders(world, it.file,
                AssetType.Level, currentLevelAssetConfig, assetReloadContext, assetReloadCache) }
        }
    }

    private suspend fun checkAssetFolders(world: World, file: VfsFile, type: AssetType, assetConfig: AssetModel, assetReloadContext: CoroutineContext, assetReloadCache: AssetReloadCache) = with (world) {
        assetConfig.backgrounds.forEach { config ->
            if (file.fullName.contains(config.value.aseName) && !reloading) {
                reloading = true  // save that reloading is in progress
                print("Reloading ${file.fullName}... ")

                launchImmediately(context = assetReloadContext) {
                    // Give aseprite more time to finish writing the files
                    kotlinx.coroutines.delay(100)
                    backgrounds[config.key] = Pair(type, resourcesVfs[assetConfig.assetFolderName + "/" + config.value.aseName].readParallaxDataContainer(config.value, ASE, atlas = null))
                    assetReloadCache.backgroundEntities.forEach { entity ->
                        entity[AssetReload].trigger = true
                    }
                    // Guard period until reloading is activated again - this is used for debouncing watch messages
                    kotlinx.coroutines.delay(100)
                    reloading = false
                    println("Finished")
                }
            }
        }
        assetConfig.images.forEach { config ->
            if (file.fullName.contains(config.value.fileName) && !reloading) {
                reloading = true
                print("Reloading ${file.fullName}... ")

                launchImmediately(context = assetReloadContext) {
                    kotlinx.coroutines.delay(100)
                    images[config.key] = Pair(type,
                        if (config.value.layers == null) {
                            resourcesVfs[assetConfig.assetFolderName + "/" + config.value.fileName].readImageDataContainer(ASE.toProps(), atlas = null)
                        } else {
                            val props = ASE.toProps()
                            props.setExtra("layers", config.value.layers)
                            resourcesVfs[assetConfig.assetFolderName + "/" + config.value.fileName].readImageDataContainer(props, atlas = null)
                        }
                    )
                    assetReloadCache.spriteEntities.forEach { entity ->
                        entity[AssetReload].trigger = true
                    }
                    kotlinx.coroutines.delay(100)
                    reloading = false
                    println("Finished")

                }
            }
        }
    }
}
