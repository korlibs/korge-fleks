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
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.familyHooks.*
import korlibs.korge.fleks.utils.AssetReloadCache
import korlibs.korge.fleks.utils.EntityConfigId
import korlibs.korge.fleks.utils.PolymorphicEnumSerializer
import korlibs.korge.fleks.utils.SnapshotSerializer
import korlibs.korge.parallax.ParallaxDataContainer
import korlibs.korge.parallax.readParallaxDataContainer
import korlibs.time.Stopwatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newCoroutineContext
import kotlinx.serialization.modules.polymorphic
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext


interface EntityConfig

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

    internal var commonAssetConfig: AssetModel = AssetModel()
    internal var currentWorldAssetConfig: AssetModel = AssetModel()
    internal var currentLevelAssetConfig: AssetModel = AssetModel()

    private var entityConfigs: MutableMap<String, EntityConfig> = mutableMapOf()
    private var tiledMaps: MutableMap<String, Pair<AssetType, TiledMap>> = mutableMapOf()
    internal var backgrounds: MutableMap<String, Pair<AssetType, ParallaxDataContainer>> = mutableMapOf()
    internal var images: MutableMap<String, Pair<AssetType, ImageDataContainer>> = mutableMapOf()
    private var fonts: MutableMap<String, Pair<AssetType, Font>> = mutableMapOf()
    private var sounds: MutableMap<String, Pair<AssetType, SoundChannel>> = mutableMapOf()

    enum class AssetType{ None, Common, World, Level }

    fun <T : EntityConfig> addEntityConfig(entityConfigId: EntityConfigId, entityConfig: T) {
        entityConfigs[entityConfigId.name] = entityConfig
    }

    fun <T : EntityConfig> getEntityConfig(entityConfigId: EntityConfigId) : T {
        if (!entityConfigs.containsKey(entityConfigId.name)) error("AssetStore - getConfig: No config found for configId name '${entityConfigId.name}'!")
        return entityConfigs[entityConfigId.name]!! as T
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
}
