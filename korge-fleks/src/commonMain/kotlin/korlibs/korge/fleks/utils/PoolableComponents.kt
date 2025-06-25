package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.*
import korlibs.datastructure.*
import korlibs.korge.fleks.gameState.*


// TODO: remove this class
abstract class PoolableComponents<T> : Component<T> {
    abstract fun World.clone(): PoolableComponents<T>  // feature of making snapshots on the fly

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
            pool.free(this@PoolableComponents as T)
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
 * All components needs to be derived from [PoolableComponents] to be able to be reused from a Component pool and
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
abstract class PoolableComponent<T> : Component<T> {
    open fun World.initComponent(entity: Entity) = Unit
    open fun World.cleanupComponent(entity: Entity) = Unit

    open fun World.initPrefabs(entity: Entity) = Unit
    open fun World.cleanupPrefabs(entity: Entity) = Unit

    /**
     * This function clones the component from the pool and initializes it to contain
     * the same data/properties as the original component. This feature is used to make snapshots on the fly.
     */
    abstract fun clone(): PoolableComponent<T>

    /**
     * The free function needs to be called if the component is not used anymore and should be freed.
     * Normally, this is done in the onRemove function of the component. But SnapshotSerializerSystem will call
     * this function directly to free the component when cleanup old snapshots.
     */
    abstract fun free()

    override fun World.onAdd(entity: Entity) {
        // Only run init function on components when the game is running and not when we load snapshots
        if (GameStateManager.gameRunning) {
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
        if (GameStateManager.gameRunning) {
            // Call cleanup function to reset the component when requested by fleks world by calling onRemove
            cleanupComponent(entity)
            println("Freeing component")
            free()
        }
    }
}

/**
 * Creates a new pool for the specified [PoolableComponents].
 *
 * Hint: Integrate as companion object in component class like below and call that function in the injectable section
 * of the Fleks world configuration:
 *
 *     fun InjectableConfiguration.addMyComponentPool(preAllocate: Int = 0) {
 *         addPool(MyComponent, preAllocate) { MyComponent() }
 *     }
 */
fun <T : PoolableComponents<T>> InjectableConfiguration.addPool(
    componentType: ComponentType<T>,
    preallocate: Int = 0,
    gen: (Int) -> T
) {
    val pool = Pool(preallocate, gen)
    add("PoolCmp${componentType.id}", pool)
}

fun <T : PoolableComponents<T>> InjectableConfiguration.addPool(componentType: ComponentType<T>, pool: Pool<T>) {
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
