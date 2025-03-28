package korlibs.korge.fleks.utils.componentPool

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.*


/**
 * All components which shall be recorded (serialized) in SnapshotSerializerSystem needs to be derived from
 * [PoolableComponent].
 * The reset function is called when the component is going to be reused for a new entity.
 * The clone function needs to be implemented to perform a deep copy of all properties of the component for
 * the serialization of the game state.
 */
abstract class PoolableComponent1<T> : Component<T> {
    abstract fun clone(): Component<T>
}

abstract class PoolableComponent<T> : Component<T> {
    abstract fun reset()
    abstract fun World.clone(): Component<T>

    override fun World.onRemove(entity: Entity) {
        runCatching {
            val pool = inject<Pool<T>>("PoolCmp${type().id}")
            @Suppress("UNCHECKED_CAST")
            pool.free(this@PoolableComponent as T)

            println("Freeing component '${this@PoolableComponent.type().id}' from entity $entity")
        }
    }
}

/**
 * Creates a new pool for the specified [PoolableComponent].
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
 * Integrate as companion object in your component class like below and call that function in the injectable section
 * of the Fleks world configuration:
 *
 *     fun InjectableConfiguration.add"Component-Name"ComponentPool(preAllocate: Int = 0) {
 *         addPool(CollisionComponent.id, preAllocate) { "Component-Name"() }
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

