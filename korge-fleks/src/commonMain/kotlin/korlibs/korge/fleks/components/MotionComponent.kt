package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*

/**
 * A component to define movement for an entity.
 *
 * @param velocityX pixel/second (?)
 */
@Serializable @SerialName("Motion")
data class MotionComponent(
    var accelX: Float = 0f,
    var accelY: Float = 0f,
    var velocityX: Float = 0f,
    var velocityY: Float = 0f
) : CloneableComponent<MotionComponent>() {
    override fun type(): ComponentType<MotionComponent> = MotionComponent
    companion object : ComponentType<MotionComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): MotionComponent = this.copy()
}
