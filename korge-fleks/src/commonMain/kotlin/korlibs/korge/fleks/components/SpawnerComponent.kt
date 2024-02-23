package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import korlibs.korge.fleks.entity.config.invalidEntity
import korlibs.korge.fleks.entity.config.nothing
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("Spawner")
data class SpawnerComponent(
    // Config for spawner
    var numberOfObjects: Int = 1,                  // The spawner will generate this number of object when triggered after interval time
    var interval: Int = 1,                         // 0 - disabled, 1 - every frame, 2 - every second frame, 3 - every third frame,...
    var timeVariation: Int = 0,                    // 0 - no variation, 1 - one frame variation, 2 - two frames variation, ...
    var positionVariation: Float = 0.0f,           // variation radius where objects will be spawned - 0.0 = no variation
    var newEntity: Entity = invalidEntity,         // If spawner shall take a specific entity for spawning it can be set here
    var config: Identifier = nothing,             // Name of configuration which is used in below function to configure the new entity
    var function: Identifier = nothing,           // Name of function which configures the new entity
    var totalNumberOfObjects: Int = -1,            // -1 - unlimited number of objects spawned, x = x-number of objects spawned in total
    // internal state
    var nextSpawnIn: Int = 0,
    var numberOfObjectsSpawned: Int = 0
) : Component<SpawnerComponent> {
    override fun type(): ComponentType<SpawnerComponent> = SpawnerComponent
    companion object : ComponentType<SpawnerComponent>()
}
