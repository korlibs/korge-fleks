package korlibs.korge.fleks.entity.blueprints

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.Motion.Companion.motionComponent
import korlibs.korge.fleks.components.Parallax.Companion.parallaxComponent
import korlibs.korge.fleks.components.Position.Companion.positionComponent
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.entity.*
import kotlinx.serialization.*


@Serializable @SerialName("ParallaxEffectBlueprint")
data class ParallaxEffectBlueprint(
    override val name: String,

    private val parallaxAssetName: String,

    private val layerTag: RenderLayerTag,
    private val x: Float = 0f,
    private val y: Float = 0f
) : EntityBlueprint {

    override fun World.entityConfigure(entity: Entity) : Entity {
        entity.configure {
            // Gobal position of parallax background in screen coordinates
            it += positionComponent {
                x = this@ParallaxEffectBlueprint.x  // global position in screen coordinates
                y = this@ParallaxEffectBlueprint.y
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
