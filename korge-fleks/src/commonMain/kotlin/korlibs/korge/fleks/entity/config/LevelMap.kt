package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.assetmanager.AssetStore
import korlibs.korge.assetmanager.ConfigBase
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.utils.Identifier


object LevelMap {

    // Config data class
    data class Config(
        val mapType: MapType,
        val assetName: String = "",
        val worldName: String = "",
        val levelName: String = "",
        val layerName: String,
        val x: Double = 0.0,
        val y: Double = 0.0,
        val alpha: Double = 1.0
    ) : ConfigBase

    enum class MapType { LDTK, TILED }

    // Used in component properties to specify invokable function
    val configureLevelMap = Identifier(name = "configureLevelMap")

    /**
     * This function creates a level map background entity which is used for various backgrounds in the game and intro.
     */
    private val configureLevelMapFct = fun(world: World, entity: Entity, config: Identifier) = with(world) {
        val levelMapConfig = AssetStore.getEntityConfig<Config>(config.name)
        entity.configure {
            when (levelMapConfig.mapType) {
                MapType.LDTK -> it += LdtkLevelMapComponent(levelMapConfig.worldName, levelMapConfig.levelName)
                MapType.TILED -> it += TiledLevelMapComponent(levelMapConfig.assetName)
            }
            it += PositionComponent(
                x = levelMapConfig.x,
                y = levelMapConfig.y
            )
            it += SizeComponent()  // Size of level map needs to be set after loading of map is finished
//            it += DrawableComponent(
//                drawOnLayer = levelMapConfig.layerName
//            )
// TODO move alpha in *LevelMapComponent
//            it += AppearanceComponent(
//                alpha = levelMapConfig.alpha
//            )
        }
        entity
    }

    // Init block which registers the configure function and its Identifier to the Invokable object store
    init {
        Invokable.register(configureLevelMap, configureLevelMapFct)
    }
}
