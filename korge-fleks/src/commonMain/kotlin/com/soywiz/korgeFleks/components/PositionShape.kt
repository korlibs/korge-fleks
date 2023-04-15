package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.soywiz.korgeFleks.entity.config.nullEntity
import com.soywiz.korgeFleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This component is used to add generic object properties like position and size to an entity.
 * The data from this component will be processed e.g. by the KorgeViewSystem in the Fleks ECS.
 */
@Serializable
@SerialName("PositionShape")
data class PositionShape(
    var initialized: Boolean = false,
    var x: Double = 0.0,
    var y: Double = 0.0,
    var width: Double = 0.0,
    var height: Double = 0.0,
    ) : Component<PositionShape>, SerializeBase {
    override fun type(): ComponentType<PositionShape> = PositionShape
    companion object : ComponentType<PositionShape>()
}

@Serializable
@SerialName("Offset")
data class Offset(
    var x: Double = 0.0,
    var y: Double = 0.0
) : Component<Offset>, SerializeBase {
    override fun type(): ComponentType<Offset> = Offset
    companion object : ComponentType<Offset>()
}

@Serializable
@SerialName("OffsetByFrameIndex")
data class OffsetByFrameIndex(
    var entity: Entity = nullEntity,
    var list: Map<String, List<Point>> = emptyMap()
) : Component<OffsetByFrameIndex>, SerializeBase {
    override fun type(): ComponentType<OffsetByFrameIndex> = OffsetByFrameIndex
    companion object : ComponentType<OffsetByFrameIndex>()
}

@Serializable
@SerialName("PositionShape.Point")
data class Point(var x: Double = 0.0, var y: Double = 0.0) : SerializeBase

@Serializable
@SerialName("Motion")
data class Motion(
    var accelX: Double = 0.0,
    var accelY: Double = 0.0,
    var velocityX: Double = 0.0,
    var velocityY: Double = 0.0
) : Component<Motion>, SerializeBase {
    override fun type(): ComponentType<Motion> = Motion
    companion object : ComponentType<Motion>()
}

@Serializable
@SerialName("ParallaxMotion")
data class ParallaxMotion(
    var isScrollingHorizontally: Boolean = true,
    var speedFactor: Double = 1.0,  // TODO put this into assets because it is static and does not change  ????
//    val speedX: Double = 0.0,
//    val speedY: Double = 0.0,
    val selfSpeedX: Double = 0.0,
    val selfSpeedY: Double = 0.0
) : Component<ParallaxMotion>, SerializeBase {
    override fun type(): ComponentType<ParallaxMotion> = ParallaxMotion
    companion object : ComponentType<ParallaxMotion>()
}
