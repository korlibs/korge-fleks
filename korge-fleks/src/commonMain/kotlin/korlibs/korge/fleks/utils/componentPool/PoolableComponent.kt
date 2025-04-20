package korlibs.korge.fleks.utils.componentPool

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.gameState.*


// TODO: remove
abstract class CloneableComponent<T> : Component<T> {
    abstract fun clone(): Component<T>
}

/**
 * All components needs to be derived from [PoolableComponent] to be able to be reused from a Component pool and
 * to be recorded in the SnapshotSerializerSystem.
 *
 * The reset function is called when the component is going to be reused for a new entity.
 * The clone function needs to be implemented to perform a deep copy of all properties of the component for
 * the serialization of the game state.
 */
abstract class PoolableComponent<T> : Component<T> {
    abstract fun reset()  // feature of poolable
    abstract fun World.clone(): Component<T>  // feature of making snapshots on the fly
// TODO    abstract fun init(from: T)  // feature for poolable data inside of poolable components

    fun World.free() {  // feature of poolable
        runCatching {
            val pool = inject<Pool<T>>("PoolCmp${type().id}")
            @Suppress("UNCHECKED_CAST")
            pool.free(this@PoolableComponent as T)
            println("Freeing component '${this@PoolableComponent::class.simpleName}'")
        }
    }

    override fun World.onRemove(entity: Entity) {
        // Do not free the component if the game is not running - i.e. during the snapshot rewind / forward feature
        val gameState = inject<GameStateManager>("GameStateManager")
        if (gameState.gameRunning) free()
        println("Freeing component '${this@PoolableComponent.type().id}' from entity $entity")
    }
}

/**
 * Creates a new pool for the specified [PoolableComponent].
 *
 * Hint: Integrate as companion object in component class like below and call that function in the injectable section
 * of the Fleks world configuration:
 *
 *     fun InjectableConfiguration.addMyComponentPool(preAllocate: Int = 0) {
 *         addPool(MyComponent, preAllocate) { MyComponent() }
 *     }
 */
fun <T : PoolableComponent<T>> InjectableConfiguration.addPool(
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