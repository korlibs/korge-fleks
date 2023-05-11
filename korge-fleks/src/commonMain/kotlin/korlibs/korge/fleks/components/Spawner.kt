package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.entity.config.nullEntity
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("Spawner")
data class Spawner(
    // Config for spawner
    var numberOfObjects: Int = 1,                  // The spawner will generate this number of object when triggered after interval time
    var interval: Int = 1,                         // 0 - disabled, 1 - every frame, 2 - every second frame, 3 - every third frame,...
    var timeVariation: Int = 0,                    // 0 - no variation, 1 - one frame variation, 2 - two frames variation, ...
    var positionVariation: Float = 0.0f,           // variation radius where objects will be spawned - 0.0 = no variation
    var newEntity: Entity = nullEntity,            // If spawner shall take a specific entity for spawning it can be set here
    var entityName: String = "NullEntity",
    @Serializable(InvokableSerializer::class)
    var configureFunction: Invokable = World::noFunction, // Name of function which configures the new entity
    var totalNumberOfObjects: Int = -1,            // -1 - unlimited number of objects spawned, x = x-number of objects spawned in total
    // internal state
    var nextSpawnIn: Int = 0,
    var numberOfObjectsSpawned: Int = 0
) : Component<Spawner>, SerializeBase {
    override fun type(): ComponentType<Spawner> = Spawner
    companion object : ComponentType<Spawner>()
}
