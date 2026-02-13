package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.WorldMap.Companion.worldMapComponent
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


/**
 * This class contains the config for a specific level map with give.
 * The function implementation [configureEntity] creates a new [WorldMapConfig] entity and configures it
 * with the specified config details.
 *
 * This class creates a world map entity which is used to store chunk config like spawnedEntities.
 */
@Serializable @SerialName("WorldMapConfig")
data class WorldMapConfig(
    override val name: String
) : EntityConfig {

    override fun World.entityConfigure(entity: Entity) : Entity {
        entity.configure {
            it += worldMapComponent {}

            // Enable debug rendering for collision layer
//            it += RenderLayerTag.DEBUG
//            it += DebugInfoTag.LEVEL_MAP_COLLISION_BOUNDS
        }
        return entity
    }

    init {
        // Register entity config into entity factory for lookup by its name
        EntityFactory.register(this)
    }
}
