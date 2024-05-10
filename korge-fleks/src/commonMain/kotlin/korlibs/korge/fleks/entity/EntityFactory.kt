package korlibs.korge.fleks.entity

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
//import korlibs.korge.fleks.systems.SpawnerSystem  // TODO enable after refactoring finished


/**
 * This factory object contains [EntityConfig]'s for creating new entities which are specified by
 * a specific configuration which is stored in the derived [EntityConfig] entries.
 *
 * New [EntityConfig]'s needs to be added to the factory with the [register] method. That makes the
 * [EntityConfig] available everywhere in the game by just its name.
 * Systems like [SpawnerSystem] call [createEntity] with a name parameter which maps to a specific [EntityConfig].
 */
object EntityFactory {

    /**
     * This interface maps the string [name] to a specific entity configuration process.
     * The entity will be created by the [configureEntity] which can be configured through additional
     * config properties in the derived class.
     */
    interface EntityConfig {
        val name: String
        val configureEntity: (World, Entity) -> Entity
    }

    private val entityConfigs: MutableMap<String, EntityConfig> = mutableMapOf()

    fun register(entityConfig: EntityConfig) {
        entityConfigs[entityConfig.name] = entityConfig
    }

    fun createEntity(name: String, world: World, entity: Entity) : Entity {
        val configuredEntity = entityConfigs[name]?.configureEntity?.invoke(world, entity)
        if (configuredEntity != null) return configuredEntity
        else {
            println("WARNING: Cannot invoke! Function with name '$name' not registered in EntityFactory!")
            return entity
        }
    }
}
