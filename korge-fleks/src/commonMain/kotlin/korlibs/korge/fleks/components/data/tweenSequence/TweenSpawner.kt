package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable @SerialName("TweenSpawner")
class TweenSpawner private constructor(
    var numberOfObjects: Int? = null,
    var interval: Int? = null,
    var timeVariation: Int? = null,
    var positionVariation: Float? = null,

    override var target: Entity = Entity.NONE,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null
) : TweenBase {
    // Init an existing tween data instance with data from another tween
    fun init(from: TweenSpawner) {
        numberOfObjects = from.numberOfObjects
        interval = from.interval
        timeVariation = from.timeVariation
        positionVariation = from.positionVariation
        target = from.target
        delay = from.delay
        duration = from.duration
        easing = from.easing
        // Hint: it is not needed to copy "easing" property by creating new one like below:
        // easing = Easing.ALL[easing::class.toString().substringAfter('$')]
    }

    // Cleanup the tween data instance manually
    override fun free() {
        numberOfObjects = null
        interval = null
        timeVariation = null
        positionVariation = null
        target = Entity.NONE
        delay = null
        duration = null
        easing = null

        pool.free(this)
    }

    companion object {
        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.tweenSpawner(config: TweenSpawner.() -> Unit) {
            tweens.add(pool.alloc().apply(config))
        }

        private val pool = Pool(preallocate = 0) { TweenSpawner() }
    }
}
