package com.soywiz.korgeFleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.soywiz.korgeFleks.components.Appearance
import com.soywiz.korgeFleks.components.Drawable
import com.soywiz.korgeFleks.components.PositionShape
import com.soywiz.korgeFleks.components.TiledMap

object TiledMapBackground {

    data class TiledMapConfig(
        val assetName: String,
        val layerName: String,
        val x: Double = 0.0,
        val y: Double = 0.0,
        val alpha: Double = 1.0
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