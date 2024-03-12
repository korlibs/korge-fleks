package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This is a very basic definition of a rigid body which does not take rotation into account.
 */
@Serializable
@SerialName("Rigidbody")
data class RigidbodyComponent(
    var mass: Double = 0.0,      // mass to calculate inertia of the object
    var damping: Double = 0.0,   // e.g. air resistance of the object when falling
    var friction: Double = 0.0,  // e.g. friction of the object when it moves over surfaces
) : Component<RigidbodyComponent> {
    override fun type(): ComponentType<RigidbodyComponent> = RigidbodyComponent
    companion object : ComponentType<RigidbodyComponent>()
}
