package korlibs.korge.fleks.assets

import korlibs.datastructure.setExtra
import korlibs.image.font.*
import korlibs.image.format.ASE
import korlibs.image.format.readImageDataContainer
import korlibs.image.format.toProps
import korlibs.io.async.launchImmediately
import korlibs.io.file.Vfs
import korlibs.io.file.VfsFile
import korlibs.io.file.fullName
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.parallax.readParallaxDataContainer
import kotlinx.coroutines.*
import kotlin.coroutines.*
import kotlin.native.concurrent.*


class AssetsWatcherConfiguration {
    internal var type: AssetType = AssetType.None
    internal var toBeStarted: Boolean = false
    internal var enabled: Boolean = false
    internal var imageChangedCallback: MutableList<() -> Unit> = mutableListOf()
    internal var fontChangedCallback: MutableList<() -> Unit> = mutableListOf()
    internal var backgroundCallback: MutableList<() -> Unit> = mutableListOf()

    fun onImageChanged(callback: () -> Unit) { imageChangedCallback += callback }

    fun onFontChanged(callback: () -> Unit) { fontChangedCallback += callback }

    fun onBackgroundChanged(callback: () -> Unit) { backgroundCallback += callback }
}

class ResourceDirWatcherConfiguration {
    internal var commonAssetsWatcher = AssetsWatcherConfiguration()
    internal var worldAssetsWatcher = AssetsWatcherConfiguration()
    internal var levelAssetsWatcher = AssetsWatcherConfiguration()
    internal var specialAssetsWatcher = AssetsWatcherConfiguration()

    private var reloading: Boolean = false  // Used for debouncing reload of config (in case the modification message comes twice from the system)

    fun addAssetWatcherAll(callback: AssetsWatcherConfiguration.() -> Unit) {
        commonAssetsWatcher.enabled = true
        worldAssetsWatcher.enabled = true
        levelAssetsWatcher.enabled = true
        specialAssetsWatcher.enabled = true
        commonAssetsWatcher.apply(callback)
        worldAssetsWatcher.apply(callback)
        levelAssetsWatcher.apply(callback)
        specialAssetsWatcher.apply(callback)
    }

    fun addAssetWatcher(type: AssetType, callback: AssetsWatcherConfiguration.() -> Unit) {
        when(type) {
            AssetType.None -> {}  // Do nothing
            AssetType.Common -> {
                commonAssetsWatcher.toBeStarted = true
                commonAssetsWatcher.apply(callback)
            }
            AssetType.World -> {
                worldAssetsWatcher.toBeStarted = true
                worldAssetsWatcher.apply(callback)
            }
            AssetType.Level -> {
                levelAssetsWatcher.toBeStarted = true
                levelAssetsWatcher.apply(callback)
            }
            AssetType.Special -> {
                specialAssetsWatcher.toBeStarted = true
                specialAssetsWatcher.apply(callback)
            }
        }
    }

    /**
     * Configures the resource directory watcher for each enabled asset type.
     */
    suspend fun configure() : ResourceDirWatcherConfiguration {
        enableAssetWatcher(commonAssetsWatcher, AssetStore.commonAssetConfig)
        enableAssetWatcher(worldAssetsWatcher, AssetStore.currentWorldAssetConfig)
        enableAssetWatcher(levelAssetsWatcher, AssetStore.currentLevelAssetConfig)
        enableAssetWatcher(specialAssetsWatcher, AssetStore.specialAssetConfig)
        return this
    }

    private suspend fun enableAssetWatcher(assetsWatcher: AssetsWatcherConfiguration, assetConfig: AssetModel) {
        if (!assetsWatcher.enabled && assetsWatcher.toBeStarted) {
            assetsWatcher.enabled = true
            assetsWatcher.toBeStarted = false

            resourcesVfs[assetConfig.folderName].apply {
                if (stat().isDirectory) {
                    println("Add watcher for '${path}'")
                    watch {
                        if (it.kind == Vfs.FileEvent.Kind.MODIFIED) {
                            checkAssetFolders(it.file, assetsWatcher, assetConfig, coroutineContext)
                        }
                    }
                }
            }
        }

    }

