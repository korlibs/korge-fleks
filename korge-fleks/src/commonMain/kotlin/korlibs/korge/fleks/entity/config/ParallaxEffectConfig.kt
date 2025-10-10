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

    private val parallaxAssetName: String,
    private val backgroundLayerNames: List<String> = emptyList(),
    private val parallaxPlaneName: String = "",
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
            it += motionComponent {}
            it += parallaxComponent {
                name = parallaxAssetName

                backgroundLayerNames.forEach { layerName ->
                    bgLayerEntities[layerName] =
                        createEntity("Parallax BG layer '$layerName' of entity '${entity.id}'") {
                            it += positionComponent {}
                            it += rgbaComponent {}
                        }
                }

                if (parallaxPlaneName != "") {
                    parallaxPlane.name = parallaxPlaneName
                    parallaxPlane.entity = createEntity("Parallax plane of entity '${entity.id}'") {
                        it += positionComponent {}
                        it += rgbaComponent {}
                    }

                    val planeConfig = inject<AssetStore>("AssetStore").getParallaxPlane(parallaxPlaneName)
                    repeat(planeConfig.lineTextures.size) { parallaxPlane.linePositions.add(0f) }
                    repeat(planeConfig.topAttachedLayerTextures.size) { parallaxPlane.topAttachedLayerPositions.add(0f) }
                    repeat(planeConfig.bottomAttachedLayerTextures.size) { parallaxPlane.bottomAttachedLayerPositions.add(0f) }
                }

                foregroundLayerNames.forEach { layerName ->
                    fgLayerEntities[layerName] =
                        createEntity("Parallax FG layer '$layerName' of entity '${entity.id}'") {
                            it += positionComponent {}
                            it += rgbaComponent {}
                        }
                }
            }

            it += layerTag
        }
        // Get height of the parallax background
        val parallaxConfig = inject<AssetStore>("AssetStore").getParallaxConfig(parallaxAssetName)
        // Set parallax height in the camera system
        system<CameraSystem>().parallaxHeight = parallaxConfig.height.toFloat()

        return entity
    }

    init {
        EntityFactory.register(this)
    }
}
