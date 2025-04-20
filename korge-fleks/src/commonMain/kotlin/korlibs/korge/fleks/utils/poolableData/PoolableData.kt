package korlibs.korge.fleks.utils.poolableData

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.componentPool.*
import kotlinx.serialization.Serializable
import kotlin.native.concurrent.*


/**
 * All data classes (not deriving from Fleks Component<...>) which are used within components need to be serializable by
 * deriving from this interface.
 */
/*
@Serializable
abstract class PoolableData<T> : Component<T> {

//    abstract fun reset()
//    abstract fun init(from: T)
//    abstract fun clone(): T

    fun World.alloc(): T {
        val pool = inject<Pool<T>>("DataPool$id")
        return pool.alloc()
    }
    fun World.free() {
        runCatching {
            val pool = inject<Pool<T>>("DataPool$id")
            @Suppress("UNCHECKED_CAST")
            pool.free(this@PoolableData as T)
            println("Freeing data '${this@PoolableData::class.simpleName}'")
        }
    }

    companion object :

}

fun <T : PoolableData<T>> InjectableConfiguration.addPool(
    id: Int,
    preallocate: Int = 0,
    gen: (Int) -> T
) {
    val pool = Pool(reset = { it.reset() }, preallocate, gen)
    add("DataPool$id", pool)
}
*/
