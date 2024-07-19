package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.assetmanager.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.entity.EntityFactory.EntityConfig
import korlibs.korge.fleks.tags.*
import kotlinx.serialization.Serializable


/**
 * This class contains the config for a specific level map with given [name].
 * The function implementation [configureEntity] creates a new [LevelMapConfig] entity and configures it
 * with the specified config details.
 *
 * This class creates a level map background entity which is used for various backgrounds in the game and intro.
 */
@Serializable
data class LevelMapConfig(
    override val name: String,

    private val mapType: TileMapType,
    private val assetName: String = "",  // Used with Tiled based maps
    private val worldName: String = "",  // Used with LDtk based maps
    private val levelName: String = "",  // Used with LDtk based maps
    private val layerTag: RenderLayerTag,
    private val x: Float = 0f,
    private val y: Float = 0f,
    private val alpha: Float = 1f,
) : EntityConfig {

    override fun World.entityConfigure(entity: Entity) : Entity {
        entity.configure {
            when (mapType) {
                TileMapType.LDTK -> it += LdtkLevelMapComponent(worldName, levelName)
                TileMapType.TILED -> it += TiledLevelMapComponent(assetName)
            }
            it += PositionComponent(
                x = this@LevelMapConfig.x,
                y = this@LevelMapConfig.y
            )
            it += SizeComponent()  // Size of level map needs to be set after loading of map is finished
            it += RgbaComponent().apply {
                alpha = this@LevelMapConfig.alpha
            }
            it += layerTag
        }
        return entity
    }

    init {
        // Register entity config into entity factory for lookup by its name
        EntityFactory.register(this)
    }
}
