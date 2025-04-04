package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import korlibs.io.async.*
import korlibs.io.file.std.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.fleks.utils.componentPool.*
import kotlinx.serialization.*
import kotlinx.serialization.modules.*
import kotlin.coroutines.*

const val snapshotFps = 30

/**
 * This system operates on a world snapshot of Fleks and stores it in an array for (fast) rewind and forward.
 *
 * Hint: The world snapshot recording will only work on components which are derived from [CloneableComponent].
 * Please make sure you are not deriving any additional components from [Component].
 */
class SnapshotSerializerSystem(module: SerializersModule) : IntervalSystem(
    interval = Fixed(step = 1f / snapshotFps.toFloat())
) {

    // entity families needed for post-processing
    private val familyParallax: Family = world.family { all(ParallaxComponent) }
    private val familyLayeredSprite: Family = world.family { all(LayeredSpriteComponent) }

    private val snapshotSerializer = SnapshotSerializer().apply { register("module", module) }
    private val recording: MutableList<Map<Entity, Snapshot>> = mutableListOf()
    private var rewindSeek: Int = 0

    var gameRunning: Boolean = true

    // After 30 seconds keep only one snapshot per second
    private var numberSnapshotsToKeep: Int = 3 * snapshotFps
    private var snapshotSecondCounter: Int = 0
    private var snapshotDeletePointer: Int = 0

    override fun onTick() {
        val snapshotCopy = mutableMapOf<Entity, Snapshot>()

        // Create deep copy of all components and tags of an entity
        world.snapshot().forEach { (entity, value) ->
            val componentsCopy = mutableListOf<Component<*>>()
            val tagsCopy = mutableListOf<UniqueId<*>>()

            value.components.forEach { component ->
                // Hint: Cloning of components is done WITHOUT any world functions or features involved because world functions
                //       will NOT operate on the components which were copied into the snapshot, instead they will
                //       operate with components of entities in the current world! This is not what we want!
                when (component) {
                    is CloneableComponent<*> -> componentsCopy.add(component.clone())
                    is PoolableComponent<*> -> componentsCopy.add(component.run { world.clone() })
                    else -> {
                        println("WARNING: Component '$component' will not be serialized in SnapshotSerializerSystem! The component needs to derive from CloneableComponent<T>!")
                    }
                }
            }
            value.tags.forEach { tag -> tagsCopy.add(tag) }

            // Create snapshot of entity as deep copy of all components and tags
            snapshotCopy[entity] = wildcardSnapshotOf(componentsCopy, tagsCopy)
        }

// Fallback: If needed deep copy of snapshot can be done via serialization and deserialization
//        val jsonSnapshot = snapshotSerializer.json().encodeToString(world.snapshot())
//        val snapshotCopy: Map<Entity, Snapshot> = snapshotSerializer.json().decodeFromString(jsonSnapshot)

        // Cleanup old snapshots so that we do not save too much
        if (recording.size > numberSnapshotsToKeep) {
            if (snapshotSecondCounter < snapshotFps) {
                recording.removeAt(snapshotDeletePointer)
                snapshotSecondCounter++
            }
            else {
                snapshotSecondCounter = 0
                numberSnapshotsToKeep++
                snapshotDeletePointer++
            }
        }

        // Store copy of word snapshot
        recording.add(snapshotCopy)
        rewindSeek = recording.size - 1
    }

    fun loadGameState(world: World, coroutineContext: CoroutineContext) {
        launchImmediately(context = coroutineContext) {
            val vfs = resourcesVfs["save_game.json"]
            if (vfs.exists()) {
                val worldSnapshot = vfs.readString()
                val snapshot: Map<Entity, Snapshot> = snapshotSerializer.json().decodeFromString(worldSnapshot)
                world.loadSnapshot(snapshot)
                println("snapshot loaded!")

                postProcessing()

            } else println("WARNING: Cannot find snapshot file. Snapshot was not loaded!")
        }
    }

    fun saveGameState(world: World, coroutineContext: CoroutineContext) {
        val worldSnapshot = snapshotSerializer.json(pretty = true).encodeToString(world.snapshot())
        launchImmediately(context = coroutineContext) {
            val vfs = resourcesVfs["save_game.json"]
            vfs.writeString(worldSnapshot)
            println("Snapshot saved!")
        }
    }

    fun triggerPause() {
        gameRunning = !gameRunning
        world.systems.forEach { system ->
            when (system) {
                // Sound system needs special handling, because it has to stop all sounds which are playing
//                is SoundSystem -> system.soundEnabled = true  // TODO keep sound playing for now -- gameRunning
                is SoundSystem -> system.soundEnabled = gameRunning
                else -> system.enabled = gameRunning
            }
        }
        // When game is resuming than delete all recordings which are beyond the new play position
        if (gameRunning) {
            // We need to free the used components in old snapshots which are not needed anymore
            // That has to be done by the fleks world removing entities
            while (rewindSeek < recording.size) {
                recording.removeLast().let { snapshot ->
                    world.loadSnapshot(snapshot)
                    // Do not clean up the last snapshots - it will stay loaded in the fleks world
                    if (rewindSeek != recording.size) world.removeAll(clearRecycled = true)
                    // gameRunning is true here - components are freed from world
                }
            }
            // Do final post-processing
            postProcessing()
        }
    }

    fun rewind(fast: Boolean = false) {
        if (gameRunning) triggerPause()
        if (!gameRunning) {
            if (fast) rewindSeek -= 4
            else rewindSeek--
            if (rewindSeek < 0) rewindSeek = 0
            world.loadSnapshot(recording[rewindSeek])
            // Hint: gameRunning is false, so that no components are freed from previous world then new world snapshot is loaded
        }
    }

    fun forward(fast: Boolean = false) {
        if (gameRunning) triggerPause()
        if (!gameRunning) {
            if (fast) rewindSeek += 4
            else rewindSeek++
            if (rewindSeek > recording.size - 1) rewindSeek = recording.size - 1
            world.loadSnapshot(recording[rewindSeek])
            // Hint: gameRunning is false, so that no components are freed from previous world then new world snapshot is loaded
        }
    }

    /**
     *
     */
    private fun postProcessing() {
        // Do some post-processing
        familyParallax.forEach { entity ->
            // Update ParallaxComponents
            val parallaxComponent = entity[ParallaxComponent]
            parallaxComponent.run { world.updateLayerEntities() }
        }
        familyLayeredSprite.forEach { entity ->
            val layeredSpriteComponent = entity[LayeredSpriteComponent]
            layeredSpriteComponent.run { world.updateLayerEntities() }
        }

        // TODO Add possibility to invoke post processing for externally added components
    }
}
