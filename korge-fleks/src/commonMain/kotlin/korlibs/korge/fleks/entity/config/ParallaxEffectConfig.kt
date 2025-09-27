package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.datastructure.iterators.fastForEachReverse
import korlibs.korge.fleks.components.EntityRef.Companion.entityRefComponent
import korlibs.korge.fleks.components.EntityRefsByName.Companion.entityRefsByNameComponent
import korlibs.korge.fleks.components.Layer.Companion.layerComponent
import korlibs.korge.fleks.components.Motion.Companion.motionComponent
import korlibs.korge.fleks.components.Parallax.Companion.parallaxComponent
import korlibs.korge.fleks.components.Position.Companion.positionComponent
import korlibs.korge.fleks.components.Sprite.Companion.spriteComponent
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


@Serializable @SerialName("ParallaxEffectConfig")
data class ParallaxEffectConfig(
    override val name: String,

    private val layerTextureNames: List<String>,
    private val layerTag: RenderLayerTag,
    private val x: Float = 0f,
    private val y: Float = 0f
) : EntityConfig {

    override fun World.entityConfigure(entity: Entity) : Entity {
        entity.configure {
            // Gobal position of parallax background in screen coordinates
            it += positionComponent {
                x = this@ParallaxEffectConfig.x  // global position in screen coordinates
                y = this@ParallaxEffectConfig.y
            }
            it += motionComponent {}
            it += entityRefsByNameComponent {
                layerTextureNames.fastForEachReverse { layerTextureName ->
                    val layerEntity = createEntity(layerTextureName) { layerEntity ->
                        layerEntity += entityRefComponent {
                            // Reference to parent entity to get the global position for rendering
                            this.entity = entity
                        }
                        // Local position of parallax layer in parallax background coordinates
                        // This position will be updated by ParallaxSystem based on speed factor
                        layerEntity += positionComponent {}
                        layerEntity += motionComponent {}
                        layerEntity += spriteComponent {
                            name = layerTextureName
                        }
                        layerEntity += parallaxComponent {}
                        layerEntity += layerComponent { index = 1 }
                    }
                    add(layerTextureName, layerEntity)
                }
            }
            it += layerTag
        }
        return entity
    }

    init {
        EntityFactory.register(this)
    }
}
