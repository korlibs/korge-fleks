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

    private val recording: MutableList<Map<Entity, Snapshot>> = mutableListOf()
    private val snapshotRecording: MutableList<Map<Entity, Snapshot>> = mutableListOf()

    private var rewindSeek: Int = 0
    private var gameRunning: Boolean = true

    // TODO remove later
    private var worldSnapshot: String? = null
    private val family: Family = world.family { all(ParallaxComponent) }

    companion object {
        fun SystemConfiguration.setup(module: SerializersModule) = add(SnapshotSerializerSystem(module))
    }

    override fun onTick() {
        val jsonSnapshot = snapshotSerializer.json().encodeToString(world.snapshot())
        val snapshot: Map<Entity, Snapshot> = snapshotSerializer.json().decodeFromString(jsonSnapshot)
        recording.add(snapshot)
        rewindSeek = recording.size - 1

        // Do some post-processing
        // Update ParallaxComponents
        family.forEach { entity ->
            val parallaxComponent = entity[ParallaxComponent]
            parallaxComponent.updateLayerEntities(world)
        }

        // TODO make deep copy of world snapshot

        val worldSnapshot = world.snapshot()

        // Deep copy of world snapshot for storing it for game recording
        val snapshotCopy = mutableMapOf<Entity, Snapshot>()

        // Create deep copy of all components and tags of an entity
        worldSnapshot.forEach { (entity, value) ->
            val componentsCopy = mutableListOf<Component<*>>()
            val tagsCopy = mutableListOf<UniqueId<*>>()

            value.components.forEach { component ->
                when (component) {
                    is EntityLinkComponent -> componentsCopy.add(component.clone())
                    is InfoComponent -> componentsCopy.add(component.clone())
                    is InputTouchButtonComponent -> componentsCopy.add(component.clone())
                    is LayerComponent-> componentsCopy.add(component.clone())
                    is LayoutComponent -> componentsCopy.add(component.clone())
                    is LdtkLevelMapComponent -> componentsCopy.add(component.clone())
                    is LifeCycleComponent -> componentsCopy.add(component.clone())
                    is MotionComponent -> componentsCopy.add(component.clone())
                    is NoisyMoveComponent -> componentsCopy.add(component.clone())
                    is OffsetByFrameIndexComponent -> componentsCopy.add(component.clone())
                    is ParallaxComponent -> componentsCopy.add(component.clone(world))
                    is PositionComponent -> componentsCopy.add(component.clone())
                    is RgbaComponent -> componentsCopy.add(component.clone())
                    is RigidbodyComponent -> componentsCopy.add(component.clone())
                    is SizeComponent -> componentsCopy.add(component.clone())
                    is SoundComponent -> componentsCopy.add(component.clone())
                    is SpawnerComponent -> componentsCopy.add(component.clone())
                    is SpriteComponent -> componentsCopy.add(component.clone())
                    is SpriteLayersComponent -> componentsCopy.add(component.clone())
                    is SwitchLayerVisibilityComponent -> componentsCopy.add(component.clone())
                    is TextComponent -> componentsCopy.add(component.clone())
                    is TiledLevelMapComponent -> componentsCopy.add(component.clone())
// TODO
//                    is TweenPropertyComponent -> componentsCopy.add(component.clone())
//                    is TweenSequenceComponent -> componentsCopy.add(component.clone())
                    else -> {
// TODO add this here again
//                        println("WARNING: Component '$component' will not be serialized in SnapshotSerializerSystem!")
                    }
                }
            }

            value.tags.forEach { tag ->

                // TODO same for tags

            }

            // Create snapshot of entity as deep copy of all components and tags
            snapshotCopy[entity] = wildcardSnapshotOf(componentsCopy, tagsCopy)
        }
        snapshotRecording.add(snapshotCopy)
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
            world.loadSnapshot(recording[rewindSeek])
        }
    }

    fun forward(fast: Boolean = false) {
        if (!gameRunning) {
            if (fast) rewindSeek += 4
            else rewindSeek++
            if (rewindSeek > recording.size - 1) rewindSeek = recording.size - 1
            world.loadSnapshot(recording[rewindSeek])
        }
    }
}
