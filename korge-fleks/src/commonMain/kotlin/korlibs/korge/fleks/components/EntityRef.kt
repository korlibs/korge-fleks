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
) : Poolable<EntityRef>() {
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


/**
 * This component is used to link one entity to multiple other entities by name.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("EntityRefsByName")
class EntityRefsByName private constructor(
    val entitiesByName: MutableMap<String, Entity> = mutableMapOf(),
    // Configure what to do with the linked entities
    var moveWith: Boolean = false
) : Poolable<EntityRefsByName>() {
    override fun type() = EntityRefsByNameComponent

    companion object {
        val EntityRefsByNameComponent = componentTypeOf<EntityRefsByName>()

        fun World.EntityRefsByNameComponent(config: EntityRefsByName.() -> Unit ): EntityRefsByName =
            getPoolable(EntityRefsByNameComponent).apply { config() }

        fun InjectableConfiguration.addEntityRefsByNameComponentPool(preAllocate: Int = 0) {
            addPool(EntityRefsByNameComponent, preAllocate) { EntityRefsByName() }
        }
    }

    override fun World.clone(): EntityRefsByName =
        getPoolable(EntityRefsByNameComponent).apply {
            entitiesByName.init(from = this@EntityRefsByName.entitiesByName)
            moveWith = this@EntityRefsByName.moveWith
        }

    override fun World.cleanupComponent(entity: Entity) {
        entitiesByName.clear()
        moveWith = true
    }
}
