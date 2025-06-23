package korlibs.korge.fleks.components.data.tweenSequence


import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable @SerialName("ResetEvent")
class ResetEvent private constructor(
    var event: Int = 0,                           // Set a specific event so that a Wait for event can be unlocked

    override var target: Entity = Entity.NONE,    // not used
    override var delay: Float? = null,            // Not used
    override var duration: Float? = null,         // not used
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase {
    // Init an existing tween data instance with data from another tween
    fun init(from: ResetEvent) {
        event = from.event
        target = from.target
        delay = from.delay
        duration = from.duration
        easing = from.easing
        // Hint: it is not needed to copy "easing" property by creating new one like below:
        // easing = Easing.ALL[easing::class.toString().substringAfter('$')]
    }

    // Cleanup the tween data instance manually
    override fun free() {
        event = 0
        target = Entity.NONE
        delay = null
        duration = null
        easing = null

        pool.free(this)
    }

    companion object {
        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.resetEvent(config: ResetEvent.() -> Unit) {
            tweens.add(pool.alloc().apply(config))
        }

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "ResetEvent") { ResetEvent() }
    }
}
