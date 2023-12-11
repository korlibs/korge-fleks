package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
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

typealias InvokableFunction = (World, Entity, Identifier) -> Entity

/**
 * This object contains all usable functions which can be invoked from components.
 * For that the components contain the [Identifier] for the function. Systems like [SpawnerSystem]
 * will then invoke the actual function by looking up the [Identifier] and executing [invoke].
 */
object Invokable {
    val map = mutableMapOf<Identifier, InvokableFunction>(
        nothing to fun(_: World, entity: Entity, _: Identifier) = entity
    )

    fun register(name: Identifier, invokableFct: InvokableFunction) {
        map[name] = invokableFct
    }

    fun unregister(name: Identifier) : InvokableFunction = map.remove(name) ?: throw Exception("Cannot unregister! Function with name '${name.name}' not registered in Invokables!")

    fun invoke(name: Identifier, world: World, entity: Entity, config: Identifier) : Entity =
        map[name]?.invoke(world, entity, config) ?: throw Exception("Cannot invoke! Function with name '${name.name}' not registered in Invokables!")
}
