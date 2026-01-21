package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.*
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.fleks.components.EntityRefsByName.Companion.EntityRefsByNameComponent
import korlibs.korge.fleks.components.EntityRefsByName.Companion.entityRefsByNameComponent
import korlibs.korge.fleks.components.LifeCycle.Companion.lifeCycleComponent
import korlibs.korge.fleks.components.Position.Companion.positionComponent
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


/**
 * Config for creation of a logo entity which can consist of multiple layers.
 * Each layer can be controlled independently e.g. to fade-in or move-in parts
 * of the logo more dynamically.
 *
 * Logo can be centered on the screen and additionally an offset can be specified.
 */
// TODO rename to SpriteWithLayersConfig or something similar?
@Serializable @SerialName("LogoEntityConfig")
data class LayeredSpriteConfig(
    override val name: String,

    private val listOfImages: List<String>,
    private val width: Float,
    private val height: Float,
    private val centerX: Boolean = false,
    private val centerY: Boolean = false,
    private val offsetX: Float = 0f,
    private val offsetY: Float = 0f,
    @Serializable(with = RGBAAsInt::class) private val tint: RGBA = Colors.WHITE,
    private val alpha: Float = 1f,
    private val layerIndex: Int,
    private val renderLayerTag: RenderLayerTag,
    private val createEntityPerLayer: Boolean = true // If true, creates an entity for each layer in the sprite for tween animation

) : EntityConfig {

    override fun World.entityConfigure(entity: Entity) : Entity {
        // Base entity
        entity.configure {
            // global position
            it += positionComponent {
                x = this@LayeredSpriteConfig.offsetX + (if (centerX) (AppConfig.VIEW_PORT_WIDTH - width) * 0.5f else 0f)
                y = this@LayeredSpriteConfig.offsetY + (if (centerY) (AppConfig.VIEW_PORT_HEIGHT - height) * 0.5f else 0f)
            }

            it += entityRefsByNameComponent {
                moveLinked = true
                deleteLinked = true
            }
            // Add life cycle component because we have list of layer entities which needs to be cleaned up by LifeCycleSystem on deletion
            it += lifeCycleComponent {}
        }

        val entityRefsByNameComponent = entity[EntityRefsByNameComponent]

        listOfImages.forEach { image ->
            val layerEntity = createAndConfigureEntity(entityConfig = "generic_sprite_$image" )
            entityRefsByNameComponent.add(image, layerEntity)
        }

        return entity
    }

    init {
        EntityFactory.register(this)

        listOfImages.forEach { image ->
            SpriteConfig(
                name = "generic_sprite_$image",
                assetName = image,
                renderLayerTag = renderLayerTag
            )
        }
    }
}
