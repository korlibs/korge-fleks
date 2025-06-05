package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable @SerialName("DeleteEntity")
class DeleteEntity private constructor(
    override var target: Entity = Entity.NONE,
    override var delay: Float? = null,     // not used
    override var duration: Float? = null,  // not used
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase {
    // Init an existing tween data instance with data from another tween
    fun init(from: DeleteEntity) {
        target = from.target
        // Hint: it is not needed to copy "easing" property by creating new one like below:
        // easing = Easing.ALL[easing::class.toString().substringAfter('$')]
    }

    // Cleanup the tween data instance manually
    override fun free() {
        target = Entity.NONE

        pool.free(this)
    }

    companion object {
        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.deleteEntity(config: DeleteEntity.() -> Unit) {
            tweens.add(pool.alloc().apply(config))
        }

        private val pool = Pool(preallocate = 0) { DeleteEntity() }
    }
}
