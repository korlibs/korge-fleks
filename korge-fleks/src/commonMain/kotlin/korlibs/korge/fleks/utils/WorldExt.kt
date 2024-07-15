package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.systems.*

/**
 * Create new [Entity] and add a name to it for easier debugging/tracing.
 */
fun World.entity(name: String, configuration: EntityCreateContext.(Entity) -> Unit = {}) : Entity =
    entity(configuration).apply { configure { it += InfoComponent(name) } }

/**
 * Delete function for [Entity] which let the [LifeCycleSystem] delete and cleanup all sub-entities, too.
 */
fun World.deleteViaLifeCycle(entity: Entity) {
    entity.configure { it.getOrAdd(LifeCycleComponent) { LifeCycleComponent() }.apply { healthCounter = 0 } }
}

/**
 * Create and configure a new [Entity] by applying the [EntityConfig][EntityFactory.EntityConfig] which is specified
 * by entityConfig string parameter.
 */
fun World.createAndConfigureEntity(entityConfig: String) : Entity =
    EntityFactory.createAndConfigure(entityConfig, this)

/**
 * Configure existing [Entity] by applying the [EntityConfig][EntityFactory.EntityConfig] which is specified
 * by entityConfig string parameter.
 */
fun World.configureEntity(entityConfig: String, entity: Entity) : Entity =
    EntityFactory.configure(entityConfig, this, entity)

/**
 * Execute function which takes an [EntityConfig][EntityFactory.EntityConfig] and an [Entity] to execute the desired
 * behaviour on the given entity. Basically the same as configureEntity, but defined for better code readability.
 */
fun World.execute(entityConfig: String, entity: Entity) : Entity =
    configureEntity(entityConfig, entity)
