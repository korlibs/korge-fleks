package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import korlibs.io.async.*
import korlibs.io.file.std.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.components.Collision.Companion.CollisionComponent
import korlibs.korge.fleks.components.LevelMap.Companion.LevelMapComponent
import korlibs.korge.fleks.gameState.*
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
    private val recordings: MutableList<Recording> = mutableListOf()
    private var recNumber: Int = 0  // Overall number of recordings - incremented by 1 for each recording
    private var rewindSeek: Int = 0
    private var snapshotLoaded: Boolean = false

    private val gameState = world.inject<GameStateManager>("GameStateManager")

    private val snapshotWorld = configureWorld {
        injectables {
            add("GameStateManager", world.inject("GameStateManager"))
            // Add all Component pools from gameWorld to the snapshot world
            add("PoolCmp${CollisionComponent.id}", world.getPool(CollisionComponent))
            add("PoolCmp${LevelMapComponent.id}", world.getPool(LevelMapComponent))
        }
    }

    data class Recording(
        val recNumber: Int,
        val snapshot: Map<Entity, Snapshot>
    )

    override fun onTick() {
        val snapshotCopy = mutableMapOf<Entity, Snapshot>()

        // Create deep copy of all components and tags of an entity
        world.snapshot().forEach { (entity, value) ->
            val componentsCopy = mutableListOf<Component<*>>()
            val tagsCopy = mutableListOf<UniqueId<*>>()

            value.components.forEach { component ->
                // Hint: Cloning of components is done WITHOUT any world functions or features involved like onAdd
                //       function of components. Otherwise, world functions would be called on the current original
                //       world and not on the snapshot world! This is not what we want!
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

        // Cleanup old snapshots so that we do not use too much of memory resources
        recordingCleanup(recNumber) { pos ->
            recordings.removeAt(pos).let { rec ->
                // gameRunning is "true" in order to free components in the world below
                snapshotWorld.loadSnapshot(rec.snapshot)
                snapshotWorld.removeAll(clearRecycled = true)
            }
        }

        // Store copy of word snapshot
        val rec = Recording(recNumber++, snapshotCopy)
        recordings.add(rec)
        rewindSeek = recordings.size - 1
    }

    fun loadGameState(world: World, coroutineContext: CoroutineContext) {
        launchImmediately(context = coroutineContext) {
            val vfs = resourcesVfs["save_game.json"]
            if (vfs.exists()) {
                val worldSnapshot = vfs.readString()
                val snapshot: Map<Entity, Snapshot> = snapshotSerializer.json().decodeFromString(worldSnapshot)
                world.loadSnapshot(snapshot)
                snapshotLoaded = true
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
        gameState.gameRunning = !gameState.gameRunning
        world.systems.forEach { system ->
            when (system) {
                // Sound system needs special handling, because it has to stop all sounds which are playing
//                is SoundSystem -> system.soundEnabled = true  // TODO keep sound playing for now -- gameRunning
                is SoundSystem -> system.soundEnabled = gameState.gameRunning
                else -> system.enabled = gameState.gameRunning
            }
        }
        // When game is resuming than delete all recordings which are beyond the new play position
        if (gameState.gameRunning) {
            // We need to free the used components in old snapshots which are not needed anymore
            // That will be done by a separate fleks world (snapshotWorld)
            // Do not free the last snapshot, because it is loaded in the current world
            while (rewindSeek < recordings.size - 1) {
                recordings.removeLast().let { rec ->
                    // gameRunning is "true" in order to free components in the world below
                    snapshotWorld.loadSnapshot(rec.snapshot)
                    snapshotWorld.removeAll(clearRecycled = true)
                }
            }
            // Do final post-processing if a snapshot was loaded (either by rewind/forward or loadGameState)
            if (snapshotLoaded) {
                postProcessing()
                snapshotLoaded = false
            }
            // Set recording number to the next recording
            recNumber = recordings.last().recNumber + 1
        }
    }

    fun rewind(fast: Boolean = false) {
        if (gameState.gameRunning) triggerPause()
        if (!gameState.gameRunning) {
            if (fast) rewindSeek -= 4
            else rewindSeek--
            if (rewindSeek < 0) rewindSeek = 0
            world.loadSnapshot(recordings[rewindSeek].snapshot)
            snapshotLoaded = true
            // Hint: gameRunning is false, so that no components are freed from previous world then new world snapshot is loaded
        }
    }

    fun forward(fast: Boolean = false) {
        if (gameState.gameRunning) triggerPause()
        if (!gameState.gameRunning) {
            if (fast) rewindSeek += 4
            else rewindSeek++
            if (rewindSeek > recordings.size - 1) rewindSeek = recordings.size - 1
            world.loadSnapshot(recordings[rewindSeek].snapshot)
            snapshotLoaded = true
            // Hint: gameRunning is false, so that no components are freed from previous world then new world snapshot is loaded
        }
    }

    /**
     * Post-processing of the world snapshot after loading it.
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

    /**
     * This function will trigger [call] with the index of the recording which should be deleted.
     * The cleanup strategy is to keep only one snapshot per second after a certain time.
     */
    private fun recordingCleanup(currentRecNumber: Int, call: (Int) -> Unit) {
        // After xx seconds keep only one snapshot per second
        val startTimeForCleanup: Int = 3 * snapshotFps  // 30 seconds

        val numberOfRecordings = recordings.size
        if (numberOfRecordings > startTimeForCleanup) {
            val recIndex = numberOfRecordings - startTimeForCleanup
            val rec = recordings[recIndex]
            if (rec.recNumber % 32 != 0) {
//                println("Cleanup rec number: ${rec.recNumber} - index '$recIndex' from overall recordings: $numberOfRecordings")
                call.invoke(recIndex)
            }
        }
    }
}
