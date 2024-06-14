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

    private val family: Family = world.family { all(ParallaxComponent) }
    private val snapshotSerializer = SnapshotSerializer().apply { register("module", module) }
    private val recording: MutableList<Map<Entity, Snapshot>> = mutableListOf()
    private var rewindSeek: Int = 0
    private var gameRunning: Boolean = true

    companion object {
        /**
         * Setup function for creating the SnapshotSerializerSystem. Here it is possible to
         * add [SerializersModule] as parameter. This enables to add components, tags and config data
         * classes outside Korge-fleks.
         *
         * TODO: Need to check how we can add "external" components into deep copy creation in onTick function
         */
        fun SystemConfiguration.setup(module: SerializersModule) = add(SnapshotSerializerSystem(module))
    }

    override fun onTick() {

        val worldSnapshot = world.snapshot()
        val snapshotCopy = mutableMapOf<Entity, Snapshot>()

        // Create deep copy of all components and tags of an entity
        worldSnapshot.forEach { (entity, value) ->
            val componentsCopy = mutableListOf<Component<*>>()
            val tagsCopy = mutableListOf<UniqueId<*>>()

            value.components.forEach { component ->
                // Hint: Cloning of components is done WITHOUT any world functions or features because world functions
                //       will NOT operate on the components which were copied into the snapshot, instead they will
                //       operate with components of entities in the current world! This is not what we want!
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
                    is ParallaxComponent -> componentsCopy.add(component.clone())
                    is PositionComponent -> componentsCopy.add(component.clone())
                    is RgbaComponent -> componentsCopy.add(component.clone())
                    is RigidbodyComponent -> componentsCopy.add(component.clone())
                    is SizeComponent -> componentsCopy.add(component.clone())
                    is SoundComponent -> componentsCopy.add(component.clone())
                    is SpawnerComponent -> componentsCopy.add(component.clone())
                    is SpriteComponent -> componentsCopy.add(component.clone())
                    is SpriteLayersComponent -> componentsCopy.add(component.clone())
                    is SwitchLayerVisibilityComponent -> componentsCopy.add(component.clone())
                    is TextFieldComponent -> componentsCopy.add(component.clone())
                    is TiledLevelMapComponent -> componentsCopy.add(component.clone())
                    is TweenPropertyComponent -> componentsCopy.add(component.clone())
                    is TweenSequenceComponent -> componentsCopy.add(component.clone())
                    else -> {
                        println("WARNING: Component '$component' will not be serialized in SnapshotSerializerSystem!")
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

        recording.add(snapshotCopy)
        rewindSeek = recording.size - 1
    }


    fun loadGameState(world: World, coroutineContext: CoroutineContext) {
        launchImmediately(context = coroutineContext) {
            val vfs = resourcesVfs["save_game.json"]
            if (vfs.exists()) {
                val worldSnapshot = vfs.readString()
                val family: Family = world.family { all(ParallaxComponent) }

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
                is SoundSystem -> system.soundEnabled = gameRunning
                else -> system.enabled = gameRunning
            }
        }
        // When game is resuming than delete all recordings which are beyond the new play position
        if (gameRunning) {
            while (rewindSeek < recording.size) { recording.removeLast() }
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
        }
    }

    fun forward(fast: Boolean = false) {
        if (gameRunning) triggerPause()
        if (!gameRunning) {
            if (fast) rewindSeek += 4
            else rewindSeek++
            if (rewindSeek > recording.size - 1) rewindSeek = recording.size - 1
            world.loadSnapshot(recording[rewindSeek])
        }
    }

    private fun postProcessing() {
        // Do some post-processing
        family.forEach { entity ->
            // Update ParallaxComponents
            val parallaxComponent = entity[ParallaxComponent]
            parallaxComponent.run { world.updateLayerEntities() }
        }

    }
}
