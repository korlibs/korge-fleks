package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.*
import korlibs.datastructure.*
import korlibs.korge.fleks.gameState.*


/**
 * All components needs to be derived from [Poolable] to be able to be reused from a Component pool and
 * to be recorded in the SnapshotSerializerSystem.
 *
 * The reset function is called when the component is going to be reused for a new entity.
 * The clone function needs to be implemented to perform a deep copy of all properties of the component for
 * the serialization of the game state.
 *
 * Note:
 *   - The initComponent and cleanupComponent functions are called as normal life-cycle functions of the component.
 *     They are not called when the snapshot rewind/forward feature is used because the components are already
 *     initialized when loaded (ie. deserialized) from a snapshot.
 *   - The initPrefabs and cleanupPrefabs functions are called always when the component is added to or removed from a world.
 *     Also during the snapshot rewind/forward feature.
 */
abstract class Poolable<T> : Component<T> {
    abstract fun World.clone(): Poolable<T>  // feature of making snapshots on the fly

    open fun World.initComponent(entity: Entity) = Unit
    open fun World.cleanupComponent(entity: Entity) = Unit

    open fun World.initPrefabs(entity: Entity) = Unit
    open fun World.cleanupPrefabs(entity: Entity) = Unit

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

    override fun World.onAdd(entity: Entity) {
        // Only run init function on components when the game is running and not when we load snapshots
        val gameState = inject<GameStateManager>("GameStateManager")
        if (gameState.gameRunning) {
            initComponent(entity)
        }
        // Call init prefabs always
        initPrefabs(entity)
    }
    /**
     * Function that is called when the component is removed from an entity.
     */
    override fun World.onRemove(entity: Entity) {
        // Call cleanup prefabs always
        cleanupPrefabs(entity)
        // Do not free the component if the game is not running - i.e. during the snapshot rewind / forward feature
        val gameState = inject<GameStateManager>("GameStateManager")
        if (gameState.gameRunning) {
            // Call cleanup function to reset the component when requested by fleks world by calling onRemove
            cleanupComponent(entity)
            free()
        }
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
    val pool = Pool(preallocate, gen)
    add("PoolCmp${componentType.id}", pool)
}

fun <T : Poolable<T>> InjectableConfiguration.addPool(componentType: ComponentType<T>, pool: Pool<T>) {
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
 * pushes back one element to the pool. Entities will be reset/cleanup before freeing.
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
    constructor(preallocate: Int = 0, gen: (Int) -> T) : this() {
        setup( preallocate, gen)
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
