package korlibs.korge.fleks.entity.blueprints

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
@Serializable @SerialName("LayeredSpriteBlueprint")
data class LayeredSpriteBlueprint(
    override val name: String,

    private val listOfImages: List<String>,
    private val listOfLayerIndexes: List<Int>,  // layer index per image
    @Serializable(with = RGBAAsInt::class) private val tint: RGBA = Colors.WHITE,
    private val alpha: Float = 1f,
    private val baseLayerIndex: Int = 0,
    private val renderLayerTag: RenderLayerTag,
    private val createEntityPerLayer: Boolean = true, // If true, creates an entity for each layer in the sprite for tween animation
    private val screenCoordinates: Boolean = false
) : EntityBlueprint {

    override fun World.entityConfigure(entity: Entity) : Entity {
        // Base entity
        entity.configure {
            // global position
            it += positionComponent {}
            it += entityRefsByNameComponent {
                moveLinked = true
                deleteLinked = true
            }
            // Add life cycle component because we have list of layer entities which needs to be cleaned up by LifeCycleSystem on deletion
            it += lifeCycleComponent {}
        }

        val entityRefsByNameComponent = entity[EntityRefsByNameComponent]

        listOfImages.forEach { image ->
            val layerEntity = createAndConfigureEntity(entityBlueprint = "${name}_${image}" )
            entityRefsByNameComponent.add(image, layerEntity)
        }

        return entity
    }

    init {
        EntityFactory.register(this)

        // Sanity check
        if (listOfImages.size != listOfLayerIndexes.size)
            println("\nERROR: LayeredSpriteConfig '$name' - Sizes of listOfImages '${listOfImages.size}' " +
                "and listOfLayerIndexes '${listOfLayerIndexes.size}' does not match!")

        listOfImages.forEachIndexed { idx, image ->
            val layerIndex = if (listOfLayerIndexes.size > idx) listOfLayerIndexes[idx] else 0
            SpriteBlueprint(
                name = "${name}_${image}",
                // Specify the layer index together with the image name
                assetName = image,
                layerIndex = baseLayerIndex + layerIndex,
                tint = tint,
                alpha = alpha,
                renderLayerTag = renderLayerTag,
                screenCoordinates = screenCoordinates
            )
        }
    }
}
