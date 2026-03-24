package korlibs.korge.fleks.entity.blueprints

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.WorldMap.Companion.worldMapComponent
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


/**
 * This class contains the config for a specific level map with give.
 * The function implementation [entityConfigure] creates a new [WorldMapBlueprint] entity and configures it
 * with the specified config details.
 *
 * This class creates a world map entity which is used to store chunk config like spawnedEntities.
 */
@Serializable @SerialName("WorldMapBlueprint")
data class WorldMapBlueprint(
    override val name: String
) : EntityBlueprint {

    override fun World.entityConfigure(entity: Entity) : Entity {
        entity.configure {
            it += worldMapComponent {}
        }
        return entity
    }

    init {
        // Register entity config into entity factory for lookup by its name
        EntityFactory.register(this)
    }
}
