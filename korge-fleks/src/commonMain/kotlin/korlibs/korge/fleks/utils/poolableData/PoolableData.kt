package korlibs.korge.fleks.utils.poolableData


/**
 * All data classes (not deriving from Fleks Component<...>) which are used within components need to be serializable by
 * deriving from this interface.
 */
interface PoolableData<T> {
    fun clone(from: T)
    fun free()
}
