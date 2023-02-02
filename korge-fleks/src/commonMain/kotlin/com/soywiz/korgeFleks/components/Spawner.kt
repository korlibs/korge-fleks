package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.soywiz.korgeFleks.entity.config.Config
import com.soywiz.korgeFleks.entity.config.noConfig
import com.soywiz.korgeFleks.entity.config.nullEntity

data class Spawner(
    // Config for spawner
    var numberOfObjects: Int = 1,                  // The spawner will generate this number of object when triggered after interval time
    var interval: Int = 1,                         // 0 - disabled, 1 - every frame, 2 - every second frame, 3 - every third frame,...
    var timeVariation: Int = 0,                    // 0 - no variation, 1 - one frame variation, 2 - two frames variation, ...
    var positionVariation: Double = 0.0,           // variation radius where objects will be spawned - 0.0 = no variation
    var config: Config = noConfig,                 // contains additional config values which will be given to the configure function of the created entity
    var newEntity: Entity = nullEntity,            // If spawner shall take a specific entity for spawning it can be set here
    var configureFunction: String = "",            // Name of function which spawns the new object
    var totalNumberOfObjects: Int = -1,            // -1 - unlimited number of objects spawned, x = x-number of objects spawned in total
    // internal state
    var nextSpawnIn: Int = 0,
    var numberOfObjectsSpawned: Int = 0
) : Component<Spawner> {
    override fun type(): ComponentType<Spawner> = Spawner
    companion object : ComponentType<Spawner>()
}
