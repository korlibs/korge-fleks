package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.systems.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to add generic object properties like position to an entity.
 * The data from this component will be processed e.g. by the SpriteRenderView in Korge-fleks.
 */
@Serializable
@SerialName("Position")
data class PositionComponent(
    var x: Float = 0f,
    var y: Float = 0f,
) : Component<PositionComponent> {
    override fun type(): ComponentType<PositionComponent> = PositionComponent
    companion object : ComponentType<PositionComponent>()
}
