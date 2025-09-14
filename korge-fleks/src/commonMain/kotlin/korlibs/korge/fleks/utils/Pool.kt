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

    val totalGeneratedItems
        get() = lastId

    val totalGeneratedItemsInUse
        get() = totalGeneratedItems - itemsInPool

    val itemsInPool: Int  // items not used
        get() = items.size

    // Used for health monitoring of pool usage
    var totalAllocatedItems: Int = 0
        private set

    var totalFreedItems: Int = 0
        private set

    val totalItemsInUse
        get() = totalAllocatedItems - totalFreedItems

    /**
     * Structure containing a set of reusable objects.
     *
     * @param preallocate the number of objects to preallocate
     * @param name the name of the pool for statistics output
     * @param gen the object generate function to create a new object when needed
     */
    constructor(preallocate: Int = 0, name: String, gen: (Int) -> T) : this() {
        listOfAllPools[name] = this
        setup( preallocate, gen)
    }

    companion object {
        val listOfAllPools = mutableMapOf<String, Pool<*>>()

        fun writeStatistics() {
            println("Pool statistics:")
            listOfAllPools.forEach { (name, pool) ->
                println("  $name: totalGeneratedItems=${pool.totalGeneratedItems}, itemsInPool=${pool.itemsInPool}, totalItemsInUse=${pool.totalItemsInUse}")
            }
        }

        fun doPoolUsageCheckAfterUnloading() {
            listOfAllPools.forEach { (name, pool) ->

                // Consistency check for total items
                if (pool.totalGeneratedItems != pool.itemsInPool) {
                    println("ERROR: Consistency check - pool '$name' has '${pool.totalGeneratedItems - pool.itemsInPool}' leaked objects! (Negative value means duplicate freed objects)")
                }
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
        // Monitor items created
        totalAllocatedItems++

        return if (items.isNotEmpty()) items.pop()
        else
            gen?.invoke(lastId++)
                ?: error("Pool<T> was not instantiated with a generator function!")
    }

    fun free(element: T) {
        // Monitor items freed
        totalFreedItems++

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
