package korlibs.korge.fleks.components

import com.github.quillraven.fleks.ComponentType
import korlibs.korge.fleks.utils.componentPool.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This is a very basic definition of a rigid body which does not take rotation into account.
 */
@Serializable @SerialName("Rigidbody")
data class RigidbodyComponent(
    var mass: Float = 0f,      // mass to calculate inertia of the object
    var damping: Float = 0f,   // e.g. air resistance of the object when falling
    var friction: Float = 0f,  // e.g. friction of the object when it moves over surfaces
) : CloneableComponent<RigidbodyComponent>() {
    override fun type(): ComponentType<RigidbodyComponent> = RigidbodyComponent
    companion object : ComponentType<RigidbodyComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): RigidbodyComponent = this.copy()
}
