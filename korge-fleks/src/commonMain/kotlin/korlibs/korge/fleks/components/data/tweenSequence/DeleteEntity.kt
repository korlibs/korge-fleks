package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This Tween is used to animate ...
 */
@Serializable @SerialName("DeleteEntity")
class DeleteEntity private constructor(
    override var target: Entity = Entity.NONE,
    override var delay: Float? = null,     // not used
    override var duration: Float? = null,  // not used
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase, Poolable<DeleteEntity> {
    // Init an existing data instance with data from another one
    override fun init(from: DeleteEntity) {
        target = from.target
        // delay not used
        // duration not used
        // easing not used
    }

    // Cleanup data instance manually
    // This is used for data instances when they are a value property of a component
    override fun cleanup() {
        target = Entity.NONE
        // delay not used
        // duration not used
        // easing not used
    }

    // Clone a new data instance from the pool
    override fun clone(): DeleteEntity = pool.alloc().apply { init(from = this@DeleteEntity ) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticDeleteEntity(config: DeleteEntity.() -> Unit ): DeleteEntity =
            DeleteEntity().apply(config)

        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.deleteEntity(config: DeleteEntity.() -> Unit ) { tweens.add(pool.alloc().apply(config)) }

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "DeleteEntity") { DeleteEntity() }
    }
}
