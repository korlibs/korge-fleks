package korlibs.korge.fleks.entity

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.utils.*


/**
 * This factory object contains [EntityConfig]'s for creating new entities which are specified by
 * a specific configuration which is stored in the derived [EntityConfig] entries.
 *
 * New [EntityConfig]'s needs to be added to the factory with the [register] method. That makes the
 * [EntityConfig] available everywhere in the game by just its name.
 * Systems like [SpawnerSystem] call [configure] with a name parameter which maps to a specific [EntityConfig].
 */
object EntityFactory {

    /**
     * This interface maps the string [name] to a specific entity configuration process.
     * The entity will be created by the [configureEntity] which can be configured through additional
     * config properties in the derived class.
     */
    interface EntityConfig {
        val name: String
        // TODO idea to make game object data serializable
        //val data: Any

        val configureEntity: (World, Entity) -> Entity
        // TODO  - check if this is really needed -  add possibility for a specific entityConfig at creation of the game object
        //val configureEntity: (World, Entity, EntityConfig) -> Entity
    }

    private val entityConfigs: MutableMap<String, EntityConfig> = mutableMapOf()

    /**
     * This function adds an [EntityConfig] to the internal config storage.
     * If another [EntityConfig] with the same [name][EntityConfig.name] already exists than adding
     * will be omitted.
     */
    fun register(entityConfig: EntityConfig) {
        if (!entityConfigs.containsKey(entityConfig.name)) entityConfigs[entityConfig.name] = entityConfig
    }

    fun createAndConfigure(entityConfig: String, world: World) : Entity = configure(entityConfig, world, Entity.NONE)
    fun configure(entityConfig: String, world: World, entity: Entity) : Entity {
        with (world) {
            // Make sure we have entity with InfoComponent for better traceability
            val baseEntity = if (entity == Entity.NONE) entity(entityConfig)
            else entity.apply { configure { it += InfoComponent(entityConfig) } }

            val configuredEntity = entityConfigs[entityConfig]?.configureEntity?.invoke(world, baseEntity)
            if (configuredEntity != null) return configuredEntity
            else {
                println("WARNING: Cannot invoke! EntityConfig with name '$entityConfig' not registered in EntityFactory!")
                return entity
            }
        }

    }
}
