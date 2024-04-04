package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import kotlinx.serialization.*

@Serializable
@SerialName("Motion")
data class MotionComponent(
    var accelX: Double = 0.0,
    var accelY: Double = 0.0,
    var velocityX: Double = 0.0,
    var velocityY: Double = 0.0
) : Component<MotionComponent> {
    override fun type(): ComponentType<MotionComponent> = MotionComponent
    companion object : ComponentType<MotionComponent>()
}

/* TODO - most likely we do not need this component any more with the new render view system
@Serializable
@SerialName("ParallaxMotion")
data class ParallaxMotionComponent(
    var isScrollingHorizontally: Boolean = true,
    var speedFactor: Double = 1.0,  // TODO put this into assets because it is static and does not change  ????
    var selfSpeedX: Double = 0.0,
    var selfSpeedY: Double = 0.0
) : Component<ParallaxMotionComponent> {
    override fun type(): ComponentType<ParallaxMotionComponent> = ParallaxMotionComponent
    companion object : ComponentType<ParallaxMotionComponent>()
}
*/
