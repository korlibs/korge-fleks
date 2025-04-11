package korlibs.korge.fleks.utils.poolableData

import kotlinx.serialization.Serializable
import kotlin.native.concurrent.*


/**
 * All data classes (not deriving from Fleks Component<...>) which are used within components need to be serializable by
 * deriving from this interface.
 */
@Serializable
abstract class PoolableData<T> {
    abstract fun clone(from: T)
    abstract fun free()

    @ThreadLocal
    companion object {
        private var nextId = 0
    }

    val id: Int = nextId++
}
