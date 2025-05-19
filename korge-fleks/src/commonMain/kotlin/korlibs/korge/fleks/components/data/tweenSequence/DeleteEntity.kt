package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.TweenSequence.TweenBase
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable @SerialName("DeleteEntity")
class DeleteEntity private constructor(
    override var entity: Entity = Entity.NONE,
    override var delay: Float? = null,     // not used
    override var duration: Float? = null,  // not used
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase {
    // Init an existing tween data instance with data from another tween
    fun init(from: DeleteEntity) {
        entity = from.entity
        // Hint: it is not needed to copy "easing" property by creating new one like below:
        // easing = Easing.ALL[easing::class.toString().substringAfter('$')]
    }

    // Cleanup the tween data instance manually
    override fun free() {
        entity = Entity.NONE

        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of tween data as val inside a component (TODO: check if needed)
        fun staticDeleteEntity(config: DeleteEntity.() -> Unit ): DeleteEntity =
            DeleteEntity().apply(config)

        // Use this function to get a new instance of a tween from the pool and add it to a TweenSequence component
        fun DeleteEntity(config: DeleteEntity.() -> Unit ): DeleteEntity =
            pool.alloc().apply(config)

        private val pool = Pool(preallocate = 0) { DeleteEntity() }
    }
}
