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
    interval = Fixed(step = 0.5f)
) {

    private val snapshotSerializer = SnapshotSerializer().apply {
        register("module", module)
    }

    private val recording: MutableList<String> = mutableListOf()

    // TODO remove later
    private var worldSnapshot: String? = null

    companion object {
        fun SystemConfiguration.setup(module: SerializersModule): SnapshotSerializerSystem {
            val system = SnapshotSerializerSystem(module)
            add(system)
            return system
        }
    }

    override fun onTick() {
        val jsonSnapshot = snapshotSerializer.json().encodeToString(world.snapshot())
        recording.add(jsonSnapshot)


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

}
