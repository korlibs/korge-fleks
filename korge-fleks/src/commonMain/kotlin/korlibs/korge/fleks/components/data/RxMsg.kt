package korlibs.korge.fleks.components.data

import com.github.quillraven.fleks.Entity
import korlibs.korge.fleks.utils.AppConfig
import korlibs.korge.fleks.utils.Pool
import korlibs.korge.fleks.utils.Poolable
import korlibs.korge.fleks.utils.cleanup
import korlibs.korge.fleks.utils.init
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This class is used my the [RxMsgPassingConfig] component to save a list of entities which want to be informed when
 * a specific RxMsg type was sent.
 */
@Serializable @SerialName("RxMsg")
class RxMsg private constructor(
    val entities: MutableList<Entity> = mutableListOf(),
    var entityConfig: String? = null
) : Poolable<RxMsg> {
    // Init an existing data instance with data from another one
    override fun init(from: RxMsg) {
        entities.init(from.entities)
        entityConfig = from.entityConfig
    }

    // Cleanup data instance manually
    // This is used for data instances when they are a value property of a component
    override fun cleanup() {
        entities.cleanup()
        entityConfig = ""
    }

    // Clone a new data instance from the pool
    override fun clone(): RxMsg = rxMsg { init(from = this@RxMsg) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticRxMsg(config: RxMsg.() -> Unit): RxMsg =
            RxMsg().apply(config)

        // Use this function to get a new instance of the data object from the pool
        fun rxMsg(config: RxMsg.() -> Unit): RxMsg =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "RxMsg") { RxMsg() }
    }
}
