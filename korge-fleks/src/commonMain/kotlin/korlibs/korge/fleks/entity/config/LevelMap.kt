package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*



/**
 * This object maps the string [name] to a specific configuration defined via a template "T : ConfigBase"
 * and a function implementation [functionImpl] which together create a specific entity.
 */
/**
 * This class creates a level map background entity which is used for various backgrounds in the game and intro.
 */
class LevelMap(
//    override val name: String,
//    override val config: Config,
//    configFct: () -> Config
    private val mapType: MapType,
    private val assetName: String = "",  // Used with Tiled based maps
    private val worldName: String = "",  // Used with LDtk based maps
    private val levelName: String = "",  // Used with LDtk based maps
    private val layerTag: RenderLayerTag,
    private val x: Float = 0f,
    private val y: Float = 0f,
    private val alpha: Float = 1f
) : EntityConfig {

    enum class MapType { LDTK, TILED }
/*
    // Config data class
    data class Config(
        val mapType: MapType,
        val assetName: String = "",  // Used with Tiled based maps
        val worldName: String = "",  // Used with LDtk based maps
        val levelName: String = "",  // Used with LDtk based maps
        val layerTag: RenderLayerTag,
        val x: Float = 0f,
        val y: Float = 0f,
        val alpha: Float = 1f
    ) : ConfigBase {
        enum class MapType { LDTK, TILED }
    }
*/

//    override val config: Config = configFct.invoke()

    override val functionImpl = fun(world: World, entity: Entity/*, config: EntityConfig<*>*/) = with(world) {
//        val config = AssetStore.getEntityConfig<Config>(config.name)
        entity.configure {
            when (mapType) {
                MapType.LDTK -> it += LdtkLevelMapComponent(worldName, levelName)
                MapType.TILED -> it += TiledLevelMapComponent(assetName)
            }
            it += PositionComponent(
                x = x,
                y = y
            )
            it += SizeComponent()  // Size of level map needs to be set after loading of map is finished
            it += RgbaComponent().apply {
                alpha = alpha
            }
            it += layerTag
        }
        entity
    }

    init {
        // Register configure into entity factory as factory function for LevelMap entities
//        EntityFactory.register(name, functionImpl)
    }
}
