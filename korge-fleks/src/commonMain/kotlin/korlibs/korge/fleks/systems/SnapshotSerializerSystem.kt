package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import korlibs.io.async.*
import korlibs.io.file.std.*
import korlibs.korge.fleks.components.LayeredSprite.Companion.LayeredSpriteComponent
import korlibs.korge.fleks.components.Parallax.Companion.ParallaxComponent
import korlibs.korge.fleks.gameState.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*
import kotlinx.serialization.modules.*
import kotlin.coroutines.*


/**
 * This system operates on a world snapshot of Fleks and stores it in an array for (fast) rewind and forward.
 *
 * Hint: The world snapshot recording will only work on components which are derived from [CloneableComponent].
 * Please make sure you are not deriving any additional components from [Component].
 */
class SnapshotSerializerSystem(
    module: SerializersModule,
    val timesPerSecond: Int = 30,
    private val snapshotBufferInSeconds: Int = 30  // How many seconds of recordings should be kept in memory
) : IntervalSystem(
    interval = Fixed(step = 1f / timesPerSecond.toFloat())
) {

    // entity families needed for post-processing
    private val familyLayeredSprite: Family = world.family { all(LayeredSpriteComponent) }

    private val snapshotSerializer = SnapshotSerializer().apply { register("module", module) }
    private val recordings: MutableList<Recording> = mutableListOf()
    private var recNumber: Int = 0  // Overall number of recordings - incremented by 1 for each recording

    private var rewindSeek: Int = 0
    private var snapshotLoaded: Boolean = false

    private val gameState = world.inject<GameStateManager>("GameStateManager")

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
                if (component !is PoolableComponent<*>) { error("Component '$component' must be derive from PoolableComponent<T>!") }
                componentsCopy.add((component as PoolableComponent<*>).clone())
            }
            value.tags.forEach { tag -> tagsCopy.add(tag) }

            // Create snapshot of entity as deep copy of all components and tags
            snapshotCopy[entity] = wildcardSnapshotOf(componentsCopy, tagsCopy)
        }

