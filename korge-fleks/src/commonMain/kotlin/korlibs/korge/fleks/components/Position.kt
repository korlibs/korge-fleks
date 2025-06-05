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
 *                [cleanup] function and initialize them in the [init] function.
 */
@Serializable @SerialName("Position")
class Position private constructor(
    var x: Float = 0f,
    var y: Float = 0f,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f
) : PoolableComponent<Position>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: Position) {
        x = from.x
        y = from.y
        offsetX = from.offsetX
        offsetY = from.offsetY
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        x = 0f
        y = 0f
        offsetX = 0f
        offsetY = 0f
    }

    override fun type() = PositionComponent

    companion object {
        val PositionComponent = componentTypeOf<Position>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticPositionComponent(config: Position.() -> Unit ): Position =
        Position().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun positionComponent(config: Position.() -> Unit ): Position =
        pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE) { Position() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): Position = positionComponent { init(from = this@Position ) }

    // Initialize the component automatically when it is added to an entity
    //override fun World.initComponent(entity: Entity) {}

    // Cleanup/Reset the component automatically when it is removed from an entity (component will be returned to the pool eventually)
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }

    // Initialize an external prefab when the component is added to an entity
    //override fun World.initPrefabs(entity: Entity) {}

    // Cleanup/Reset an external prefab when the component is removed from an entity
    //override fun World.cleanupPrefabs(entity: Entity) {}

    // Free the component and return it to the pool - this is called directly by the SnapshotSerializerSystem
    override fun free() {
        cleanup()
        pool.free(this)
    }

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
