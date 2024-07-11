package korlibs.korge.fleks.entity.config

import korlibs.korge.fleks.components.ParallaxComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.entity.EntityFactory.EntityConfig


data class ParallaxLayerConfig(
    override val name: String,
    
    private val assetName: String,
    private val layerTag: RenderLayerTag,
    private val x: Float = 0f,
    private val y: Float = 0f
) : EntityConfig {

    override val configureEntity = fun(world: World, entity: Entity): Entity = with(world) {
        entity.configure {
            it += PositionComponent(
                x = this@ParallaxLayerConfig.x,
                y = this@ParallaxLayerConfig.y
            )  // global position for the whole parallax background
            it += MotionComponent(
                velocityX = -12f  // world units per second
            )
            it += ParallaxComponent(
                name = assetName
            )
            it += layerTag
        }
        entity
    }

    init {
        EntityFactory.register(this)
    }
}
