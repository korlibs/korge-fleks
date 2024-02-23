package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import korlibs.korge.fleks.entity.config.invalidEntity
import korlibs.korge.fleks.utils.SerializeBase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to add generic object properties like position and size to an entity.
 * The data from this component will be processed e.g. by the KorgeViewSystem in the Fleks ECS.
 */
@Serializable
@SerialName("PositionShape")
data class PositionShapeComponent(
    var initialized: Boolean = false,
    var x: Double = 0.0,
    var y: Double = 0.0,
    var width: Double = 0.0,
    var height: Double = 0.0,
    ) : Component<PositionShapeComponent> {
    override fun type(): ComponentType<PositionShapeComponent> = PositionShapeComponent
    companion object : ComponentType<PositionShapeComponent>()
}

@Serializable
@SerialName("Offset")
data class OffsetComponent(
    var x: Double = 0.0,
    var y: Double = 0.0
) : Component<OffsetComponent> {
    override fun type(): ComponentType<OffsetComponent> = OffsetComponent
    companion object : ComponentType<OffsetComponent>()
}

@Serializable
@SerialName("OffsetByFrameIndex")
data class OffsetByFrameIndexComponent(
    var entity: Entity = invalidEntity,
    var list: Map<String, List<Point>> = emptyMap()
) : Component<OffsetByFrameIndexComponent> {
    override fun type(): ComponentType<OffsetByFrameIndexComponent> = OffsetByFrameIndexComponent
    companion object : ComponentType<OffsetByFrameIndexComponent>()
}

@Serializable
@SerialName("PositionShape.Point")
data class Point(
    var x: Double = 0.0,
    var y: Double = 0.0
) : SerializeBase

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
