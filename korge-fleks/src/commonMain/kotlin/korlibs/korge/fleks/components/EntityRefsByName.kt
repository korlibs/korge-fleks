package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


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
