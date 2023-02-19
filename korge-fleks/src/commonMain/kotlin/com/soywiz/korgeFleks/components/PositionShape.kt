package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.soywiz.korgeFleks.entity.config.nullEntity
import com.soywiz.korgeFleks.utils.*
import com.soywiz.korgeFleks.korlibsAdaptation.Json.CustomSerializer
import com.soywiz.korma.geom.Point


/**
 * This component is used to add generic object properties like position and size to an entity.
 * The data from this component will be processed e.g. by the KorgeViewSystem in the Fleks ECS.
 */
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

) : Component<PositionShape>, SerializeFleksComponent {
    override fun type(): ComponentType<PositionShape> = PositionShape
    companion object : ComponentType<PositionShape>()

    /**
     * Function to get properties via deserialization.
     * Unfortunately types of properties cannot be mixed within one decodeComponent call.
     */
    override fun decodeFromJson(v: FleksJsonComponent) {
        v.decodeComponent(::x, ::y, ::width, ::height)
        v.decodeEntityComponent(::entity)
        v.decodeComponent(::string)
        v.decodeComponent(::nullString, ::notNullString)
        v.decodeEntityMapComponent(::entities)
        v.decodeComponent(::boolean)
    }

    /**
     * Function which is used to serialize properties of a component.
     * Add all properties which shall be serialized.
     */
    override fun encodeToJson(b: StringBuilder) =
        encodeComponent(b, ::x, ::y, ::width, ::height, ::entity, ::string, ::nullString, ::boolean, ::notNullString, ::entities)
}

//fun CustomSerializer.encodeString(value: String?): String? = if (value == null) null else """"$value""""

data class Offset(
    var x: Double = 0.0,
    var y: Double = 0.0
) : Component<Offset>, CustomSerializer {
    override fun type(): ComponentType<Offset> = Offset
    companion object : ComponentType<Offset>()

    override fun encodeToJson(b: StringBuilder) {
        b.append("""{"Offset":{"x":$x,"y":$y}}""")
    }
}

data class OffsetByFrameIndex(
    var entity: Entity = nullEntity,
    var list: Map<String, List<Point>> = emptyMap()
) : Component<OffsetByFrameIndex>, CustomSerializer {
    override fun type(): ComponentType<OffsetByFrameIndex> = OffsetByFrameIndex
    companion object : ComponentType<OffsetByFrameIndex>()

    override fun encodeToJson(b: StringBuilder) { b.append(this) }
}


data class Motion(
    var accelX: Double = 0.0,
    var accelY: Double = 0.0,
    var velocityX: Double = 0.0,
    var velocityY: Double = 0.0
) : Component<Motion>, CustomSerializer {
    override fun type(): ComponentType<Motion> = Motion
    companion object : ComponentType<Motion>()

    override fun encodeToJson(b: StringBuilder) { b.append(this) }
}
