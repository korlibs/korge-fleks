package korlibs.korge.fleks.utils


/**
 * Interface for poolable objects. It is used to define objects that can be reused from a pool.
 * Those objects are used as properties of components.
 */
interface Poolable<T> {
    fun clone(): T
    fun init(from: T)
    fun cleanup()
    fun free()
}
