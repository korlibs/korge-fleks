package korlibs.korge.fleks.assets.data

import korlibs.image.bitmap.slice
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.assets.configureResourceDirWatcher
import korlibs.korge.fleks.utils.EntityConfigSerializer
import korlibs.time.Stopwatch
import kotlinx.serialization.modules.SerializersModule


class AssetLoader(
    private val assetStore: AssetStore
) {
    private val loadedClusterAssets: MutableSet<String> = mutableSetOf()
    private var configSerializer: EntityConfigSerializer = EntityConfigSerializer()

    /**
     * Register a new serializers module for the entity config serializer.
     */
    fun register(name: String, module: SerializersModule) {
        configSerializer.register(name, module)
    }

    /**
     * Function for loading common game assets.
     */
    suspend fun loadCommonAssets() {
        loadClusterAssets(clusterName = "common")
    }

    /**
     * Function for loading common world chunk assets which are needed for all chunks of a world. This includes the
     * collision shapes for the world chunks.
     */
    suspend fun loadCommonWorldChunkAssets(worldName: String) {
        val sw = Stopwatch().start()
        print("INFO: AssetStore - Start loading world chunk '${worldName}/level_data/common'... ")

        val commonChunkInfo: CommonChunkInfo = configSerializer.json().decodeFromString(resourcesVfs["${worldName}/level_data/common.json"].readString())

        // Get version info
        val major: Int = commonChunkInfo.version[0]
        val minor: Int = commonChunkInfo.version[1]
        val build: Int = commonChunkInfo.version[2]
        // TODO Check later if asset version/build is compatible otherwise convert to new version

        // Load collision shapes for the world chunks
        val collisionShapes = resourcesVfs["${worldName}/level_data/collision_shapes.png"].readBitmap().slice()

        assetStore.worldMapData.init(
            //    size = (Number of chunks * Number of tiles * Size of tile)
            worldWidth = (commonChunkInfo.gridVaniaWidth * commonChunkInfo.chunkWidth * commonChunkInfo.tileSize).toFloat(),
            worldHeight = (commonChunkInfo.gridVaniaHeight * commonChunkInfo.chunkHeight * commonChunkInfo.tileSize).toFloat(),
            gridVaniaWidth = commonChunkInfo.gridVaniaWidth,
            gridVaniaHeight = commonChunkInfo.gridVaniaHeight,
            levelChunkWidth = commonChunkInfo.chunkWidth,
            levelChunkHeight = commonChunkInfo.chunkHeight,
            tileSize = commonChunkInfo.tileSize,
            collisionTiles = commonChunkInfo.collisionTiles,
            collisionShapesBitmapSlice = collisionShapes
        )
        println("- Resources loaded in ${sw.elapsed}")
    }

    /**
     * Function for loading all assets which are needed for a list of world chunks. This includes the JSON file for the
     * chunk data itself and any tile sets and other assets which are needed for the tile maps and game objects of the chunk.
     */
    suspend fun loadWorldChunkAssets(worldName: String, chunkList: List<Int>) {
        // First load chunks and get list of asset clusters which need to be loaded.
        chunkList.forEach { chunk ->
            loadWorldChunkAssets(worldName, chunk)
        }
    }

    /**
     * Function for loading all assets which are needed for a world chunk. This includes the JSON file for the chunk data
     * itself and any tile sets and other assets which are needed for the tile maps and game objects of the chunk.
     */
    suspend fun loadWorldChunkAssets(worldName: String, chunkIndex: Int) {
        val sw = Stopwatch().start()
        print("INFO: AssetStore - Start loading world chunk '${worldName}/level_data/chunk_${chunkIndex}'... ")

        // By deserializing the chunk JSON file the EntityConfig objects for the chunk will be created and registered in the EntityFactory.
        // The chunk asset info will be used to load the tile map and other assets for the chunk and to check if the chunk asset version is
        // compatible with the current version of the game.
        val worldChunk: ChunkAssetInfo = configSerializer.json().decodeFromString(resourcesVfs["${worldName}/level_data/chunk_${chunkIndex}.json"].readString())
        // Add world chunk
        assetStore.worldMapData.chunkMeshes[chunkIndex] = worldChunk
        assetStore.worldMapData.levelGridVania[worldChunk.chunkX, worldChunk.chunkY] = chunkIndex  // TODO remove

        println("- Resources loaded in ${sw.elapsed}")

        // Create list of tile sets for each level map in the chunk - this is needed for the renderer to get the correct tile set objects for rendering the tile maps of the chunk.
        worldChunk.levelMaps.forEach { (_, layer) ->
            // Load cluster assets for the tile map of the chunk if it was not loaded already
            layer.clusterList.forEach { clusterName ->
                loadClusterAssets(worldName, clusterName)
            }
            layer.listOfTileSets = layer.clusterList.map { assetStore.getTileSet(it) }
        }
    }

    internal suspend fun loadClusterAssets(world: String? = null, clusterName: String, hotReloading: Boolean = false) {
        // Keep track was loaded already to avoid reloading of assets which are already in memory.
        val clusterPath = world?.let { "${world}/${clusterName}" } ?: clusterName
        if (loadedClusterAssets.contains(clusterPath)) {
            //println("INFO: Asset cluster '$clusterPath' already loaded! No reload is happening!")
            return
        } else {
            loadedClusterAssets.add(clusterPath)
        }

        val sw = Stopwatch().start()
        print("INFO: AssetStore - Start loading asset cluster '$clusterPath'... ")

// TODO load sounds and music
//            // Update maps of music, images, ...
//            if (!testing) {
//                assetConfig.sounds.forEach { sound ->
//                    val soundFile = resourcesVfs[assetConfig.folder + "/" + sound.fileName].readSound(  //readMusic(
//                        props = AudioDecodingProps(exactTimings = true),
//                        streaming = true
//                    )
////                    val soundChannel = soundFile.decode().toWav().readMusic().play()  // -- convert to WAV
//                    val soundChannel = soundFile.play()
////                    val soundChannel2 = resourcesVfs[assetConfig.folder + "/" + sound.value].readSound().play()
//
//                    soundChannel.pause()
//                    sounds[sound.name] = Pair(type, soundChannel)
//                }
//            }

        resourcesVfs["${clusterPath}/assets.json"].readKorgeFleksClusterAssetJson(clusterName, assetStore)

        println("- Resources loaded in ${sw.elapsed}")

        // TODO hot reloading needs rework!!!
        if (hotReloading) {
            assetStore.configureResourceDirWatcher {
                addAssetWatcher(clusterName) {}
            }
        }
    }
}
