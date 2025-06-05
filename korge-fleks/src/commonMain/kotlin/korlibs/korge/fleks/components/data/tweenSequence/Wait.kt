package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable @SerialName("Wait")
class Wait private constructor(
    var event: Int? = null,                           // Wait for a specific event if event is not "null" - need to unblock from infinite wait (Float.MAX_VALUE)

    override var target: Entity = Entity.NONE,        // not used
    override var delay: Float? = null,                // Not used
    override var duration: Float? = Float.MAX_VALUE,  // Use duration by setting explicitly a value
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase {
    // Init an existing tween data instance with data from another tween
    fun init(from: Wait) {
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
        event = null
        target = Entity.NONE
        delay = null
        duration = null
        easing = null

        pool.free(this)
    }

    companion object {
        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.wait(config: Wait.() -> Unit) {
            tweens.add(pool.alloc().apply(config))
        }

        private val pool = Pool(preallocate = 0) { Wait() }
    }
}
