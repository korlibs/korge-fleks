package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.assetmanager.AssetStore
import korlibs.korge.assetmanager.ConfigBase
import korlibs.korge.fleks.components.Appearance
import korlibs.korge.fleks.components.Drawable
import korlibs.korge.fleks.components.PositionShape
import korlibs.korge.fleks.components.TiledMap
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
    val configureTiledMap = Identifier(name = "configureTiledMap")

    /**
     * This function creates a tiled map background entity which is used for various backgrounds in the game and intro.
     */
    private val configureTiledMapFct = fun(world: World, entity: Entity, config: Identifier) = with(world) {
        val tiledMapConfig = inject<AssetStore>("AssetStore").getEntityConfig<Config>(config.name)
        entity.configure {
            it += TiledMap(
                assetName = tiledMapConfig.assetName
            )
            it += PositionShape(
                x = tiledMapConfig.x,
                y = tiledMapConfig.y
            )
            it += Drawable(
                drawOnLayer = tiledMapConfig.layerName
            )
            it += Appearance(
                alpha = tiledMapConfig.alpha
            )
        }
        entity
    }

    init {
        Invokable.register(configureTiledMap, configureTiledMapFct)
    }
}
