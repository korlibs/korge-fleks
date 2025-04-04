package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.renderSystems.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.fleks.utils.componentPool.*
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
) : CloneableComponent<PositionComponent>() {
    override fun type(): ComponentType<PositionComponent> = PositionComponent
    companion object : ComponentType<PositionComponent>()

    /**
     * Convert the position of the entity to world coordinates.
     * This is useful to convert the position of a touch position into world coordinates.
     */
    fun World.convertToWorldCoordinates(camera: Entity) {
        val cameraPosition = camera[PositionComponent]
        x += cameraPosition.x - cameraPosition.offsetX - AppConfig.VIEW_PORT_WIDTH_HALF
        y += cameraPosition.y - cameraPosition.offsetY - AppConfig.VIEW_PORT_HEIGHT_HALF
    }

    /**
     * Convert the position of the entity to screen coordinates.
     * This is useful to convert the position of an entity to screen coordinates for rendering.
     */
    fun World.convertToScreenCoordinates(camera: Entity): PositionComponent {
        val cameraPosition = camera[PositionComponent]
        return PositionComponent(
            x = x - cameraPosition.x + cameraPosition.offsetX + AppConfig.VIEW_PORT_WIDTH_HALF,
            y = y - cameraPosition.y + cameraPosition.offsetY + AppConfig.VIEW_PORT_HEIGHT_HALF,
            offsetX = offsetX,
            offsetY = offsetY
        )
    }

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): PositionComponent = this.copy()
}
