package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.Appearance
import korlibs.korge.fleks.components.Drawable
import korlibs.korge.fleks.components.PositionShape
import korlibs.korge.fleks.components.TiledMap


object TiledMapBackground {

    data class TiledMapConfig(
        val assetName: String,
        val layerName: String,
        val x: Float = 0.0f,
        val y: Float = 0.0f,
        val alpha: Float = 1.0f
    )

    /**
     * This function creates a tiled map background entity which is used for various backgrounds in the game and intro.
     */
    fun createTiledMap(world: World, config: TiledMapConfig) : Entity {
        return world.entity {
            it += TiledMap(
                assetName = config.assetName
            )
            it += PositionShape(
                x = config.x,
                y = config.y
            )
            it += Drawable(
                drawOnLayer = config.layerName
            )
            it += Appearance(
                alpha = config.alpha
            )
        }
    }
}