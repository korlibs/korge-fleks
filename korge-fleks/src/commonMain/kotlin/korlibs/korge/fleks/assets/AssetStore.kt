package korlibs.korge.fleks.assets

import com.github.quillraven.fleks.World
import korlibs.audio.sound.SoundChannel
import korlibs.audio.sound.readMusic
import korlibs.datastructure.setExtra
import korlibs.image.atlas.MutableAtlasUnit
import korlibs.image.bitmap.*
import korlibs.image.font.Font
import korlibs.image.font.readBitmapFont
import korlibs.image.format.*
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.fleks.utils.Identifier
import korlibs.korge.parallax.ParallaxDataContainer
import korlibs.korge.parallax.readParallaxDataContainer
import korlibs.time.Stopwatch
import kotlin.collections.set
import kotlin.concurrent.*
import kotlin.coroutines.CoroutineContext


interface ConfigBase

/**
 * This class is responsible to load all kind of game data and make it usable / consumable by entities of Korge-Fleks.
 *
 * Assets are separated into 'common', 'world' and 'level' types. The common type means that the asset is used throughout
 * the game. So it makes sense to not reload those assets on every level or world. The same applies also for world type.
 * It means a world-asset is used in all levels of a world. An asset of type 'level' means that it is really only used in
 * one level (e.g. a level-end boss or other level specific graphics).
 */
object AssetStore {
    val commonAtlas: MutableAtlasUnit = MutableAtlasUnit(1024, 2048, border = 1)
    val worldAtlas: MutableAtlasUnit = MutableAtlasUnit(1024, 2048, border = 1)
    val levelAtlas: MutableAtlasUnit = MutableAtlasUnit(1024, 2048, border = 1)

//    @Volatile
    internal var commonAssetConfig: AssetModel = AssetModel()
    internal var currentWorldAssetConfig: AssetModel = AssetModel()
    internal var currentLevelAssetConfig: AssetModel = AssetModel()

    var entityConfigs: MutableMap<String, ConfigBase> = mutableMapOf()
// TODO    private var tiledMaps: MutableMap<String, Pair<AssetType, TiledMap>> = mutableMapOf()
    internal var backgrounds: MutableMap<String, Pair<AssetType, ParallaxDataContainer>> = mutableMapOf()
    internal var images: MutableMap<String, Pair<AssetType, ImageDataContainer>> = mutableMapOf()
    private val ninePatches: MutableMap<String, Pair<AssetType, NinePatchBmpSlice>> = mutableMapOf()
    private var fonts: MutableMap<String, Pair<AssetType, Font>> = mutableMapOf()
    private var sounds: MutableMap<String, Pair<AssetType, SoundChannel>> = mutableMapOf()

    private val assetReload = AssetReload(assetStore = this)

    suspend fun watchForChanges(world: World, assetReloadContext: CoroutineContext) = assetReload.watchForChanges(world, assetReloadContext)

    enum class AssetType{ None, Common, World, Level }

    fun <T : ConfigBase> addEntityConfig(identifier: Identifier, entityConfig: T) {
        entityConfigs[identifier.name] = entityConfig
    }

    inline fun <reified T : ConfigBase> getEntityConfig(identifier: Identifier) : T {
        val config: ConfigBase = entityConfigs[identifier.name] ?: error("AssetStore - getConfig: No config found for configId name '${identifier.name}'!")
        if (config !is T) error("AssetStore - getConfig: Config for '${identifier.name}' is not of type ${T::class}!")
        return config
    }

    fun getSound(name: String) : SoundChannel =
        if (sounds.contains(name)) sounds[name]!!.second
        else error("AssetStore: Sound '$name' not found!")

    fun getImage(name: String, slice: String = "") : ImageData =
        if (images.contains(name)) {
            if (slice.isEmpty()) {
                images[name]!!.second.default
            } else {
                if (images[name]!!.second[slice] != null) {
                    images[name]!!.second[slice]!!
                } else error("AssetStore: Slice '$slice' of image '$name' not found!")
            }
        } else error("AssetStore: Image '$name' not found!")

    fun getNinePatch(name: String) : NinePatchBmpSlice =
        if (ninePatches.contains(name)) ninePatches[name]!!.second
        else error("AssetStore: Ninepatch image '$name' not found!")

    fun getBackground(assetConfig: Identifier) : ParallaxDataContainer =
        if (backgrounds.contains(assetConfig.name)) backgrounds[assetConfig.name]!!.second
        else error("AssetStore: Parallax background '${assetConfig.name}' not found!")

// TODO
//    fun getTiledMap(name: String) : TiledMap {
//        return if (tiledMaps.contains(name)) tiledMaps[name]!!.second
//        else error("AssetStore: TiledMap '$name' not found!")
//    }

    fun getFont(name: String) : Font {
        return if (fonts.contains(name)) fonts[name]!!.second
        else error("AssetStore: Cannot find font '$name'!")
    }

    private fun removeAssets(type: AssetType) {
// TODO        tiledMaps.entries.iterator().let { while (it.hasNext()) if (it.next().value.first == type) it.remove() }
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
            assetConfig.assetFolderName.contains(Regex("^world[0-9]+\\/(intro[0-9]+|extro/level[0-9]+)\$")) -> {
                when (currentLevelAssetConfig.assetFolderName) {
                    "none" -> { /* Just load assets */ }
                    assetConfig.assetFolderName -> {
                        println("INFO: Level assets already loaded! No reload is happening!")
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
        println("AssetStore: Start loading [${type.name}] resources from '${assetConfig.assetFolderName}'...")

        // Update maps of music, images, ...
// TODO
//        assetConfig.tiledMaps.forEach { tiledMap ->
//            tiledMaps[tiledMap.key] = Pair(type, resourcesVfs[assetConfig.assetFolderName + "/" + tiledMap.value].readTiledMap(atlas = atlas))
//        }
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
        assetConfig.ninePatches.forEach { ninePatch ->
            ninePatches[ninePatch.key] = Pair(type, resourcesVfs[assetConfig.assetFolderName + "/" + ninePatch.value].readNinePatch())
        }
        assetConfig.fonts.forEach { font ->
            fonts[font.key] = Pair(type, resourcesVfs[assetConfig.assetFolderName + "/" + font.value].readBitmapFont(atlas = atlas))
        }
        assetConfig.entityConfigs.forEach { config ->
            entityConfigs[config.key] = config.value
        }

        println("Assets: Loaded resources in ${sw.elapsed}")
    }
}
