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
    internal var type: AssetType = AssetType.None,
    internal var toBeEnabled: Boolean = false,
    internal var enabled: Boolean = false,
    internal val imageChangedCallback: MutableList<() -> Unit> = mutableListOf(),
    internal val fontChangedCallback: MutableList<() -> Unit> = mutableListOf(),
    internal val backgroundCallback: MutableList<() -> Unit> = mutableListOf(),
 ) {

    fun onImageChanged(callback: () -> Unit) { imageChangedCallback += callback }

    fun onFontChanged(callback: () -> Unit) { fontChangedCallback += callback }

    fun onBackgroundChanged(callback: () -> Unit) { backgroundCallback += callback }
}

/**
 * A DSL class to configure an [ResourceDirWatcher][ResourceDirWatcherConfiguration].
 */
@AssetReloadCfgMarker
class ResourceDirWatcherConfiguration(
    internal var commonAssetUpdater: AssetUpdaterConfiguration = AssetUpdaterConfiguration(),
    internal var worldAssetUpdater: AssetUpdaterConfiguration = AssetUpdaterConfiguration(),
    internal var levelAssetUpdater: AssetUpdaterConfiguration = AssetUpdaterConfiguration(),
    internal var specialAssetUpdater: AssetUpdaterConfiguration = AssetUpdaterConfiguration(),
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
            AssetType.None -> {}  // Do nothing
            AssetType.Common -> {
                commonAssetUpdater.toBeEnabled = true
                commonAssetUpdater.apply(callback)
            }
            AssetType.World -> {
                worldAssetUpdater.toBeEnabled = true
                worldAssetUpdater.apply(callback)
            }
            AssetType.Level -> {
                levelAssetUpdater.toBeEnabled = true
                levelAssetUpdater.apply(callback)
            }
            AssetType.Special -> {
                specialAssetUpdater.toBeEnabled = true
                specialAssetUpdater.apply(callback)
            }
        }
    }

    /**
     * Configures the resource directory watcher for each enabled asset type.
     */
    suspend fun configure() : ResourceDirWatcherConfiguration {
        enableAssetWatcher(commonAssetUpdater, AssetStore.commonAssetConfig)
        enableAssetWatcher(worldAssetUpdater, AssetStore.currentWorldAssetConfig)
        enableAssetWatcher(levelAssetUpdater, AssetStore.currentLevelAssetConfig)
        enableAssetWatcher(specialAssetUpdater, AssetStore.specialAssetConfig)
        return this
    }

    private suspend fun enableAssetWatcher(assetUpdater: AssetUpdaterConfiguration, assetConfig: AssetModel) {
        if (!assetUpdater.enabled && assetUpdater.toBeEnabled) {
            assetUpdater.enabled = true
            assetUpdater.toBeEnabled = false

            resourcesVfs[assetConfig.folderName].apply {
                if (stat().isDirectory) {
                    println("Add watcher for '${path}'")
                    watch {
                        if (it.kind == Vfs.FileEvent.Kind.MODIFIED) {
                            checkAssetFolders(it.file, assetUpdater, assetConfig, coroutineContext)
                        }
                    }
                }
            }
        }

    }

    private suspend fun checkAssetFolders(file: VfsFile, assetUpdater: AssetUpdaterConfiguration, assetConfig: AssetModel, assetReloadContext: CoroutineContext) {

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
                    AssetStore.fonts[assetName] = Pair(assetUpdater.type, resourcesVfs[assetConfig.folderName + "/" + config.value].readBitmapFont(atlas = null))
                }

                delay(100)
                reloading = false
                println("Finished")
                assetUpdater.fontChangedCallback.forEach { it.invoke() }
            }
        }
        assetConfig.images.forEach { config ->
            if (file.fullName.contains(config.value.fileName) && !reloading) {
                reloading = true
                println("Reloading ${file.fullName}... ")

                launchImmediately(context = assetReloadContext) {
                    delay(500)
                    val assetName = config.key
                    AssetStore.images[assetName] = Pair(assetUpdater.type,
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
                    assetUpdater.imageChangedCallback.forEach { it.invoke() }
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
                    assetUpdater.backgroundCallback.forEach { it.invoke() }
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
 * @param cfg the [configuration][AssetUpdaterConfiguration] for specifying the callbacks which
 * will update the assets in specific game objects.
 */
fun configureAssetUpdater(type: AssetType, cfg: AssetUpdaterConfiguration.() -> Unit) {
    val currentWatcher = ResourceDirWatcherConfiguration.CURRENT_WATCHER
    if (currentWatcher != null) {
        when(type) {
            AssetType.None -> {}
            AssetType.Common -> currentWatcher.commonAssetUpdater.apply(cfg)
            AssetType.World -> currentWatcher.worldAssetUpdater.apply(cfg)
            AssetType.Level -> currentWatcher.levelAssetUpdater.apply(cfg)
            AssetType.Special -> currentWatcher.specialAssetUpdater.apply(cfg)
        }
    } else {
        println("INFO: Asset reloading for type '$type' not applied, because ResourceDirWatcher is not set up!")
    }
}
