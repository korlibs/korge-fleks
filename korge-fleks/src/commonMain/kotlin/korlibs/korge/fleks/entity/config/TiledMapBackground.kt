package korlibs.korge.fleks.entity.config

import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.assets.EntityConfig
import korlibs.korge.fleks.components.Appearance
import korlibs.korge.fleks.components.Drawable
import korlibs.korge.fleks.components.PositionShape
import korlibs.korge.fleks.components.TiledMap
import korlibs.korge.fleks.utils.Invokable
import korlibs.korge.fleks.utils.InvokableSerializer


object TiledMapBackground {

    data class Config(
        val assetName: String,
        val layerName: String,
        val x: Float = 0.0f,
        val y: Float = 0.0f,
        val alpha: Float = 1.0f
    ) : EntityConfig

    /**
     * This function creates a tiled map background entity which is used for various backgrounds in the game and intro.
     */
    val configureTiledMap = Invokable { world, entity, config ->
        with(world) {
            val tiledMapConfig = inject<AssetStore>("AssetStore").getEntityConfig<Config>(config)
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
        }
        entity
    }

    init {
        InvokableSerializer.register(
            configureTiledMap
        )
    }
}
