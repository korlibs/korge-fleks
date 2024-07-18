package korlibs.korge.fleks.entity

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.entity.GameObjectFactory.register
import korlibs.korge.fleks.utils.*


/**
 * This class maps the string [name] to a specific game object configuration process.
 * The game object entity will be created by the [entityConfigure] function which can be configured through additional
 * config properties in the derived class.
 */
class GameObject(
    val name: String,
    val entityConfig: Any = Any(),
    val entityConfigure: World.(entity: Entity, entityConfig: Any) -> Entity
) {
    init {
        register(this)
    }
}

/**
 * This factory contains [GameObject] configs for creating new entities which are specified by
 * running the [entityConfigure][GameObject.entityConfigure] function.
 * Thus, by invoking [createAndConfigure] or [configure] functions an entity will be returned that
 * represents the configured game object.
 *
 * New [GameObject]'s will be added automatically on creation to the factory with the [register]
 * method. That makes the [GameObject] available everywhere in the game by just its name.
 * Systems like [SpawnerSystem] call [configure] with a name parameter which maps to the specific
 * [GameObject].
 */
object GameObjectFactory {
    private val entityConfigs: MutableMap<String, GameObject> = mutableMapOf()

    /**
     * This function adds an [GameObject] to the internal config storage.
     * If another [GameObject] with the same [name][GameObject.name] already exists than adding
     * will be omitted.
     */
    fun register(gameObject: GameObject) {
        if (!entityConfigs.containsKey(gameObject.name)) entityConfigs[gameObject.name] = gameObject
    }

    fun createAndConfigure(entityConfig: String, world: World) : Entity = configure(entityConfig, world, Entity.NONE)
    fun configure(entityConfig: String, world: World, entity: Entity) : Entity {
        with (world) {
            // Make sure we have entity with InfoComponent for better traceability
            val baseEntity = if (entity == Entity.NONE) entity(entityConfig)
            else entity.apply { configure { it += InfoComponent(entityConfig) } }

            val gameObject = entityConfigs[entityConfig]
            return if (gameObject != null) {
                gameObject.entityConfigure(world, baseEntity, gameObject.entityConfig)
            } else {
                println("WARNING: Cannot invoke! EntityConfig with name '$entityConfig' not registered in EntityFactory!")
                entity
            }
        }
    }
}