    private suspend fun checkAssetFolders(file: VfsFile, assetsWatcherCfg: AssetsWatcherConfiguration, assetConfig: AssetModel, assetReloadContext: CoroutineContext) {

        // TODO: Currently only fonts, sprite images and parallax images are reloaded
        //       -> Implement reloading also for other asset types

        assetConfig.fonts.forEach { config ->
            // Check filename
            if (file.fullName.contains(config.value.removeSuffix(".fnt")) && !reloading) {
                reloading = true  // save that reloading is in progress
                println("Reloading ${assetConfig.folderName}/${config.value} for changes in ${file.fullName} ... ")

                launchImmediately(context = assetReloadContext) {
                    delay(500)
                    val assetName = config.key
                    AssetStore.fonts[assetName] = Pair(assetsWatcherCfg.type, resourcesVfs[assetConfig.folderName + "/" + config.value].readBitmapFont(atlas = null))
                }

                delay(100)
                reloading = false
                println("Finished")
                assetsWatcherCfg.fontChangedCallback.forEach { it.invoke() }
            }
        }
        assetConfig.images.forEach { config ->
            if (file.fullName.contains(config.value.fileName) && !reloading) {
                reloading = true
                println("Reloading ${file.fullName}... ")

                launchImmediately(context = assetReloadContext) {
                    delay(500)
                    val assetName = config.key
                    AssetStore.images[assetName] = Pair(assetsWatcherCfg.type,
                        if (config.value.layers == null) {
                            resourcesVfs[assetConfig.folderName + "/" + config.value.fileName].readImageDataContainer(ASE.toProps(), atlas = null)
                        } else {
                            val props = ASE.toProps()
                            props.setExtra("layers", config.value.layers)
                            resourcesVfs[assetConfig.folderName + "/" + config.value.fileName].readImageDataContainer(props, atlas = null)
                        }
                    )

                    delay(100)
                    reloading = false
                    println("Finished")
                    assetsWatcherCfg.imageChangedCallback.forEach { it.invoke() }
                }
            }
        }
        assetConfig.backgrounds.forEach { config ->
            if (file.fullName.contains(config.value.aseName) && !reloading) {
                reloading = true  // save that reloading is in progress
                println("Reloading ${file.fullName}... ")

                launchImmediately(context = assetReloadContext) {
                    // Give aseprite more time to finish writing the files
                    delay(100)
                    val assetName = config.key
                    AssetStore.backgrounds[assetName] = Pair(assetConfig.type, resourcesVfs[assetConfig.folderName + "/" + config.value.aseName].readParallaxDataContainer(config.value, ASE, atlas = null))

                    println("\nTriggering asset change for: $assetName")
                    // Guard period until reloading is activated again - this is used for debouncing watch messages
                    delay(100)
                    reloading = false
                    println("Finished")
                    assetsWatcherCfg.backgroundCallback.forEach { it.invoke() }
                }
            }
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
suspend fun configureResourceDirWatcher(cfg: ResourceDirWatcherConfiguration.() -> Unit) {

    if (ResourceDirWatcherConfiguration.CURRENT_WATCHER == null) {
        val resourceDirWatcher = ResourceDirWatcherConfiguration().apply(cfg).configure()
        ResourceDirWatcherConfiguration.CURRENT_WATCHER = resourceDirWatcher
    } else {
        ResourceDirWatcherConfiguration.CURRENT_WATCHER!!.apply(cfg).configure()
    }
}

/**
 * Configure callbacks for asset reloading in game objects.
 *
 * @param cfg the [configuration][AssetsWatcherConfiguration] for specifying the callbacks which
 * will update the assets in specific game objects.
 */
fun configureAssetReloading(type: AssetType, cfg: AssetsWatcherConfiguration.() -> Unit) {
    val currentWatcher = ResourceDirWatcherConfiguration.CURRENT_WATCHER
    if (currentWatcher != null) {
        when(type) {
            AssetType.None -> {}
            AssetType.Common -> currentWatcher.commonAssetsWatcher.apply(cfg)
            AssetType.World -> currentWatcher.worldAssetsWatcher.apply(cfg)
            AssetType.Level -> currentWatcher.levelAssetsWatcher.apply(cfg)
            AssetType.Special -> currentWatcher.specialAssetsWatcher.apply(cfg)
        }
    } else {
        println("INFO: Asset reloading for type '$type' not applied, because ResourceDirWatcher is not set up!")
    }
}
