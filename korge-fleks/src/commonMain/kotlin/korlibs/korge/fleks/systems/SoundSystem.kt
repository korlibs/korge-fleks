package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.Sound

/**
 *
 * A system which moves entities. It either takes the rigidbody of an entity into account or if not
 * it moves the entity linear without caring about gravity.
 */
class SoundSystem(
    private val assets: AssetStore = World.inject()
) : IteratingSystem(
    family {
        all(Sound)
    },
    interval = EachFrame
) {
/*
    private val arraySize: Int = 64

    // Korge specific internal objects which we do not want to store in the components - they are accessed by entity id the same way as components
    private var soundChannels: Array<SoundChannel?> = Array(arraySize) { null }

    fun addOrUpdate(entity: Entity, channel: SoundChannel) {
        if (entity.id >= soundChannels.size) {
            soundChannels = soundChannels.copyOf(max(soundChannels.size * 2, entity.id + 1))
        }
        soundChannels[entity.id] = channel
    }

    operator fun get(entity: Entity) : SoundChannel {
        return if (soundChannels.size > entity.id) {
            soundChannels[entity.id] ?: error("SoundSystem: Channel of entity '${entity.id}' is null!")
        } else error("SoundSystem: Entity '${entity.id}' is out of range!")
    }

    fun getOrNull(entity: Entity) : SoundChannel? {
        return if (soundChannels.size > entity.id) soundChannels[entity.id]  // Cache potentially has this sound channel. However, return value can still be null!
        else null
    }
*/

    override fun onTickEntity(entity: Entity) {
        val sound = entity[Sound]
        val soundChannel = assets.getSound(sound.name)

        if (sound.stopTrigger) {
            soundChannel.pause()
            sound.stopTrigger = false
        }
        if (sound.startTrigger) {
            soundChannel.pause()
            soundChannel.reset()
            soundChannel.resume()
            sound.startTrigger = false
        }

        // continously save the play position
        sound.position = soundChannel.current.milliseconds

//        getOrNull(entity)?.let { channel ->
//            sound.position = channel.current.milliseconds
//        }

    }
}
