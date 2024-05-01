package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import korlibs.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This component adds the control-specific-layer aspect to the entity.
 * I.e. when this component is added to an entity than that entity will control e.g. [AppearanceComponent], [PositionComponent] or
 * [InputTouchButtonComponent] aspects of a specific layer of a sprite.
 *
 * Hint: Usually [PositionComponent] and [OffsetComponent] are also added to that entity in order to change the layer
 * position relatively to the [SpriteComponent] position or pivot point.
 *
 * @param [spriteLayer] has to be set to the same layer name as in Aseprite to select that layer.
 * @param [parentEntity] is the entity (ID) which defines [SpriteComponent] data.
 * @param [parallaxPlaneLine] is the index in the array of lines for the parallaxPlane (the pseudo 3D parallax effect)
 *
 * Hint: Either [spriteLayer] or [parallaxPlaneLine] needs to be specified.
 */
@Serializable
@SerialName("SpecificLayer")
data class SpecificLayerComponent(
    var parentEntity: Entity = Entity.NONE,  // The entity which contains the sprite data with layers (ImageAnimView)
    var spriteLayer: String? = null,
    var parallaxPlaneLine: Int? = null
) : Component<SpecificLayerComponent> {
    override fun type() = SpecificLayerComponent
    companion object : ComponentType<SpecificLayerComponent>()
}
