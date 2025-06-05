package korlibs.korge.fleks.utils


interface Poolable<T> {
    fun clone(): T
    fun init(from: T)
    fun cleanup()
    fun free()
}
