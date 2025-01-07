package korlibs.korge.fleks.entity

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.utils.*


/**
 * This factory contains [EntityConfig]'s for creating new entities with components which are
 * specified by running the [entityConfigure][EntityConfig.entityConfigure] function.
 * Thus, by invoking [createAndConfigure] or [configure] functions an entity will be returned that
 * represents the configured game object.
 *
 * New [EntityConfig]'s needs to be added on creation to the factory with the [register]
 * method. That makes the [EntityConfig] available everywhere in the game by just its name.
 * Systems like [SpawnerSystem] call [configure] with a name parameter which maps to the specific
 * [EntityConfig].
 */
object EntityFactory {
    // Internal storage for entity config objects
    private val entityConfigs: MutableMap<String, EntityConfig> = mutableMapOf()

    /**
     * This function adds an [EntityConfig] to the internal config storage.
     * If another [EntityConfig] with the same [name][EntityConfig.name] already exists than
     * the existing config object will be replaced.
     */
    fun register(entityConfig: EntityConfig) {
        entityConfigs[entityConfig.name] = entityConfig
    }

    /**
     * This function checks if an [EntityConfig] with a specific [name] is already registered in the factory.
     */
    fun contains(name: String) = entityConfigs.containsKey(name)

    /**
     * Configure (after optional creation of a new entity) an entity with a specific [EntityConfig] by calling the
     * [entityConigure][EntityConfig.entityConfigure function.
     */
    fun createAndConfigure(entityConfig: String, world: World) : Entity = configure(entityConfig, world, Entity.NONE)
    fun configure(name: String, world: World, entity: Entity) : Entity {
        with (world) {
            // Make sure we have entity with InfoComponent for better traceability
            val baseEntity = if (entity == Entity.NONE) entity(name)
            else entity.apply { configure { it += InfoComponent(name) } }

            val entityConfig = entityConfigs[name]
            return if (entityConfig != null) {
                // println("INFO: Configure entity '${baseEntity.id}' with '${entityConfig.name}' EntityConfig.")
                entityConfig.run { world.entityConfigure(baseEntity) }
            } else {
                println("WARNING: Cannot invoke! EntityConfig with name '$name' not registered in EntityFactory!")
                entity
            }
        }
    }
}
