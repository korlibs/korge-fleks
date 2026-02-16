package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.Info.Companion.InfoComponent
import korlibs.korge.fleks.components.Info.Companion.infoComponent
import korlibs.korge.fleks.components.LifeCycle.Companion.LifeCycleComponent
import korlibs.korge.fleks.components.LifeCycle.Companion.lifeCycleComponent
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.systems.*
import korlibs.korge.fleks.tags.*

/**
 * Create new [Entity] and add a name to it for easier debugging/tracing.
 */
fun World.createEntity(aName: String, configuration: EntityCreateContext.(Entity) -> Unit = {}) : Entity {
    val newEntity = entity(configuration).apply { configure { it += infoComponent { name = aName } } }
    //println("Created entity: $aName with id: ${newEntity.id} (v${newEntity.version})")
    return newEntity
}

fun World.createEmptyEntity(aName: String, configuration: EntityCreateContext.(Entity) -> Unit = {}) : Entity =
    entity(configuration).apply {
        configure {
            it += infoComponent { name = aName }
            it += EmptyInitialized
        }
    }

/**
 * Delete function for [Entity] which let the [LifeCycleSystem] delete and cleanup all sub-entities, too.
 */
fun World.deleteViaLifeCycle(entity: Entity) {
    entity.configure { it.getOrAdd(LifeCycleComponent) { lifeCycleComponent {} }.apply { healthCounter = 0 } }
}

/**
 * Create and configure a new [Entity] by applying the [EntityConfig][EntityFactory.EntityConfig] which is specified
 * by entityConfig string parameter.
 */
fun World.createAndConfigureEntity(entityConfig: String) : Entity =
    EntityFactory.createAndConfigureEntity(this, entityConfig)

/**
 * Configure existing [Entity] by applying the [EntityConfig][EntityFactory.EntityConfig] which is specified
 * by entityConfig string parameter.
 */
fun World.configureEntity(entityConfig: String, entity: Entity) : Entity =
    EntityFactory.configureEntity(this, entityConfig, entity)

/**
 * Execute-function which takes an [EntityConfig][EntityFactory.EntityConfig] and an [Entity] to execute the desired
 * behaviour on the given entity. Basically the same as configureEntity, but defined for better code readability.
 */
fun World.execute(entityConfig: String, entity: Entity) : Entity =
    configureEntity(entityConfig, entity)

/**
 * Get name of entity if entity has InfoComponent
 */
fun World.nameOf(entity: Entity) : String = if (entity has InfoComponent) entity[InfoComponent].name else "noName"

/**
 * Get entity for main camera.
 */
fun World.getMainCamera(): Entity {
    val cameraFamily: Family = family { all(MainCameraTag, PositionComponent) }
    return cameraFamily.firstOrNull() ?: error("No main camera found in world!")
// For TESTING
//    if (cameraFamily.isEmpty) {
//        println("Snapshot: ${snapshot()}")
//        error("No main camera found in world!")
//    }
//    return cameraFamily.first()
}

fun World.getMainCameraOrNull(): Entity? {
    val cameraFamily: Family = family { all(MainCameraTag, PositionComponent) }
    return cameraFamily.firstOrNull()
}

/**
 * Print snapshot of components of given entity for debugging purposes.
 */
fun World.traceEntitySnapshot(entity: Entity) {
    println("Entity snapshot:\n ${system<SnapshotSerializerSystem>().traceEntitySnapshot(entity)}")
}
