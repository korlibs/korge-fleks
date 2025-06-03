package korlibs.korge.fleks.utils


interface Poolable<T> {
    fun init(from: T)
    fun cleanup()
    fun free()
}
