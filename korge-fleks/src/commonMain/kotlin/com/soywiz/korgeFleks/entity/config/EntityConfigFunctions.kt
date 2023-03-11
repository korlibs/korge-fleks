package com.soywiz.korgeFleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

/**
 * This interface defines the function prototype for creating entities.
 *
 * The parameters are:
 * - world: reference to the world
 * - entity: reference to a valid to-be-used entity for the new object
 * - config: a data class containing the specific configuration for the to-be-spawned entity
 */
// TODO adapt this to Invokable
interface EntityConfigFunctions {
    operator fun get(name: String) : (World, Entity, Config) -> Unit
}

/**
 *  Specify config properties which can be accessed by the [SpawnerSystem][com.soywiz.korgeFleks.systems.SpawnerSystem]
 */
interface Config
private class NoConfig: Config

// An empty config object for initialization of config properties of components.
// This config does not have any data per definition.
val noConfig: Config = NoConfig()

// The nullEntity should be the first created entity in a world. It is used to initialize entity properties of components.
// This entity does not have any components per definition.
val nullEntity: Entity = Entity(id = 0)

inline fun isNullEntity(entity: Entity) : Boolean = entity.id == 0
