package samples.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * This component is used to add position and acceleration to an entity. The data from this
 * component will be processed by the MoveSystem of the Fleks ECS.
 */
data class Position(
    var x: Double = 100.0,
    var y: Double = 100.0,
    var xAcceleration: Double = 0.0,
    var yAcceleration: Double = 0.0,
) : Component<Position> {
    override fun type(): ComponentType<Position> = Position
    companion object : ComponentType<Position>()
}
