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
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.entity.config.ParallaxBackground.configureParallaxLayers
import korlibs.korge.fleks.familyHooks.onDrawableFamilyAdded
import korlibs.korge.fleks.familyHooks.onDrawableFamilyRemoved
import korlibs.korge.fleks.familyHooks.onSpecificLayerFamilyAdded
import korlibs.korge.fleks.familyHooks.onSpecificLayerFamilyRemoved
import korlibs.korge.parallax.readParallaxDataContainer
import kotlinx.coroutines.delay
import kotlin.coroutines.CoroutineContext

class AssetReload(private val assetStore: AssetStore = AssetStore) {

    private var reloading: Boolean = false  // Used for debouncing reload of config (in case the modification message comes twice from the system)
    private lateinit var commonResourcesWatcher: Closeable
    private lateinit var currentWorldResourcesWatcher: Closeable
    private lateinit var currentLevelResourcesWatcher: Closeable

    private var callback: () -> Unit = {}

    suspend fun watchForChanges(world: World, assetReloadContext: CoroutineContext, block: () -> Unit = {}) {
        callback = block
        resourcesVfs["."].listRecursiveSimple().forEach { file ->
            if (file.stat().isDirectory) {
                println("Add watcher for '${file.path}'")
                file.watch {
                    if (it.kind == Vfs.FileEvent.Kind.MODIFIED) {
                        checkAssetFolders(world, it.file, AssetStore.AssetType.Common, assetStore.commonAssetConfig, assetReloadContext)
                        checkAssetFolders(world, it.file, AssetStore.AssetType.World, assetStore.currentWorldAssetConfig, assetReloadContext)
                        checkAssetFolders(world, it.file, AssetStore.AssetType.Level, assetStore.currentLevelAssetConfig, assetReloadContext)
                    }
                }
            }
        }
    }

    private suspend fun checkAssetFolders(world: World, file: VfsFile, type: AssetStore.AssetType, assetConfig: AssetModel, assetReloadContext: CoroutineContext) = with (world) {

        // TODO: Currently only sprite images and parallax images are reloaded
        //       -> Implement reloading also for other asset types

        assetConfig.backgrounds.forEach { config ->
            if (file.fullName.contains(config.value.aseName) && !reloading) {
                reloading = true  // save that reloading is in progress
                println("Reloading ${file.fullName}... ")

                launchImmediately(context = assetReloadContext) {
                    // Give aseprite more time to finish writing the files
                    delay(100)
                    val assetName = config.key
                    assetStore.backgrounds[assetName] = Pair(type, resourcesVfs[assetConfig.assetFolderName + "/" + config.value.aseName].readParallaxDataContainer(config.value, ASE, atlas = null))

                    println("\nTriggering asset change for: $assetName")
                    world.family { all(Drawable) }.forEach { entity ->
                        if (entity has Parallax && entity[Parallax].config.name == assetName) {
                            println("Updating sprite data in entity: ${entity.id}")
                            onDrawableFamilyRemoved(world, entity)
                            onDrawableFamilyAdded(world, entity)

                            // We need to update the layer config for the parallax entity - create AnimationScript entity and execute a config function for the parallax entity
                            world.entity {
                                it += AnimationScript(
                                    tweens = listOf(ExecuteConfigFunction(entity = entity, config = entity[Parallax].config, function = configureParallaxLayers))
                                )
                            }
                        }
                    }
                    world.family { all(SpecificLayer) }.forEach { entity ->
                        if (entity[SpecificLayer].parentEntity has Parallax && entity[SpecificLayer].parentEntity[Parallax].config.name == assetName) {
                            println("Updating layer in entity: ${entity.id} - layer: ${entity[SpecificLayer].spriteLayer}")
                            onSpecificLayerFamilyRemoved(world, entity)
                            entity.getOrNull(PositionShape)?.let { it.initialized = false }  // reset position otherwise position data will not be initialized with updated view data (x, y)
                            onSpecificLayerFamilyAdded(world, entity)
                        }
                    }
                    // Guard period until reloading is activated again - this is used for debouncing watch messages
                    delay(100)
                    reloading = false
                    println("Finished")

                    callback.invoke()
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

                    callback.invoke()
                }
            }
        }
    }
}
