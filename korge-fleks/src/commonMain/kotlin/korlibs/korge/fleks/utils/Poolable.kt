package korlibs.korge.fleks.utils


/**
 * Interface for poolable data classes.
 * It provides methods to clone, initialize, cleanup, and free the instance.
 * This is used for managing data object instances in a pool.
 */
interface Poolable<T> {
    fun clone(): T
    fun init(from: T)
    fun cleanup()
    fun free()
}
