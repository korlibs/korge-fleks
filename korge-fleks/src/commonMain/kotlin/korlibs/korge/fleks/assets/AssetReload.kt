package korlibs.korge.fleks.assets

import korlibs.io.async.launchImmediately
import korlibs.io.file.*
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.fleks.assets.data.AssetType
import korlibs.korge.fleks.assets.data.LayerTileMaps
import korlibs.korge.fleks.prefab.Prefab
import korlibs.korge.ldtk.view.*
import kotlinx.coroutines.*
import kotlin.collections.set
import kotlin.coroutines.*
import kotlin.native.concurrent.*


/**
 * DSL marker for the [ResourceDirWatcherConfiguration].
 */
@DslMarker
annotation class AssetReloadCfgMarker

/**
 * A DSL class to configure an [AssetUpdater][AssetUpdaterConfiguration] of a [ResourceDirWatcher][ResourceDirWatcherConfiguration].
 */
@AssetReloadCfgMarker
class AssetUpdaterConfiguration(
    internal var type: AssetType,
    internal var toBeEnabled: Boolean = false,
    internal var enabled: Boolean = false,
    internal val imageChangedCallback: MutableList<() -> Unit> = mutableListOf(),
    internal val fontChangedCallback: MutableList<() -> Unit> = mutableListOf(),
    internal val backgroundCallback: MutableList<() -> Unit> = mutableListOf(),
    internal val ldtkLevelMapCallback: MutableList<() -> Unit> = mutableListOf(),
 ) {

    // To be used in View code from Korge objects
    fun onImageChanged(callback: () -> Unit) { imageChangedCallback += callback }
    fun onFontChanged(callback: () -> Unit) { fontChangedCallback += callback }
    fun onBackgroundChanged(callback: () -> Unit) { backgroundCallback += callback }
    fun onLdtkLevelMapChanged(callback: () -> Unit) { ldtkLevelMapCallback += callback }
}

/**
 * A DSL class to configure an [ResourceDirWatcher][ResourceDirWatcherConfiguration].
 */
