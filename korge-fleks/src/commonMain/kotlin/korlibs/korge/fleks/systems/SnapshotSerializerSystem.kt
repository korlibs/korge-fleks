package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import korlibs.io.async.*
import korlibs.io.file.std.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*
import kotlinx.serialization.modules.*
import kotlin.coroutines.*


class SnapshotSerializerSystem(module: SerializersModule) : IntervalSystem(
    interval = Fixed(step = 1/30.0f)
) {

    private val snapshotSerializer = SnapshotSerializer().apply {
        register("module", module)
    }

    private val recording: MutableList<String> = mutableListOf()
    private var rewindSeek: Int = 0
    private var gameRunning: Boolean = true

    // TODO remove later
    private var worldSnapshot: String? = null

    companion object {
        fun SystemConfiguration.setup(module: SerializersModule) = add(SnapshotSerializerSystem(module))
    }

    override fun onTick() {
        val jsonSnapshot = snapshotSerializer.json().encodeToString(world.snapshot())
        recording.add(jsonSnapshot)
        rewindSeek = recording.size - 1
    }


    fun loadGameState(world: World, coroutineContext: CoroutineContext) {
        launchImmediately(context = coroutineContext) {
            if (worldSnapshot == null) {
                val vfs = resourcesVfs["save_game.json"]
                if (vfs.exists()) {
                    worldSnapshot = vfs.readString()
//                snapshotDeleted = false
                }
            }
            if (worldSnapshot != null) {
                val family: Family = world.family { all(ParallaxComponent) }

//            family.forEach {
//                println("Before loading: $it")
//                it[ParallaxComponent].backgroundLayers.forEach { bg ->
//                    println("bg: $bg")
//                }
//            }

                val snapshot: Map<Entity, Snapshot> = snapshotSerializer.json().decodeFromString(worldSnapshot!!)
                world.loadSnapshot(snapshot)
                println("snapshot loaded!")

//                family.forEach {
//                    println("After loading: $it")
//                    it[ParallaxComponent].backgroundLayers.forEach { bg ->
//                        println("bg: $bg")
//                    }
//                }

                // Do some post-processing
                // Update ParallaxComponents
                family.forEach { entity ->
                    val parallaxComponent = entity[ParallaxComponent]
                    parallaxComponent.updateLayerEntities(world)
                }
            } else println("snapshot not loaded!")
        }
    }

    fun saveGameState(world: World, coroutineContext: CoroutineContext) {
        worldSnapshot = snapshotSerializer.json(pretty = true).encodeToString(world.snapshot())
        launchImmediately(context = coroutineContext) {
            val vfs = resourcesVfs["save_game.json"]
            vfs.writeString(worldSnapshot!!)
            println("Snapshot saved!")
//            snapshotDeleted = false
        }
    }

    fun pause() {
        gameRunning = !gameRunning
        world.systems.forEach { system ->
            when (system) {
                // Sound system needs special handling, because it has to stop all sounds which are playing
                is SoundSystem -> system.soundEnabled = gameRunning
                else -> system.enabled = gameRunning
            }
        }
    }

    fun rewind(fast: Boolean = false) {
        if (!gameRunning) {
            if (fast) rewindSeek -= 4
            else rewindSeek--
            if (rewindSeek < 0) rewindSeek = 0
            val snapshot: Map<Entity, Snapshot> = snapshotSerializer.json().decodeFromString(recording[rewindSeek])
            world.loadSnapshot(snapshot)
        }
    }

    fun forward(fast: Boolean = false) {
        if (!gameRunning) {
            if (fast) rewindSeek += 4
            else rewindSeek++
            if (rewindSeek > recording.size - 1) rewindSeek = recording.size - 1
            val snapshot: Map<Entity, Snapshot> = snapshotSerializer.json().decodeFromString(recording[rewindSeek])
            world.loadSnapshot(snapshot)
        }
    }
}
