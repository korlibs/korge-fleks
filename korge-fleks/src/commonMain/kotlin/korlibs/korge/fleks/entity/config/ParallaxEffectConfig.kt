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
import korlibs.korge.fleks.components.Rgba.Companion.rgbaComponent
import korlibs.korge.fleks.components.Sprite.Companion.spriteComponent
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


@Serializable @SerialName("ParallaxEffectConfig")
data class ParallaxEffectConfig(
    override val name: String,

    private val backgroundLayerNames: List<String>,

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

            it += parallaxComponent {
                backgroundLayerNames.fastForEachReverse { layerTextureName ->
                    bgLayerEntities.add(
                        createEntity("Parallax BG layer '$layerTextureName' of entity '${entity.id}'") {
                            it += positionComponent {}
                            it += rgbaComponent {}
                        }
                    )
                }

//                repeat(numberForegroundLayers) { index ->
//                    val name = assetStore.getBackground(name).config.foregroundLayers?.get(index)?.name ?: "No layer name"
//                    fgLayerEntities.add(
//                        world.createEntity("Parallax FG layer '$index' ($name) of entity '${entity.id}'") {
//                            it += positionComponent {}
//                            it += rgbaComponent {}
//                        }
//                    )
//                }
//
//                repeat(numberAttachedRearLayers) { attachedLayersRearPositions.add(0f) }
//                repeat(numberParallaxPlaneLines) { linePositions.add(0f) }
//                repeat(numberAttachedFrontLayers) { attachedLayersFrontPositions.add(0f) }
//
//                parallaxPlaneEntity = world.createEntity("Parallax plane of entity '${entity.id}'") {
//                    it += positionComponent {}
//                    it += rgbaComponent {}
//                }

            }
            it += layerTag
        }
        return entity
    }

    init {
        EntityFactory.register(this)
    }
}
