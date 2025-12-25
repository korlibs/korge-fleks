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
    // TODO remove below - use layer names as defined in the parallax asset config
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
            }

            it += layerTag
        }

        return entity
    }

    init {
        EntityFactory.register(this)
    }
}
