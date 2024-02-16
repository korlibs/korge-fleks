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
    var x: Float = 0.0f,
    var y: Float = 0.0f,
    var width: Float = 0.0f,
    var height: Float = 0.0f,
    ) : Component<PositionShapeComponent>, SerializeBase {
    override fun type(): ComponentType<PositionShapeComponent> = PositionShapeComponent
    companion object : ComponentType<PositionShapeComponent>()
}

@Serializable
@SerialName("Offset")
data class Offset(
    var x: Float = 0.0f,
    var y: Float = 0.0f
) : Component<Offset>, SerializeBase {
    override fun type(): ComponentType<Offset> = Offset
    companion object : ComponentType<Offset>()
}

@Serializable
@SerialName("OffsetByFrameIndex")
data class OffsetByFrameIndex(
    var entity: Entity = invalidEntity,
    var list: Map<String, List<Point>> = emptyMap()
) : Component<OffsetByFrameIndex>, SerializeBase {
    override fun type(): ComponentType<OffsetByFrameIndex> = OffsetByFrameIndex
    companion object : ComponentType<OffsetByFrameIndex>()
}

@Serializable
@SerialName("PositionShape.Point")
data class Point(
    var x: Float = 0.0f,
    var y: Float = 0.0f
) : SerializeBase

@Serializable
@SerialName("Motion")
data class Motion(
    var accelX: Float = 0.0f,
    var accelY: Float = 0.0f,
    var velocityX: Float = 0.0f,
    var velocityY: Float = 0.0f
) : Component<Motion>, SerializeBase {
    override fun type(): ComponentType<Motion> = Motion
    companion object : ComponentType<Motion>()
}

@Serializable
@SerialName("ParallaxMotion")
data class ParallaxMotion(
    var isScrollingHorizontally: Boolean = true,
    var speedFactor: Float = 1.0f,  // TODO put this into assets because it is static and does not change  ????
    var selfSpeedX: Float = 0.0f,
    var selfSpeedY: Float = 0.0f
) : Component<ParallaxMotion>, SerializeBase {
    override fun type(): ComponentType<ParallaxMotion> = ParallaxMotion
    companion object : ComponentType<ParallaxMotion>()
}
