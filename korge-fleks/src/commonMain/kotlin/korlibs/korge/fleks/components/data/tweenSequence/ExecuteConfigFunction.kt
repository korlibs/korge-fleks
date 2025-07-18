package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This Tween is used to explicitly execute an entity config as a function.
 */
@Serializable @SerialName("ExecuteConfigFunction")
class ExecuteConfigFunction private constructor(
    var entityConfig: String = "",                // name of entity config which contains the function to configure the spawned entity

    override var target: Entity = Entity.NONE,    // [optional] entity can be provided if needed in the configure-function
    override var delay: Float? = null,            // not used
    override var duration: Float? = null,         // not used
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase, Poolable<ExecuteConfigFunction> {
    // Init an existing data instance with data from another one
    override fun init(from: ExecuteConfigFunction) {
        entityConfig = from.entityConfig

        target = from.target
        // delay not used
        // duration not used
        // easing not used
    }

    // Cleanup data instance manually
    // This is used for data instances when they are a value property of a component
    override fun cleanup() {
        entityConfig = ""

        target = Entity.NONE
        // delay not used
        // duration not used
        // easing not used
    }

    // Clone a new data instance from the pool
    override fun clone(): ExecuteConfigFunction = pool.alloc().apply { init(from = this@ExecuteConfigFunction) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticExecuteConfigFunction(config: ExecuteConfigFunction.() -> Unit): ExecuteConfigFunction =
            ExecuteConfigFunction().apply(config)

        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.executeConfigFunction(config: ExecuteConfigFunction.() -> Unit) { tweens.add(pool.alloc().apply(config)) }

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "ExecuteConfigFunction") { ExecuteConfigFunction() }
    }
}
