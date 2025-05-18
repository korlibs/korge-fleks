package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.*
import korlibs.datastructure.*
import korlibs.korge.fleks.gameState.*


// TODO: remove
abstract class CloneableComponent<T> : Component<T> {
    abstract fun clone(): Component<T>
}

/**
 * All components needs to be derived from [Poolable] to be able to be reused from a Component pool and
 * to be recorded in the SnapshotSerializerSystem.
 *
 * The reset function is called when the component is going to be reused for a new entity.
 * The clone function needs to be implemented to perform a deep copy of all properties of the component for
 * the serialization of the game state.
 */
abstract class Poolable<T> : Component<T> {
    abstract fun reset()  // feature of poolable
    abstract fun World.clone(): Component<T>  // feature of making snapshots on the fly

    /**
     * This function needs to be called if the component is not used anymore and should be freed.
     * Normally, this is done in the onRemove function of the component. But SnapshotSerializerSystem will call
     * this function to free the component when cleanup old snapshots.
     */
    fun World.free() {
        runCatching {
            val pool = inject<Pool<T>>("PoolCmp${type().id}")
            @Suppress("UNCHECKED_CAST")
            pool.free(this@Poolable as T)
        }
    }

    /**
     * Function that is called when the component is removed from an entity.
     */
    override fun World.onRemove(entity: Entity) {
        // Do not free the component if the game is not running - i.e. during the snapshot rewind / forward feature
        val gameState = inject<GameStateManager>("GameStateManager")
        if (gameState.gameRunning) free()
    }
}

/**
 * Creates a new pool for the specified [Poolable].
 *
 * Hint: Integrate as companion object in component class like below and call that function in the injectable section
 * of the Fleks world configuration:
 *
 *     fun InjectableConfiguration.addMyComponentPool(preAllocate: Int = 0) {
 *         addPool(MyComponent, preAllocate) { MyComponent() }
 *     }
 */
fun <T : Poolable<T>> InjectableConfiguration.addPool(
    componentType: ComponentType<T>,
    preallocate: Int = 0,
    gen: (Int) -> T
) {
    val pool = Pool(reset = { it.reset() }, preallocate, gen)
    add("PoolCmp${componentType.id}", pool)
}

/**
 * Allocates a new component of type [T] from the specific injected pool.
 *
 * Hint: Integrate as companion object in component class like below:
 *     fun World.myComponent(config: MyComponent.() -> Unit ): MyComponent =
 *         getPoolable(MyComponent).apply { config() }
 *
 * Then it can be used like this when creating a new entity:
 *     entity.configure {
 *         it += myComponent {
 *             property = 42
 *         }
 *     }
 */
fun <T> World.getPoolable(componentType: ComponentType<T>): T {
    val pool = try {
        this.inject<Pool<T>>("PoolCmp${componentType.id}")
    } catch (e: FleksNoSuchInjectableException) {
        error("Attempting to allocate to pool 'PoolCmp${componentType.id}' without adding it to injectables! Ensure to call 'addPool' for your component type '${componentType::class.simpleName}'.")
    }
    return pool.alloc()
}

fun <T> World.getPool(componentType: ComponentType<T>): Pool<T> {
    return try {
        this.inject<Pool<T>>("PoolCmp${componentType.id}")
    } catch (e: FleksNoSuchInjectableException) {
        error("Pool 'PoolCmp${componentType.id}' for componentType '${componentType::class.simpleName}' not found.")
    }
}

/**
 * Structure containing a set of reusable objects.
 *
 * The method [alloc] retrieves from the pool or allocates a new object, while the [free] method
 * pushes back one element to the pool and resets it to reuse it.
 */
class Pool<T> internal constructor() {
    private var reset: (T) -> Unit = {}
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
    constructor(reset: (T) -> Unit = {}, preallocate: Int = 0, gen: (Int) -> T) : this() {
        setup(reset, preallocate, gen)
    }

    /**
     * Structure containing a set of reusable objects.
     *
     * @param preallocate the number of objects to preallocate
     * @param gen the object generate function to create a new object when needed
     */
    constructor(preallocate: Int = 0, gen: (Int) -> T) : this({}, preallocate, gen)

    /**
     * Setup structure containing a set of reusable objects.
     *
     * @param reset the function that reset an existing object to its initial state
     * @param preallocate the number of objects to preallocate
     * @param gen the object generate function to create a new object when needed
     */
    fun setup(reset: (T) -> Unit = {}, preallocate: Int, gen: (Int) -> T) {
        this.reset = reset
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
        reset(element)
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
