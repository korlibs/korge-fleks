package korlibs.korge.fleks.entity

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.utils.*


/**
 * This factory contains [EntityBlueprint]'s for creating new entities with components which are
 * specified by running the [entityConfigure][EntityBlueprint.entityConfigure] function.
 * Thus, by invoking [createAndConfigureEntity] or [configureEntity] functions an entity will be returned that
 * represents the configured game object.
 *
 * New [EntityBlueprint]'s needs to be added on creation to the factory with the [register]
 * method. That makes the [EntityBlueprint] available everywhere in the game by just its name.
 * Systems like [SpawnerSystem] call [configureEntity] with a name parameter which maps to the specific
 * [EntityBlueprint].
 */
object EntityFactory {
    // Internal storage for entity config objects
    private val entityConfigs: MutableMap<String, EntityBlueprint> = mutableMapOf()

    /**
     * This function adds an [EntityBlueprint] to the internal config storage.
     * If another [EntityBlueprint] with the same [name][EntityBlueprint.name] already exists than
     * the existing config object will NOT be replaced. This is done in order to have
     * base configuration for multiple entities which is identical.
     */
    fun register(entityBlueprint: EntityBlueprint) {
        if (!entityConfigs.containsKey(entityBlueprint.name)) {
            entityConfigs[entityBlueprint.name] = entityBlueprint
        }
    }

    /**
     * This function checks if an [EntityBlueprint] with a specific [name] is already registered in the factory.
     */
    fun contains(name: String) = entityConfigs.containsKey(name)

    /**
     * Configure (after optional creation of a new entity) an entity with a specific [EntityBlueprint] by calling the
     * [entityConigure][EntityConfig.entityConfigure function.
     */
    fun createAndConfigureEntity(world: World, entityConfig: String) : Entity = configureEntity(world, entityConfig, Entity.NONE)
    fun configureEntity(world: World, name: String, entity: Entity) : Entity {
        with (world) {
            // Make sure we have entity with InfoComponent for better traceability
            val baseEntity = if (entity == Entity.NONE) createEntity("EntityFactory: $name") else entity

            val entityConfig = entityConfigs[name]
            return if (entityConfig != null) {
                // println("INFO: Configure entity '${baseEntity.id}' with '${entityConfig.name}' EntityConfig.")
                entityConfig.run { world.entityConfigure(baseEntity) }
            } else {
                println("ERROR: Cannot invoke! EntityConfig with name '$name' not registered in EntityFactory!")
                baseEntity
            }
        }
    }
}
