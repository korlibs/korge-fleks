package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.data.*
import korlibs.korge.fleks.components.data.EntityVar.Companion.EntityVarData
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This component is used to store a list of sub-entities either as entity list or a lookup map with
 * entity names as references to other entities.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("SubEntities")
class SubEntities private constructor(
    val subEntities: MutableList<Entity> = mutableListOf(),
    val subEntitiesByName: MutableMap<String, Entity> = mutableMapOf(),
    // Configure what to do with the linked entities
    var moveWith: Boolean = false,  // Not used currently!
) : Poolable<SubEntities>() {
    override fun type() = SubEntitiesComponent

    companion object {
        val SubEntitiesComponent = componentTypeOf<SubEntities>()

        fun World.SubEntitiesComponent(config: SubEntities.() -> Unit ): SubEntities =
            getPoolable(SubEntitiesComponent).apply { config() }

        fun InjectableConfiguration.addSubEntitiesComponentPool(preAllocate: Int = 0) {
            addPool(SubEntitiesComponent, preAllocate) { SubEntities() }
        }
    }

    fun getSubEntity(name: String) : EntityVar =
        if (subEntitiesByName.contains(name)) subEntitiesByName[name]!!
        else EntityVar.NONE

    override fun World.clone(): SubEntities =
        getPoolable(SubEntitiesComponent).apply {
            subEntities.init(from = this@SubEntities.subEntities)
            subEntitiesByName.init(from = this@SubEntities.subEntitiesByName)
        }

    override fun World.initComponent(entity: Entity) {
    }

    override fun World.cleanupComponent(entity: Entity) {
        val pool = getPool(EntityVarData)
        subEntities.cleanup(pool)
        subEntitiesByName.cleanup(pool)
    }
}