// Fallback: If needed deep copy of snapshot can be done via serialization and deserialization
//        val jsonSnapshot = snapshotSerializer.json().encodeToString(world.snapshot())
//        val snapshotCopy: Map<Entity, Snapshot> = snapshotSerializer.json().decodeFromString(jsonSnapshot)

        // Cleanup old snapshots so that we do not use too much of memory resources
        recordingCleanup { pos -> freeComponents(recordings.removeAt(pos)) }

        // Store copy of word snapshot
        recordings.add(Recording(recNumber, snapshotCopy))
        recNumber++
        rewindSeek = recordings.size - 1
    }

    fun loadGameState(world: World, coroutineContext: CoroutineContext) {
        launchImmediately(context = coroutineContext) {
            val vfs = resourcesVfs["save_game.json"]
            if (vfs.exists()) {
                val worldSnapshot = vfs.readString()
                val snapshot: Map<Entity, Snapshot> = snapshotSerializer.json().decodeFromString(worldSnapshot)
                world.loadSnapshot(snapshot)
                println("snapshot loaded!")
                // Because we have deserialized a snapshot, we need to run post-processing on all components
                // which need to be partly initialized again.
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
            // Do not free the last snapshot, because it is loaded in the current world
            while (rewindSeek < recordings.size - 1) {
                freeComponents(recordings.removeLast())
            }
            // Do final post-processing if a snapshot was loaded (by rewind or forward)
            if (snapshotLoaded) {

                // TODO: Check - We do not need to run postProcessing() here, because the snapshot which was loaded
                // was not deserialized from JSON, but loaded from the recordings list
                //postProcessing()
                snapshotLoaded = false
                // remove current snapshot from the list of recordings - otherwise it would be freed later, but it is still in used
                freeComponents(recordings.removeLast())
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
     * Remove all recordings and free all components from all snapshots.
     * This will reset the recording system to its initial state.
     */
    fun removeAll() {
        // Free all components from all recordings
        recordings.forEach { recording -> freeComponents(recording) }
        recordings.clear()
        recNumber = 0
        rewindSeek = 0
        snapshotLoaded = false
    }

    private fun freeComponents(recording: Recording) {
        recording.snapshot.forEach { (_, snapshot) ->
            // Free all components from snapshot
            snapshot.components.forEach { component ->
                (component as PoolableComponent<*>).free()
            }
        }
    }

    /**
     * Post-processing of the world snapshot after loading it.
     */
    private fun postProcessing() {
        // Do some post-processing
        familyLayeredSprite.forEach { entity ->
            val layeredSpriteComponent = entity[LayeredSpriteComponent]
            layeredSpriteComponent.run { world.updateLayerEntities() }
        }
    }

    /**
     * This function will trigger [call] with the index of the recording which should be deleted.
     * The cleanup strategy is to keep only one snapshot per second after a certain time.
     */
    private fun recordingCleanup(call: (Int) -> Unit) {
        // After XX seconds start to clean up recordings
        val startTimeForCleanup: Int = snapshotBufferInSeconds * timesPerSecond

        val numberOfRecordings = recordings.size
        if (numberOfRecordings > startTimeForCleanup) {  // Keep very first recording
            val recIndex = numberOfRecordings - startTimeForCleanup
            val rec = recordings[recIndex]
            if (rec.recNumber % timesPerSecond != 0) {  // Do not delete first recording of each second
                call(recIndex)
            }
        }
    }

    // Below is currently not used

    /*
     * +------- Time modulo window (14 seconds)
     * |    +-- Indices of recordings in the modulo window (32 recordings)
     * v    v
     *     00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31
     * 0                                                                                                 0
     * 1                                                    1                                           xx
     * 2                            2                      xx                       2                   xx
     * 3                3          xx           3          xx           3          xx           3       xx
     * 4          4    xx     4    xx     4    xx     4    xx     4    xx     4    xx     4    xx     4 xx
     * 5       5 xx    xx    xx    xx    xx    xx    xx    xx    xx    xx    xx    xx    xx    xx    xx xx
     * 6      xx xx    xx    xx    xx    xx    xx    xx    xx  6 xx    xx    xx    xx    xx    xx    xx xx
     * 7      xx xx    xx    xx    xx  7 xx    xx    xx    xx xx xx    xx    xx    xx  7 xx    xx    xx xx
     * 8      xx xx    xx  8 xx    xx xx xx    xx  8 xx    xx xx xx    xx  8 xx    xx xx xx    xx  8 xx xx
     * 9      xx xx  9 xx xx xx    xx xx xx    xx xx xx    xx xx xx    xx xx xx    xx xx xx    xx xx xx xx
     * 10     xx xx xx xx xx xx    xx xx xx    xx xx xx    xx xx xx 10 xx xx xx    xx xx xx    xx xx xx xx
     * 11     xx xx xx xx xx xx    xx xx xx 11 xx xx xx    xx xx xx xx xx xx xx    xx xx xx 11 xx xx xx xx
     * 12     xx xx xx xx xx xx 12 xx xx xx xx xx xx xx    xx xx xx xx xx xx xx 12 xx xx xx xx xx xx xx xx
     * 13 [ ] xx xx xx xx xx xx xx xx xx xx xx xx xx xx 13 xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx xx
     *
     * recNumber modulo(32) == 0 -> those recordings will be kept
     */
    private val deleteTimes: Array<Set<Int>> = Array(14) { pos ->
        when (pos) {
            0 -> setOf(31)
            1 -> setOf(16)
            2 -> setOf(8, 24)
            3 -> setOf(4, 12, 20, 28)
            4 -> setOf(2, 6, 10, 14, 18, 22, 26, 30)
            5 -> setOf(1)
            6 -> setOf(17)
            7 -> setOf(9, 25)
            8 -> setOf(5, 13, 21, 29)
            9 -> setOf(3)
            10 -> setOf(19)
            11 -> setOf(11, 27)
            12 -> setOf(7, 23)
            13 -> setOf(15)
            else -> setOf()  // This should never happen
        }
    }

    /*
     * Another way to delete recordings
     */
    private val deleteTimes2: Array<Int> = Array(32) { pos ->
        when (pos) {
            0  -> 31
            1  -> 16
            2  -> 8
            3  -> 24
            4  -> 4
            5  -> 12
            6  -> 20
            7  -> 28
            8  -> 2
            9  -> 6
            10 -> 10
            11 -> 14
            12 -> 18
            13 -> 22
            14 -> 26
            15 -> 30
            16 -> 1
            17 -> 17
            18 -> 9
            19 -> 25
            20 -> 5
            21 -> 13
            22 -> 21
            23 -> 29
            24 -> 3
            25 -> 19
            26 -> 11
            27 -> 27
            28 -> 7
            29 -> 23
            30 -> 15
            31 -> 0  // Keep this recording
            else -> 0  // This should never happen
        }
    }
}
