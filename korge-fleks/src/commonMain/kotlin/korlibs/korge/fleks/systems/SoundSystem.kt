package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.audio.sound.paused
import korlibs.korge.assetmanager.AssetStore
import korlibs.korge.fleks.components.SoundComponent
import korlibs.time.*

/**
 *
 * A system which moves entities. It either takes the rigidbody of an entity into account or if not
 * it moves the entity linear without caring about gravity.
 */
class SoundSystem : IteratingSystem(
    family {
        all(SoundComponent)
    },
    interval = EachFrame
) {

    var soundEnabled: Boolean = true
    private var soundEnabledNext: Boolean = false
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
        val soundComponent = entity[SoundComponent]


        val soundChannel = AssetStore.getSound(soundComponent.name)

        // Sound enabling/disabling triggered from outside
        if (soundEnabled == soundEnabledNext) {
            soundEnabledNext = !soundEnabled
            if (soundEnabled) {
//                if (soundComponent.isPlaying && soundChannel.paused) soundChannel.resume()
                if (soundComponent.isPlaying) {
                    soundChannel.current = 10000.milliseconds
                    soundChannel.resume()
                    println("SoundSystem: Resume sound '${soundComponent.name}'")
                }
            } else {
//                if(!soundChannel.paused) soundChannel.pause()
                soundChannel.pause()
                println("SoundSystem: Pause sound '${soundComponent.name}' at position '${soundComponent.position}'")
            }
        } else {

            if (soundComponent.stopTrigger) {
                if (!soundChannel.paused) soundChannel.pause()
                soundComponent.stopTrigger = false
                soundComponent.isPlaying = false
            }
            if (soundComponent.startTrigger) {
//                if (soundChannel.paused) soundChannel.pause()
                soundChannel.reset()
                soundChannel.resume()
                println("SoundSystem: Start sound '${soundComponent.name}'")

                soundComponent.startTrigger = false
                soundComponent.isPlaying = true
            }
        }

        // continuously save the play position
        soundComponent.position = soundChannel.current.milliseconds

//        getOrNull(entity)?.let { channel ->
//            sound.position = channel.current.milliseconds
//        }

    }
}
