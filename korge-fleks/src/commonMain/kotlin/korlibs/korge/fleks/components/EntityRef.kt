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

val subEntities: MutableList<Entity> = mutableListOf(),
val subEntitiesByName: MutableMap<String, Entity> = mutableMapOf(),
// Configure what to do with the linked entities
var moveWith: Boolean = false,  // Not used currently!

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This component is used to ...
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("EntityRefs")
class EntityRefs private constructor(
    var answer: Int = 42
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
            answer = this@EntityRefs.answer
        }

    override fun World.initComponent(entity: Entity) {
    }

    override fun World.cleanupComponent(entity: Entity) {
        answer = 42
    }

    override fun World.initPrefabs(entity: Entity) {
    }

    override fun World.cleanupPrefabs(entity: Entity) {
    }
}

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This component is used to ...
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("EntityRefsByName")
class EntityRefsByName private constructor(
    var answer: Int = 42
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
            answer = this@EntityRefsByName.answer
        }

    override fun World.initComponent(entity: Entity) {
    }

    override fun World.cleanupComponent(entity: Entity) {
        answer = 42
    }

    override fun World.initPrefabs(entity: Entity) {
    }

    override fun World.cleanupPrefabs(entity: Entity) {
    }
}

