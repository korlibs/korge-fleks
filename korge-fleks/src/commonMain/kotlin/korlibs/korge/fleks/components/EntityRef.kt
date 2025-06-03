package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to link one entity to another entity.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("EntityRef")
class EntityRef private constructor(
    var entity: Entity = Entity.NONE,
    // Configure what to do with the linked entity
    var moveWith: Boolean = true
) : PoolableComponents<EntityRef>() {
    override fun type() = EntityRefComponent

    companion object {
        val EntityRefComponent = componentTypeOf<EntityRef>()

        fun World.EntityRefComponent(config: EntityRef.() -> Unit ): EntityRef =
            getPoolable(EntityRefComponent).apply { config() }

        fun InjectableConfiguration.addEntityRefComponentPool(preAllocate: Int = 0) {
            addPool(EntityRefComponent, preAllocate) { EntityRef() }
        }
    }

    override fun World.clone(): EntityRef =
        getPoolable(EntityRefComponent).apply {
            // for the entity we just use the existing reference since Entity is a static data class (id and version are vals)
            entity = this@EntityRef.entity
            moveWith = this@EntityRef.moveWith
        }

    override fun World.cleanupComponent(entity: Entity) {
        this@EntityRef.entity = Entity.NONE
        moveWith = true
    }
}
