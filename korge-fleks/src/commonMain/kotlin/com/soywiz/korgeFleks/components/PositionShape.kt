package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.soywiz.korgeFleks.entity.config.nullEntity
import com.soywiz.korma.geom.Point

/**
 * This component is used to add generic object properties like position and size to an entity.
 * The data from this component will be processed e.g. by the KorgeViewSystem in the Fleks ECS.
 */
data class PositionShape(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var width: Double = 0.0,
    var height: Double = 0.0
) : Component<PositionShape> {
    override fun type(): ComponentType<PositionShape> = PositionShape
    companion object : ComponentType<PositionShape>()
}

data class Offset(
    var x: Double = 0.0,
    var y: Double = 0.0
) : Component<Offset> {
    override fun type(): ComponentType<Offset> = Offset
    companion object : ComponentType<Offset>()
}

data class OffsetByFrameIndex(
    var entity: Entity = nullEntity,
    var list: Map<String, List<Point>> = emptyMap()
) : Component<OffsetByFrameIndex> {
    override fun type(): ComponentType<OffsetByFrameIndex> = OffsetByFrameIndex
    companion object : ComponentType<OffsetByFrameIndex>()
}


data class Motion(
    var accelX: Double = 0.0,
    var accelY: Double = 0.0,
    var velocityX: Double = 0.0,
    var velocityY: Double = 0.0
) : Component<Motion> {
    override fun type(): ComponentType<Motion> = Motion
    companion object : ComponentType<Motion>()
}
