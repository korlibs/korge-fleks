package korlibs.korge.fleks.utils.componentPool

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.systems.*


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

/**
 * All components needs to be derived from [PoolableComponent] to be able to be reused from a Component pool and
 * to be snapshot in the SnapshotSerializerSystem.
 */
abstract class PoolableComponent<T> : Component<T> {
    abstract fun reset()  // feature of poolable
    abstract fun World.clone(): Component<T>  // feature of making snapshots on the fly

//    abstract val poolType: PoolType<T>

    override fun World.onRemove(entity: Entity) {
        // Do not free the component if the game is not running - i.e. during the snapshot rewind / forward feature
        val system = system<SnapshotSerializerSystem>()
        if (system.gameRunning) {
            runCatching {
                val pool = inject<Pool<T>>("PoolCmp${type().id}")
                @Suppress("UNCHECKED_CAST")
                pool.free(this@PoolableComponent as T)

                println("Freeing component '${this@PoolableComponent.type().id}' from entity $entity")
            }
        }
    }
}

//interface PoolType<T> {
//    val poolName: String
//
//    fun alloc(world: World): T {
//        val pool = try {
//            world.inject<Pool<T>>(poolName)
//        } catch (e: FleksNoSuchInjectableException) {
//            error("Attempting to allocate to pool '$poolName' without adding it to injectables! Ensure to call 'addPool' and specify ${this::class.simpleName} as the type.")
//        }
//        return pool.alloc()
//    }
//}
//
//inline fun <reified T> poolTypeOf(
//    typeName: String = T::class.simpleName ?: T::class.toString(),
//): PoolType<T> = object : PoolType<T> {
//    override val poolName: String = "${typeName}Pool"
//}

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
