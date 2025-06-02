package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to add position related properties to an entity.
 * The data from this component will be processed e.g. by the [ObjectRenderSystem] in Korge-fleks.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("Position")
class Position private constructor(
    var x: Float = 0f,
    var y: Float = 0f,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f
) : Poolable<Position>() {
    fun init(from: Position) {
        x = from.x
        y = from.y
        offsetX = from.offsetX
        offsetY = from.offsetY
    }

    fun cleanup() {
        x = 0f
        y = 0f
        offsetX = 0f
        offsetY = 0f
    }

    override fun type() = PositionComponent

    companion object {
        val PositionComponent = componentTypeOf<Position>()

        fun World.positionComponent(config: Position.() -> Unit ): Position =
            getPoolable(PositionComponent).apply(config)

        // Use this function to create a new instance as static property (val)
        fun staticPositionComponent(): Position = Position()

        fun InjectableConfiguration.addPositionComponentPool(preAllocate: Int = 0) {
            addPool(PositionComponent, preAllocate) { Position() }
        }
    }

    override fun World.clone(): Position =
        getPoolable(PositionComponent).apply { init(from = this@Position) }

    override fun World.cleanupComponent(entity: Entity) { cleanup() }

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
    fun World.convertToScreenCoordinates(camera: Entity): Position {
        val cameraPosition = camera[PositionComponent]
        x = x - cameraPosition.x + cameraPosition.offsetX + AppConfig.VIEW_PORT_WIDTH_HALF
        y = y - cameraPosition.y + cameraPosition.offsetY + AppConfig.VIEW_PORT_HEIGHT_HALF
        return this@Position
    }
}
