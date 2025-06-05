package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This is the sound config component for a sound entity.
 * Other entities like explosions can trigger to play a specific sound by having a reference to a sound entity.
 *
 * The system is set up in a way that every specific sound effect can only be played once a time.
 * Thus, if multiple other entities are triggering the sound effect than playing of the sound effect
 * will be stopped and started again. This creates the typical retro sound effects playback.
 *
 * TODO: Remove Sound component from entity when it has finished playing
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("Sound")
class Sound private constructor(
    var name: String = "",
    var startTrigger: Boolean = false,  // to play this sound effect set this to "true"
    var stopTrigger: Boolean = false,  // to stop playing this sound effect set this to "true"
    var position: Double = 0.0,  // playing position in milliseconds
    var volume: Double = 1.0,
    var isPlaying: Boolean = false,
    var loop: Boolean = false  // TODO not yet implemented
) : PoolableComponent<Sound>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: Sound) {
        name = from.name
        startTrigger = from.startTrigger
        stopTrigger = from.stopTrigger
        position = from.position
        volume = from.volume
        isPlaying = from.isPlaying
        loop = from.loop
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        name = ""
        startTrigger = false
        stopTrigger = false
        position = 0.0
        volume = 1.0
        isPlaying = false
        loop = false
    }

    override fun type() = SoundComponent

    companion object {
        val SoundComponent = componentTypeOf<Sound>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticSoundComponent(config: Sound.() -> Unit ): Sound =
        Sound().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun soundComponent(config: Sound.() -> Unit ): Sound =
        pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE) { Sound() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): Sound = soundComponent { init(from = this@Sound ) }

    // Initialize the component automatically when it is added to an entity
    override fun World.initComponent(entity: Entity) {
    }

    // Cleanup/Reset the component automatically when it is removed from an entity (component will be returned to the pool eventually)
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }

    // Initialize an external prefab when the component is added to an entity
    override fun World.initPrefabs(entity: Entity) {
    }

    // Cleanup/Reset an external prefab when the component is removed from an entity
    override fun World.cleanupPrefabs(entity: Entity) {
    }

    // Free the component and return it to the pool - this is called directly by the SnapshotSerializerSystem
    override fun free() {
        cleanup()
        pool.free(this)
    }
}
