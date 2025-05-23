package korlibs.korge.fleks.components.data.tweenSequence


import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable @SerialName("TweenSound")
class TweenSound private constructor(
    var startTrigger: Boolean? = null,
    var stopTrigger: Boolean? = null,
    var position: Float? = null,
    var volume: Float? = null,

    override var target: Entity = Entity.NONE,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null
) : TweenBase {
    // Init an existing tween data instance with data from another tween
    fun init(from: TweenSound) {
        startTrigger = from.startTrigger
        stopTrigger = from.stopTrigger
        position = from.position
        volume = from.volume
        target = from.target
        delay = from.delay
        duration = from.duration
        easing = from.easing
        // Hint: it is not needed to copy "easing" property by creating new one like below:
        // easing = Easing.ALL[easing::class.toString().substringAfter('$')]
    }

    // Cleanup the tween data instance manually
    override fun free() {
        startTrigger = null
        stopTrigger = null
        position = null
        volume = null
        target = Entity.NONE
        delay = null
        duration = null
        easing = null

        pool.free(this)
    }

    companion object {
        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.tweenSound(config: TweenSound.() -> Unit) {
            tweens.add(pool.alloc().apply(config))
        }

        private val pool = Pool(preallocate = 0) { TweenSound() }
    }
}
