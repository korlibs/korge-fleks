package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.Motion.Companion.motionComponent
import korlibs.korge.fleks.components.Parallax.Companion.parallaxComponent
import korlibs.korge.fleks.components.Position.Companion.positionComponent
import korlibs.korge.fleks.components.Rgba.Companion.rgbaComponent
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.systems.CameraSystem
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


@Serializable @SerialName("ParallaxEffectConfig")
data class ParallaxEffectConfig(
    override val name: String,

    private val assetName: String,
    private val backgroundLayerNames: List<String> = emptyList(),
    private val foregroundLayerNames: List<String> = emptyList(),

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
//            backgroundLayerNames.forEach { layerName ->
//                it += positionComponent {}  // local position of each layer, will be updated in ParallaxSystem
//                it += parallaxComponent { name = layerName }
//                it += rgbaComponent {}  // default white color tint
//                it += entityRefComponent { this.entity = entity }  // reference to parent entity for global position
//                it += layerTag
//            }
//            foregroundLayerNames.forEach { layerName ->
//                it += positionComponent {}  // local position of each layer, will be updated in ParallaxSystem
//                it += parallaxComponent { name = layerName }
//                it += rgbaComponent {}  // default white color tint
//                it += entityRefComponent { this.entity = entity }  // reference to parent entity for global position
//                it += layerTag
//            }


            it += motionComponent {}




            it += parallaxComponent {
                backgroundLayerNames.forEach { layerName ->
                    bgLayerEntities[layerName] =
                        createEntity("Parallax BG layer '$layerName' of entity '${entity.id}'") {
                            it += positionComponent {}
                            it += rgbaComponent {}
                        }
                }

                foregroundLayerNames.forEach { layerName ->
                    fgLayerEntities[layerName] =
                        createEntity("Parallax FG layer '$layerName' of entity '${entity.id}'") {
                            it += positionComponent {}
                            it += rgbaComponent {}
                        }
                }
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

            it += layerTag
        }
        // Get height of the parallax background
        val parallaxConfig = inject<AssetStore>("AssetStore").getParallaxConfig(assetName)
        val parallaxLayerHeight: Float = parallaxConfig.size
        val offset: Float = parallaxConfig.offset.toFloat()
        // Set parallax height and offset in the camera system
        system<CameraSystem>().parallaxHeight = parallaxLayerHeight - offset
        system<CameraSystem>().parallaxOffset = offset

        return entity
    }

    init {
        EntityFactory.register(this)
    }
}
