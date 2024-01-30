package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.assetmanager.AssetStore
import korlibs.korge.assetmanager.ConfigBase
import korlibs.korge.fleks.components.Appearance
import korlibs.korge.fleks.components.Drawable
import korlibs.korge.fleks.components.PositionShape
import korlibs.korge.fleks.components.TileMap
import korlibs.korge.fleks.utils.Identifier


object TiledMapBackground {

    data class Config(
        val assetName: String,
        val layerName: String,
        val x: Float = 0.0f,
        val y: Float = 0.0f,
        val alpha: Float = 1.0f
    ) : ConfigBase

    // Used in component properties to specify invokable function
    val configureTileMap = Identifier(name = "configureTileMap")

    /**
     * This function creates a tiled map background entity which is used for various backgrounds in the game and intro.
     */
    private val configureTileMapFct = fun(world: World, entity: Entity, config: Identifier) = with(world) {
        val tileMapConfig = AssetStore.getEntityConfig<Config>(config.name)
        entity.configure {
            it += TileMap(
                assetName = tileMapConfig.assetName
            )
            it += PositionShape(
                x = tileMapConfig.x,
                y = tileMapConfig.y
            )
            it += Drawable(
                drawOnLayer = tileMapConfig.layerName
            )
            it += Appearance(
                alpha = tileMapConfig.alpha
            )
        }
        entity
    }

    init {
        Invokable.register(configureTileMap, configureTileMapFct)
    }
}
