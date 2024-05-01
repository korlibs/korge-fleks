package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.assetmanager.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.entity.EntityFactory.EntityConfig
import korlibs.korge.fleks.tags.*


/**
 * This class contains the config for a specific level map with given [name].
 * The function implementation [functionImpl] creates a new [LevelMap] entity and configures it
 * with the specified config details.
 *
 * This class creates a level map background entity which is used for various backgrounds in the game and intro.
 */
data class LevelMap(
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

    override val functionImpl = fun(world: World, entity: Entity) = with(world) {
        entity.configure {
            when (mapType) {
                TileMapType.LDTK -> it += LdtkLevelMapComponent(worldName, levelName)
                TileMapType.TILED -> it += TiledLevelMapComponent(assetName)
            }
            it += PositionComponent(
                x = this@LevelMap.x,
                y = this@LevelMap.y
            )
            it += SizeComponent()  // Size of level map needs to be set after loading of map is finished
            it += RgbaComponent().apply {
                alpha = this@LevelMap.alpha
            }
            it += layerTag
        }
        entity
    }

    init {
        // Register entity config into entity factory for lookup by its name
        EntityFactory.register(this)
    }
}
