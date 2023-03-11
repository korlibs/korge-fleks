package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.soywiz.korgeFleks.entity.config.Config
import com.soywiz.korgeFleks.entity.config.noConfig
import com.soywiz.korgeFleks.entity.config.nullEntity
import com.soywiz.korgeFleks.utils.*
import com.soywiz.korma.interpolation.Easing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This component is used to add generic object properties like position and size to an entity.
 * The data from this component will be processed e.g. by the KorgeViewSystem in the Fleks ECS.
 */
@Serializable
@SerialName("PositionShape")
data class PositionShape(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var width: Double = 0.0,
    var height: Double = 0.0,

    // further testing
    var entity: Entity = nullEntity,
    var string: String = "",
    var nullString: String? = null,
    var notNullString: String? = null,
    var boolean: Boolean = false,
    var entities: MutableMap<String, Entity> = mutableMapOf("one" to Entity(2), "two" to Entity(42)),
    var componentProperty: AnimateComponentType = AnimateComponentType.PositionShapeX,
    @Serializable(with = AnySerializer::class)
    var changeDouble: Any = Unit,
    @Serializable(with = AnySerializer::class)
    var changeString: Any = Unit,
    @Serializable(with = AnySerializer::class)
    var changeRgb: Any = Unit,
    @Serializable(with = EasingSerializer::class)
    var easing: Easing? = Easing.LINEAR,
    @Serializable(with = EasingSerializer::class)
    var nullEasing: Easing? = null,
    var nonConfig: Config = noConfig,
    var config: Config = noConfig,
    @Serializable(InvokableSerializer::class)
    var lambdaFunction: Invokable = World::noFunction

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
data class Point(var x: Double = 0.0, var y: Double = 0.0)

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
