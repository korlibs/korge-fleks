package korlibs.korge.fleks.components.data.tweenSequence


import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable @SerialName("Jump")
class Jump private constructor(
    var distance: Int = 0,                        // Jump relative distance from current index (minus means jump back)
    var event: Int? = null,                       // Check for a specific event - if event is 0 than jump otherwise do not jump and tween execution continues with next tween in the list

    override var target: Entity = Entity.NONE,    // not used
    override var delay: Float? = null,            // not used
    override var duration: Float? = null,         // not used
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase {
    // Init an existing tween data instance with data from another tween
    fun init(from: Jump) {
        distance = from.distance
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
        distance = 0
        event = null
        target = Entity.NONE
        delay = null
        duration = null
        easing = null

        pool.free(this)
    }

    companion object {
        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.jump(config: Jump.() -> Unit) {
            tweens.add(pool.alloc().apply(config))
        }

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "Jump") { Jump() }
    }
}
