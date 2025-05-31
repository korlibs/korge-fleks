package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.CloneableComponent
import kotlinx.serialization.*


/**
 * A component to define movement for an entity.
 *
 * @param velocityX in "world units" per delta time
 * @param velocityY in "world units" per delta time
 */
@Serializable @SerialName("Motion")
data class MotionComponent(
    var accelX: Float = 0f,
    var accelY: Float = 0f,

    var velocityX: Float = 0f,
    var velocityY: Float = 0f,
    var velocityZ: Float = 0f,
    var frictionX: Float = 0.82f,
    var frictionY: Float = 0.82f,
    var frictionZ: Float = 0f
) : CloneableComponent<MotionComponent>() {
    override fun type(): ComponentType<MotionComponent> = MotionComponent
    companion object : ComponentType<MotionComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): MotionComponent = this.copy()
}
