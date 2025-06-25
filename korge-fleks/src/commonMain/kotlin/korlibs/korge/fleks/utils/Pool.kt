package korlibs.korge.fleks.utils

import korlibs.datastructure.*


/**
 * Structure containing a set of reusable objects.
 *
 * The method [alloc] retrieves from the pool or allocates a new object, while the [free] method
 * pushes back one element to the pool. Entities needs to be reset/cleanup before freeing manually.
 */
class Pool<T> internal constructor() {
    private var gen: ((Int) -> T)? = null
    private val items = Stack<T>()
    private var lastId = 0

    val totalAllocatedItems
        get() = lastId

    val totalItemsInUse
        get() = totalAllocatedItems - itemsInPool

    val itemsInPool: Int
        get() = items.size

    /**
     * Structure containing a set of reusable objects.
     *
     * @param reset the function that reset an existing object to its initial state
     * @param preallocate the number of objects to preallocate
     * @param gen the object generate function to create a new object when needed
     */
    constructor(preallocate: Int = 0, name: String, gen: (Int) -> T) : this() {
        pools[name] = this
        setup( preallocate, gen)
    }

    companion object {
        val pools = mutableMapOf<String, Pool<*>>()

        fun writeStatistics() {
            println("Pool statistics:")
            pools.forEach { (name, pool) ->
                println("  $name: totalAllocatedItems=${pool.totalAllocatedItems}, itemsInPool=${pool.itemsInPool}, totalItemsInUse=${pool.totalItemsInUse}")
            }
        }
    }

    /**
     * Setup structure containing a set of reusable objects.
     *
     * @param preallocate the number of objects to preallocate
     * @param gen the object generate function to create a new object when needed
     */
    private fun setup(preallocate: Int, gen: (Int) -> T) {
        this.gen = gen
        preAlloc(preallocate, gen)
    }

    private fun preAlloc(preallocate: Int, gen: (Int) -> T) {
        for (n in 0 until preallocate) items.push(gen(lastId++))
    }

    fun alloc(): T {
        return if (items.isNotEmpty()) items.pop()
        else
            gen?.invoke(lastId++)
                ?: error("Pool<T> was not instantiated with a generator function!")
    }

    fun free(element: T) {
        items.push(element)
    }

    fun free(vararg elements: T) {
        elements.forEach { free(it) }
    }

    fun free(elements: Iterable<T>) {
        for (element in elements) free(element)
    }

    fun free(elements: List<T>) {
        elements.forEach { free(it) }
    }

    inline operator fun <R> invoke(callback: (T) -> R): R = alloc(callback)

    inline fun <R> alloc(callback: (T) -> R): R {
        val temp = alloc()
        return callback(temp)
    }

    inline fun <R> allocMultiple(
        count: Int,
        temp: MutableList<T> = mutableListOf(),
        callback: (MutableList<T>) -> R,
    ): R {
        temp.clear()
        for (n in 0 until count) temp.add(alloc())
        return callback(temp)
    }

    inline fun <R> allocThis(callback: T.() -> R): R {
        val temp = alloc()
        return callback(temp)
    }

    override fun hashCode(): Int = items.hashCode()

    override fun equals(other: Any?): Boolean =
        (other is Pool<*>) && this.items == other.items && this.itemsInPool == other.itemsInPool
}
