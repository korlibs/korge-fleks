package korlibs.korge.fleks.utils.componentPool

import com.github.quillraven.fleks.*


interface PoolType<T> {
    val poolName: String

    fun alloc(world: World): T {
        val pool = try {
            world.inject<Pool<T>>(poolName)
        } catch (e: FleksNoSuchInjectableException) {
            error("Attempting to allocate to pool '$poolName' without adding it to injectables! Ensure to call 'addPool' and specify ${this::class.simpleName} as the type.")
        }
        return pool.alloc()
    }
}
