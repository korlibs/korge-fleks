package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.assetmanager.AssetStore
import korlibs.korge.assetmanager.ConfigBase
import korlibs.korge.fleks.components.AppearanceComponent
import korlibs.korge.fleks.components.DrawableComponent
import korlibs.korge.fleks.components.PositionShapeComponent
import korlibs.korge.fleks.components.TiledMapComponent
import korlibs.korge.fleks.utils.Identifier


object TiledMapBackground {

    data class Config(
        val assetName: String,
        val layerName: String,
        val x: Double = 0.0,
        val y: Double = 0.0,
        val alpha: Float = 1.0f
    ) : ConfigBase

    // Used in component properties to specify invokable function
    val configureTileMap = Identifier(name = "configureTileMap")

    /**
     * This function creates a tiled map background entity which is used for various backgrounds in the game and intro.
     */
    private val configureTiledMapFct = fun(world: World, entity: Entity, config: Identifier) = with(world) {
        val tileMapConfig = AssetStore.getEntityConfig<Config>(config.name)
        entity.configure {
            it += TiledMapComponent(
                assetName = tileMapConfig.assetName
            )
            it += PositionShapeComponent(
                x = tileMapConfig.x,
                y = tileMapConfig.y
            )
            it += DrawableComponent(
                drawOnLayer = tileMapConfig.layerName
            )
            it += AppearanceComponent(
                alpha = tileMapConfig.alpha
            )
        }
        entity
    }

    init {
        Invokable.register(configureTileMap, configureTiledMapFct)
    }
}
