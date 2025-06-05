package korlibs.korge.fleks.components.data.tweenSequence


import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable @SerialName("ExecuteConfigFunction")
class ExecuteConfigFunction private constructor(
    var entityConfig: String = "",                // name of entity config which contains the function to configure the spawned entity

    override var target: Entity = Entity.NONE,    // [optional] entity can be provided if needed in the configure-function
    override var delay: Float? = null,            // not used
    override var duration: Float? = null,         // not used
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase {
    // Init an existing tween data instance with data from another tween
    fun init(from: ExecuteConfigFunction) {
        entityConfig = from.entityConfig
        target = from.target
        delay = from.delay
        duration = from.duration
        easing = from.easing
        // Hint: it is not needed to copy "easing" property by creating new one like below:
        // easing = Easing.ALL[easing::class.toString().substringAfter('$')]
    }

    // Cleanup the tween data instance manually
    override fun free() {
        entityConfig = ""
        target = Entity.NONE
        delay = null
        duration = null
        easing = null

        pool.free(this)
    }

    companion object {
        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.executeConfigFunction(config: ExecuteConfigFunction.() -> Unit) {
            tweens.add(pool.alloc().apply(config))
        }

        private val pool = Pool(preallocate = 0) { ExecuteConfigFunction() }
    }
}
