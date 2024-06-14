package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
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
 */
@Serializable
@SerialName("Sound")
data class SoundComponent(
    var name: String = "",
    var startTrigger: Boolean = false,  // to play this sound effect set this to "true"
    var stopTrigger: Boolean = false,  // to stop playing this sound effect set this to "true"
    var position: Double = 0.0,  // playing position in milliseconds
    var volume: Double = 1.0,
    var isPlaying: Boolean = false,
    var loop: Boolean = false  // TODO not yet implemented
) : Component<SoundComponent> {
    override fun type(): ComponentType<SoundComponent> = SoundComponent
    companion object : ComponentType<SoundComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    fun clone() : SoundComponent = this.copy()
}
