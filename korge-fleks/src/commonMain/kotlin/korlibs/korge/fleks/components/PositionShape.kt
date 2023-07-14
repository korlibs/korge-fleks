package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import korlibs.korge.fleks.entity.config.invalidEntity
import korlibs.korge.fleks.utils.SerializeBase
import korlibs.korge.fleks.systems.KorgeViewSystem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to add generic object properties like position and size to an entity.
 * The data from this component will be processed e.g. by the KorgeViewSystem in the Fleks ECS.
 */
@Serializable @SerialName("PositionShape")
data class PositionShape(
    var initialized: Boolean = false,
    var x: Float = 0.0f,
    var y: Float = 0.0f,
    var width: Float = 0.0f,
    var height: Float = 0.0f,
    ) : Component<PositionShape>, SerializeBase {
    override fun type() = PositionShape
    companion object : ComponentType<PositionShape>()
}

@Serializable @SerialName("Offset")
data class Offset(
    var x: Float = 0.0f,
    var y: Float = 0.0f
) : Component<Offset>, SerializeBase {
    override fun type() = Offset
    companion object : ComponentType<Offset>()
}

@Serializable @SerialName("OffsetByFrameIndex")
data class OffsetByFrameIndex(
    var entity: Entity = invalidEntity,
    var list: Map<String, List<Point>> = emptyMap()
) : Component<OffsetByFrameIndex>, SerializeBase {
    override fun type() = OffsetByFrameIndex
    companion object : ComponentType<OffsetByFrameIndex>()
}

/**
 * Add this component to an entity together with Offset component to randomly change the offset withing
 * the specified [offsetXRange] and [offsetYRange].
 */
@Serializable @SerialName("ChangeOffsetRandomly")
data class ChangeOffsetRandomly(
    // variance in triggering offset change: (1.0) - every frame triggering possible, (0.0) - no triggering at all
    var triggerChangeVariance: Float = 0.0f,
    // variance in triggering back to original offset: (1.0) - triggered offset change switches back immediately, (0.0) - triggered offset change stays forever
    var triggerBackVariance: Float = 0.0f,
    var offsetXRange: Float = 0f,
    var offsetYRange: Float = 0f,

    /** Final offset values which are applied to the [Offset] component values of the entity in [KorgeViewSystem] */
    var triggered: Boolean = false,
    var x: Float = 0f,
    var y: Float = 0f
) : Component<ChangeOffsetRandomly>, SerializeBase {
    override fun type() = ChangeOffsetRandomly
    companion object : ComponentType<ChangeOffsetRandomly>()
}

@Serializable @SerialName("PositionShape.Point")
data class Point(
    var x: Float = 0.0f,
    var y: Float = 0.0f
) : SerializeBase

@Serializable @SerialName("Motion")
data class Motion(
    var accelX: Float = 0.0f,
    var accelY: Float = 0.0f,
    var velocityX: Float = 0.0f,
    var velocityY: Float = 0.0f
) : Component<Motion>, SerializeBase {
    override fun type() = Motion
    companion object : ComponentType<Motion>()
}

@Serializable @SerialName("ParallaxMotion")
data class ParallaxMotion(
    var isScrollingHorizontally: Boolean = true,
    var speedFactor: Float = 1.0f,  // TODO put this into assets because it is static and does not change  ????
    var selfSpeedX: Float = 0.0f,
    var selfSpeedY: Float = 0.0f
) : Component<ParallaxMotion>, SerializeBase {
    override fun type() = ParallaxMotion
    companion object : ComponentType<ParallaxMotion>()
}
