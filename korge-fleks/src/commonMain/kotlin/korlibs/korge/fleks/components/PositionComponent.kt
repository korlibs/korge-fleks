package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.renderSystems.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to add position related properties to an entity.
 * The data from this component will be processed e.g. by the [ObjectRenderSystem] in Korge-fleks.
 */
@Serializable @SerialName("Position")
data class PositionComponent(
    var x: Float = 0f,
    var y: Float = 0f,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f
) : Component<PositionComponent> {
    override fun type(): ComponentType<PositionComponent> = PositionComponent
    companion object : ComponentType<PositionComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    fun clone() : PositionComponent = this.copy()
}
