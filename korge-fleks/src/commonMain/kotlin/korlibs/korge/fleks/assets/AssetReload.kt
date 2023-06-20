package korlibs.korge.fleks.assets

import com.github.quillraven.fleks.World
import korlibs.datastructure.setExtra
import korlibs.image.format.ASE
import korlibs.image.format.readImageDataContainer
import korlibs.image.format.toProps
import korlibs.io.async.launchImmediately
import korlibs.io.file.Vfs
import korlibs.io.file.VfsFile
import korlibs.io.file.fullName
import korlibs.io.file.std.resourcesVfs
import korlibs.io.lang.Closeable
import korlibs.korge.fleks.components.Drawable
import korlibs.korge.fleks.components.PositionShape
import korlibs.korge.fleks.components.SpecificLayer
import korlibs.korge.fleks.components.Sprite
import korlibs.korge.fleks.familyHooks.onDrawableFamilyAdded
import korlibs.korge.fleks.familyHooks.onDrawableFamilyRemoved
import korlibs.korge.fleks.familyHooks.onSpecificLayerFamilyAdded
import korlibs.korge.fleks.familyHooks.onSpecificLayerFamilyRemoved
import korlibs.korge.parallax.readParallaxDataContainer
import kotlinx.coroutines.delay
import kotlin.coroutines.CoroutineContext

class AssetReload(private val assetStore: AssetStore) {

    private var reloading: Boolean = false  // Used for debouncing reload of config (in case the modification message comes twice from the system)
    private lateinit var commonResourcesWatcher: Closeable
    private lateinit var currentWorldResourcesWatcher: Closeable
    private lateinit var currentLevelResourcesWatcher: Closeable

    suspend fun watchForChanges(world: World, assetReloadContext: CoroutineContext) {
        // This resource watcher will check if one asset file was changed. If yes then it will reload the asset.
        if (assetStore.commonAssetConfig.assetFolderName != "none") commonResourcesWatcher = resourcesVfs[assetStore.commonAssetConfig.assetFolderName].watch {
            if (it.kind == Vfs.FileEvent.Kind.MODIFIED) { checkAssetFolders(world, it.file,
                AssetStore.AssetType.Common, assetStore.commonAssetConfig, assetReloadContext) }
        }
        if (assetStore.currentWorldAssetConfig.assetFolderName != "none") currentWorldResourcesWatcher = resourcesVfs[assetStore.currentWorldAssetConfig.assetFolderName].watch {
            if (it.kind == Vfs.FileEvent.Kind.MODIFIED) { checkAssetFolders(world, it.file,
                AssetStore.AssetType.World, assetStore.currentWorldAssetConfig, assetReloadContext) }
        }
        if (assetStore.currentLevelAssetConfig.assetFolderName != "none") currentLevelResourcesWatcher = resourcesVfs[assetStore.currentLevelAssetConfig.assetFolderName].watch {
            if (it.kind == Vfs.FileEvent.Kind.MODIFIED) { checkAssetFolders(world, it.file,
                AssetStore.AssetType.Level, assetStore.currentLevelAssetConfig, assetReloadContext) }
        }
    }

    private suspend fun checkAssetFolders(world: World, file: VfsFile, type: AssetStore.AssetType, assetConfig: AssetModel, assetReloadContext: CoroutineContext) = with (world) {

        // TODO: Currently only sprite images and parallax images are reloaded
        //       -> Implement reloading also for other asset types

        assetConfig.backgrounds.forEach { config ->
            if (file.fullName.contains(config.value.aseName) && !reloading) {
                reloading = true  // save that reloading is in progress
                print("Reloading ${file.fullName}... ")

                launchImmediately(context = assetReloadContext) {
                    // Give aseprite more time to finish writing the files
                    delay(100)
                    assetStore.backgrounds[config.key] = Pair(type, resourcesVfs[assetConfig.assetFolderName + "/" + config.value.aseName].readParallaxDataContainer(config.value, ASE, atlas = null))

                    // TODO

                    // Guard period until reloading is activated again - this is used for debouncing watch messages
                    delay(100)
                    reloading = false
                    println("Finished")
                }
            }
        }
        assetConfig.images.forEach { config ->
            if (file.fullName.contains(config.value.fileName) && !reloading) {
                reloading = true
                println("Reloading ${file.fullName}... ")

                launchImmediately(context = assetReloadContext) {
                    delay(100)
                    val assetName = config.key
                    assetStore.images[assetName] = Pair(type,
                        if (config.value.layers == null) {
                            resourcesVfs[assetConfig.assetFolderName + "/" + config.value.fileName].readImageDataContainer(ASE.toProps(), atlas = null)
                        } else {
                            val props = ASE.toProps()
                            props.setExtra("layers", config.value.layers)
                            resourcesVfs[assetConfig.assetFolderName + "/" + config.value.fileName].readImageDataContainer(props, atlas = null)
                        }
                    )


                    println("\nTriggering asset change for: $assetName")

                    world.family { all(Drawable) }.forEach { entity ->
                        if (entity has Sprite && entity[Sprite].assetName == assetName) {
                            println("Updating sprite data in entity: ${entity.id}")
                            onDrawableFamilyRemoved(world, entity)
                            onDrawableFamilyAdded(world, entity)
                        }
                    }
                    world.family { all(SpecificLayer) }.forEach { entity ->
                        if (entity[SpecificLayer].parentEntity has Sprite && entity[SpecificLayer].parentEntity[Sprite].assetName == assetName) {
                            println("Updating layer in entity: ${entity.id} - layer: ${entity[SpecificLayer].spriteLayer}")
                            onSpecificLayerFamilyRemoved(world, entity)
                            entity.getOrNull(PositionShape)?.let { it.initialized = false }  // reset position otherwise position data will not be initialized with updated view data (x, y)
                            onSpecificLayerFamilyAdded(world, entity)
                        }
                    }

                    delay(100)
                    reloading = false
                    println("Finished")

                }
            }
        }
    }
}