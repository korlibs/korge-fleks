package korlibs.korge.fleks.components.data

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * TODO: We should not use this because we cannot call entity[SomeComponent] with it in systems
 */
@Serializable @SerialName("EntityVar")
class EntityVar private constructor(
    var id: Int = -1,
    var version: UInt = 0u
) : Poolable<EntityVar>() {
    override fun type() = EntityVarData

    companion object {
        val EntityVarData= componentTypeOf<EntityVar>()

        // Use this function to create a new instance as val inside a component
        fun value(): EntityVar = EntityVar()

        // Use this to initialize mutable lists of entities in components
        val NONE = EntityVar(-1, 0u)

        fun InjectableConfiguration.addEntityVarDataPool(preAllocate: Int = 0) {
            addPool(EntityVarData, preAllocate) { EntityVar() }
        }
    }

    override fun World.clone(): EntityVar =
        getPoolable(EntityVarData).apply {
            id = this@EntityVar.id
            version = this@EntityVar.version
        }

    fun cleanup() {
        id = -1
    }

    fun init(from: EntityVar) {
        id = from.id
        version = from.version
    }
}

fun MutableList<EntityVar>.init(from: List<EntityVar>) {
    this.addAll(from)
}

fun MutableMap<String, EntityVar>.init(from: Map<String, EntityVar>) {
    this.putAll(from)
}

fun <T> MutableList<EntityVar>.cleanup(pool: Pool<T>) {
    // Put all entities back to the pool
    while (isNotEmpty()) {
        @Suppress("UNCHECKED_CAST")
        pool.free(this.removeLast() as T)
    }
}

fun <T> MutableMap<String, EntityVar>.cleanup(pool: Pool<T>) {
    // Put all entities back to the pool
    while (isNotEmpty()) {
        @Suppress("UNCHECKED_CAST")
        pool.free(this.remove(this.keys.first()) as T)
    }
}
