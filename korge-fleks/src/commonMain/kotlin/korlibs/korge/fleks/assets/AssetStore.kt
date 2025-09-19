package korlibs.korge.fleks.assets

import korlibs.audio.format.*
import korlibs.audio.sound.*
import korlibs.datastructure.*
import korlibs.image.atlas.Atlas
import korlibs.image.atlas.MutableAtlasUnit
import korlibs.image.atlas.readAtlas
import korlibs.image.bitmap.*
import korlibs.image.font.Font
import korlibs.image.font.readBitmapFont
import korlibs.image.format.*
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.fleks.assets.data.AssetLoader
import korlibs.korge.fleks.assets.data.AssetType
import korlibs.korge.fleks.assets.data.GameObjectConfig
import korlibs.korge.fleks.assets.data.LayerTileMaps
import korlibs.korge.fleks.assets.data.ParallaxDataContainer
import korlibs.korge.fleks.assets.data.SpriteAnimFrames
import korlibs.korge.fleks.assets.data.SpriteFrame
import korlibs.korge.fleks.assets.data.readParallaxDataContainer
import korlibs.korge.ldtk.view.*
import korlibs.korge.view.SpriteAnimation
import korlibs.time.Stopwatch
import kotlin.collections.set


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

    internal val tileMaps: MutableMap<String, Pair<AssetType, LayerTileMaps>> = mutableMapOf()
    internal val backgrounds: MutableMap<String, Pair<AssetType, ParallaxDataContainer>> = mutableMapOf()
    internal val images: MutableMap<String, Pair<AssetType, ImageDataContainer>> = mutableMapOf()
    internal val fonts: MutableMap<String, Pair<AssetType, Font>> = mutableMapOf()
    internal val sounds: MutableMap<String, Pair<AssetType, SoundChannel>> = mutableMapOf()
    internal val gameObjectConfig: MutableMap<String, GameObjectConfig> = mutableMapOf()

    internal val textureAtlases: MutableList<Pair<AssetType, Atlas>> = mutableListOf()
    internal val textures: MutableMap<String, Pair<AssetType, SpriteAnimFrames>> = mutableMapOf()




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

    fun getTexture() {
//        textureAtlases.first().second.entriesMap[""]?.info
    }

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

            // Update maps of music, images, ...
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
            assetConfig.textureAtlas.forEach { atlas ->
                // TODO save map of images for each sprite
                val spriteAtlas = resourcesVfs[assetConfig.folder + "/" + atlas].readAtlas()
                spriteAtlas.entries.forEach { entry ->
                    //println("sprite: ${entry.name}")

                    val regex = "_\\d+$".toRegex()
                    if (regex.containsMatchIn(entry.name)) {
                        // entry is part of a sprite animation
                        val spriteName = entry.name.replace(regex, "")
                        // Get the animation index number
                        val regex = "_(\\d+)$".toRegex()
                        val match = regex.find(entry.name)
                        val animIndex = match?.groupValues?.get(1)?.toInt() ?: error("Cannot get animation index of sprite '${entry.name}'!")
                        if (textures.containsKey(spriteName)) {
                            textures[spriteName]!!.second.sprites.add(animIndex, SpriteFrame(
                                entry.texture,
                                entry.info.virtFrame?.x ?: 0,
                                entry.info.virtFrame?.y ?: 0,
                                entry.info.virtFrame?.width ?: 0,
                                entry.info.virtFrame?.height ?: 0
                            ))
                        } else {
                            val spriteSourceSize = entry.info.virtFrame
                            val spriteAnimFrame = SpriteAnimFrames()
                            spriteAnimFrame.sprites.add(
                                SpriteFrame(
                                    entry.texture,
                                    spriteSourceSize?.x ?: 0,
                                    spriteSourceSize?.y ?: 0,
                                    entry.info.virtFrame?.width ?: 0,
                                    entry.info.virtFrame?.height ?: 0
                                )
                            )
                            textures[spriteName] = Pair(type, spriteAnimFrame)
                        }
                    } else {
                        // entry is not part of a sprite animation
                        val spriteSourceSize = entry.info.virtFrame
                        val spriteAnimFrame = SpriteAnimFrames()
                        spriteAnimFrame.sprites.add(
                            SpriteFrame(
                                entry.texture,
                                spriteSourceSize?.x ?: 0,
                                spriteSourceSize?.y ?: 0,
                                entry.info.virtFrame?.width ?: 0,
                                entry.info.virtFrame?.height ?: 0
                            )
                        )
                        textures[entry.name] = Pair(type, spriteAnimFrame)
                    }
                }
                //println()
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
        // TODO: In case of SPECIAL assets we need to consider the level chunk position of the asset

        tileMaps.values.removeAll { it.first == type }
        backgrounds.values.removeAll { it.first == type }
        images.values.removeAll { it.first == type }
        fonts.values.removeAll { it.first == type }
        sounds.values.removeAll { it.first == type }
//        textureAtlases.removeAll { it.first == type }
    }
}
