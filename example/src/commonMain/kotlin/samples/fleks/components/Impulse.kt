package samples.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/**
 * This component is used to add an entity the behaviour to "bounce" at collision with the ground.
 */
data class Impulse(
    var xForce: Double = 0.0,  // not used currently
    var yForce: Double = 0.0
) : Component<Impulse> {
    override fun type(): ComponentType<Impulse> = Impulse
    companion object : ComponentType<Impulse>()
}

