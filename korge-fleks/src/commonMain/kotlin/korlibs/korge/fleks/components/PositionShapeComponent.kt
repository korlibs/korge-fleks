package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.entity.config.invalidEntity
import korlibs.korge.fleks.systems.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
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

/**
 * Add this component to an entity together with PositionShape component to randomly change the position withing
 * the specified [xVariance] and [yVariance].
 */
@Serializable @SerialName("NoisyMove")
data class NoisyMoveComponent(
    // trigger variance for start moving: (1.0) - trigger immediately when possible, (0.0) - no trigger for start moving at all
    var triggerVariance: Double = 0.0,
    // terminate variance for stop moving: (1.0) - always terminate previous trigger, (0.0) - triggered moving stays forever
    var terminateVariance: Double = 0.0,
    var interval: Double = 0.0,          // in seconds
    var intervalVariance: Double = 0.0,  // in seconds
    var xTarget: Double = 0.0,
    var yTarget: Double = 0.0,
    var xVariance: Double = 0.0,
    var yVariance: Double = 0.0,

    /** Final absolute move values which are applied to the [PositionShapeComponent]'s (x,y) properties of the entity in [KorgeViewSystem] */
    var triggered: Boolean = false,
    var x: Double = 0.0,
    var y: Double = 0.0,

    // Internal runtime data
    var timeProgress: Double = 0.0,
    var waitTime: Double = 0.0
) : Component<NoisyMoveComponent>, SerializeBase {
    override fun type() = NoisyMoveComponent

    override fun World.onAdd(entity: Entity) {

        timeProgress = 0.0
        waitTime = interval + if (intervalVariance != 0.0) (-intervalVariance..intervalVariance).random() else 0.0

        val startX = x
        val startY = y
        val endX = xTarget + if (xVariance != 0.0) (-xVariance..xVariance).random() else 0.0
        val endY = yTarget + if (yVariance != 0.0) (-yVariance..yVariance).random() else 0.0
        updateAnimateComponent(this, entity, TweenProperty.NoisyMoveX, value = startX, change = endX - startX, waitTime, Easing.EASE_IN_OLD)
        updateAnimateComponent(this, entity, TweenProperty.NoisyMoveY, value = startY, change = endY - startY, waitTime, Easing.EASE_IN_OUT)

    }

    fun updateAnimateComponent(world: World, entity: Entity, componentProperty: TweenProperty, value: Any, change: Any = Unit, duration: Double? = null, easing: Easing? = null) = with (world) {
        entity.configure { animatedEntity ->
            animatedEntity.getOrAdd(componentProperty.type) { TweenPropertyComponent(componentProperty) }.also {
                it.change = change
                it.value = value
                it.duration = duration ?: 0.0
                it.timeProgress = 0.0
                it.easing = easing ?: Easing.LINEAR
            }
        }
    }

    companion object : ComponentType<NoisyMoveComponent>()
}