@AssetReloadCfgMarker
class ResourceDirWatcherConfiguration(
    internal var commonAssetUpdater: AssetUpdaterConfiguration = AssetUpdaterConfiguration(type = AssetType.COMMON),
    internal var worldAssetUpdater: AssetUpdaterConfiguration = AssetUpdaterConfiguration(type = AssetType.WORLD),
    internal var levelAssetUpdater: AssetUpdaterConfiguration = AssetUpdaterConfiguration(type = AssetType.LEVEL),
    internal var specialAssetUpdater: AssetUpdaterConfiguration = AssetUpdaterConfiguration(type = AssetType.SPECIAL),
) {
    private var reloading: Boolean = false  // Used for debouncing reload of config (in case the modification message comes twice from the system)

    fun addAssetWatcherAll(callback: AssetUpdaterConfiguration.() -> Unit) {
        commonAssetUpdater.toBeEnabled = true
        worldAssetUpdater.toBeEnabled = true
        levelAssetUpdater.toBeEnabled = true
        specialAssetUpdater.toBeEnabled = true
        commonAssetUpdater.apply(callback)
        worldAssetUpdater.apply(callback)
        levelAssetUpdater.apply(callback)
        specialAssetUpdater.apply(callback)
    }

    fun addAssetWatcher(type: AssetType, callback: AssetUpdaterConfiguration.() -> Unit) {
        when(type) {
            AssetType.COMMON -> {
                commonAssetUpdater.toBeEnabled = true
                commonAssetUpdater.apply(callback)
            }
            AssetType.WORLD -> {
                worldAssetUpdater.toBeEnabled = true
                worldAssetUpdater.apply(callback)
            }
            AssetType.LEVEL -> {
                levelAssetUpdater.toBeEnabled = true
                levelAssetUpdater.apply(callback)
            }
            AssetType.SPECIAL -> {
                specialAssetUpdater.toBeEnabled = true
                specialAssetUpdater.apply(callback)
            }
        }
    }

    /**
     * Configures the resource directory watcher for each enabled asset type.
     */
    suspend fun configure(assetStore: AssetStore) : ResourceDirWatcherConfiguration {
        enableAssetWatcher(assetStore, commonAssetUpdater, assetStore.commonAssetConfig)
        enableAssetWatcher(assetStore, worldAssetUpdater, assetStore.currentWorldAssetConfig)
        enableAssetWatcher(assetStore, levelAssetUpdater, assetStore.currentLevelAssetConfig)
        enableAssetWatcher(assetStore, specialAssetUpdater, assetStore.specialAssetConfig)
        return this
    }

    private suspend fun enableAssetWatcher(assetStore: AssetStore, assetUpdater: AssetUpdaterConfiguration, assetConfig: AssetModel) {
        if (!assetUpdater.enabled && assetUpdater.toBeEnabled) {
            assetUpdater.enabled = true
            assetUpdater.toBeEnabled = false

            resourcesVfs[assetConfig.folder].apply {
                if (stat().isDirectory) {
                    println("Add watcher for '${path}'")
                    watch {
                        if (it.kind == Vfs.FileEvent.Kind.MODIFIED) {
                            checkAssetFolders(assetStore, it.file, assetUpdater, assetConfig, coroutineContext)
                        }
                    }
                }
            }
        }
    }

    private suspend fun checkAssetFolders(assetStore: AssetStore, file: VfsFile, assetUpdater: AssetUpdaterConfiguration, assetConfig: AssetModel, assetReloadContext: CoroutineContext) {

        // TODO: Currently only fonts, sprite images, background (parallax images) and tile maps are reloaded
        //       -> Implement reloading also for other asset types

//        assetConfig.images.forEach { config ->
//            if (file.fullName.contains(config.value.fileName) && !reloading) {
//                reloading = true
//                println("Reloading ${file.fullName}... ")
//
//                launchImmediately(context = assetReloadContext) {
//                    delay(500)
//                    val assetName = config.key
//                    assetStore.images[assetName] = Pair(assetUpdater.type,
//                        if (config.value.layers == null) {
//                            resourcesVfs[assetConfig.folder + "/" + config.value.fileName].readImageDataContainer(ASE.toProps(), atlas = null)
//                        } else {
//                            val props = ASE.toProps()
//                            props.setExtra("layers", config.value.layers)
//                            resourcesVfs[assetConfig.folder + "/" + config.value.fileName].readImageDataContainer(props, atlas = null)
//                        }
//                    )
//
//                    delay(100)
//                    reloading = false
//                    println("Finished")
//                    assetUpdater.imageChangedCallback.forEach { it.invoke() }
//                }
//            }
//        }
        assetConfig.tileMaps.forEach { config ->
            // Check if LDTK json file was modified
            if (file.fullName.contains(config.value.fileName) && !reloading) {
                reloadLdtkWorld(assetStore, config.value.fileName, config.key, assetUpdater, assetConfig, assetReloadContext)
            }
            // Check if any tileset was modified
            config.value.tileSetPaths.forEach { path ->
                if (file.fullName.contains(path) && !reloading) {
                    reloadLdtkWorld(assetStore, config.value.fileName, config.key, assetUpdater, assetConfig, assetReloadContext)
                }
            }
        }
    }

    private fun reloadLdtkWorld(assetStore: AssetStore, fileName: String, levelName: String, assetUpdater: AssetUpdaterConfiguration, assetConfig: AssetModel, assetReloadContext: CoroutineContext) {
        reloading = true  // save that reloading is in progress
        print("Reloading ${assetConfig.folder + "/" + fileName}... ")
        launchImmediately(context = assetReloadContext) {
            // Give LDtk more time to finish writing the files
            delay(500)

            val ldtkWorld = resourcesVfs[assetConfig.folder + "/" + fileName].readLDTKWorld(extrude = true)
            //println("\nTriggering asset change for LDtk: $fileName")

            // Check if the name of the level is the one for the level chunks
            if (Prefab.levelName == levelName) {
                assetStore.assetLevelDataLoader.reloadAllLevelChunks(ldtkWorld)
            } else {
                // Reload all levels from ldtk world file
                ldtkWorld.ldtk.levels.forEach { ldtkLevel ->
                    assetStore.tileMaps[ldtkLevel.identifier] = Pair(assetUpdater.type, LayerTileMaps(levelName, ldtkWorld, ldtkLevel))
                }
            }

            // Guard period until reloading is activated again - this is used for debouncing watch messages
            delay(100)
            reloading = false
            println("Finished")
            assetUpdater.ldtkLevelMapCallback.forEach { it.invoke() }
        }
    }

    @ThreadLocal
    companion object {
        @PublishedApi
        internal var CURRENT_WATCHER: ResourceDirWatcherConfiguration? = null
    }
}

/**
 * Creates a Watcher for a directory under resources with the given [cfg][ResourceDirWatcherConfiguration].
 *
 * @param cfg the [configuration][ResourceDirWatcherConfiguration] of the [ResourceDirWatcher][ResourceDirWatcherConfiguration]
 * which contains callbacks for each type of assets to be reloaded when they change in Resources directory.
 */
suspend fun AssetStore.configureResourceDirWatcher(cfg: ResourceDirWatcherConfiguration.() -> Unit) {

    if (ResourceDirWatcherConfiguration.CURRENT_WATCHER == null) {
        val resourceDirWatcher = ResourceDirWatcherConfiguration().apply(cfg).configure(this)
        ResourceDirWatcherConfiguration.CURRENT_WATCHER = resourceDirWatcher
    } else {
        ResourceDirWatcherConfiguration.CURRENT_WATCHER!!.apply(cfg).configure(this)
    }
}

/**
 * Configure callbacks for asset reloading in game objects.
 *
 * @param cfg the [configuration][AssetUpdaterConfiguration] for specifying the callbacks which
 * will update the assets in specific game objects.
 */
fun configureAssetUpdater(type: AssetType, cfg: AssetUpdaterConfiguration.() -> Unit) {
    val currentWatcher = ResourceDirWatcherConfiguration.CURRENT_WATCHER
    if (currentWatcher != null) {
        when(type) {
            AssetType.COMMON -> currentWatcher.commonAssetUpdater.apply(cfg)
            AssetType.WORLD -> currentWatcher.worldAssetUpdater.apply(cfg)
            AssetType.LEVEL -> currentWatcher.levelAssetUpdater.apply(cfg)
            AssetType.SPECIAL -> currentWatcher.specialAssetUpdater.apply(cfg)
        }
    } else {
        println("INFO: Asset reloading for type '$type' not applied, because ResourceDirWatcher is not set up!")
    }
}
