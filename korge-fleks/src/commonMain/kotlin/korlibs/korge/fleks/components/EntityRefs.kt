package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to link one entity to multiple other entities.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("EntityRefs")
class EntityRefs private constructor(
    val entities: MutableList<Entity> = mutableListOf(),
    // Configure what to do with the linked entities
    var moveWith: Boolean = true
) : Poolable<EntityRefs>() {
    override fun type() = EntityRefsComponent

    companion object {
        val EntityRefsComponent = componentTypeOf<EntityRefs>()

        fun World.EntityRefsComponent(config: EntityRefs.() -> Unit ): EntityRefs =
            getPoolable(EntityRefsComponent).apply { config() }

        fun InjectableConfiguration.addEntityRefsComponentPool(preAllocate: Int = 0) {
            addPool(EntityRefsComponent, preAllocate) { EntityRefs() }
        }
    }

    fun addAll(vararg newEntities: Entity) {
        entities.addAll(newEntities)
    }

    override fun World.clone(): EntityRefs =
        getPoolable(EntityRefsComponent).apply {
            entities.init(from = this@EntityRefs.entities)
            moveWith = this@EntityRefs.moveWith
        }

    override fun World.cleanupComponent(entity: Entity) {
        entities.clear()
        moveWith = true
    }
}
