package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.assetmanager.*
import korlibs.korge.fleks.utils.Identifier
import korlibs.korge.fleks.systems.SpawnerSystem


/**
 * The invalidEntity is used to initialize entity properties of components.
 * This entity is not existing and thus should never be created in a Fleks world.
 */
val invalidEntity: Entity = Entity.NONE
fun Entity.isInvalidEntity() : Boolean = this.id == -1

/**
 * Object which is used to initialize [Identifier] component properties.
 */
val nothing = Identifier(name = "nothing")



typealias InvokableFunction = (World, Entity/*, EntityConfig<*>*/) -> Entity

/**
 * This interface maps the string [name] to a specific configuration defined via a template "T : ConfigBase"
 * and a function implementation [functionImpl] which together create a specific entity.
 */
interface EntityConfig/*<T : ConfigBase>*/ {
//    val name: String
//    val config: T
//    val function: String
    val functionImpl: (World, Entity) -> Entity
}




/**
 * This factory object contains [EntityConfig]'s for creating new entities which are specified by
 * a specific configuration which is stored in the [EntityConfig] entries.
 *
 * Systems like [SpawnerSystem] call [createEntity] with an [EntityConfig] which will invoke the
 * by looking up the [Identifier] and executing [createEntity].
 */
object EntityFactory {

    private val entityConfigs: MutableMap<String, EntityConfig/*<*>*/> = mutableMapOf()

//    fun <T : ConfigBase> defineEntity(entityConfig: EntityConfig<T>) {
    fun defineEntity(name: String, entityConfig: EntityConfig/*<*>*/) {
        entityConfigs[name] = entityConfig
    }

    fun createEntity(name: String, world: World, entity: Entity) : Entity =
//        factoryFunctionsMap[config.function]?.invoke(world, entity, config) ?: throw Exception("Cannot invoke! Function with name '$name' not registered in Invokables!")
        entityConfigs[name]?.functionImpl?.invoke(world, entity/*, config*/)
            ?: throw Exception("Cannot invoke! Function with name '$name' not registered in EntityFactory!")


//    private val factoryFunctionsMap = mutableMapOf<String, InvokableFunction>(
//        "nothing" to fun(_: World, entity: Entity, _: EntityConfig<ConfigBase>) = entity
//    )

//    fun register(name: String, invokableFct: InvokableFunction) {
//        factoryFunctionsMap[name] = invokableFct
//    }

//    fun unregister(name: String) : InvokableFunction = factoryFunctionsMap.remove(name) ?: throw Exception("Cannot unregister! Function with name '$name' not registered in Invokables!")

}
