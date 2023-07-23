package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.entity.config.invalidEntity
import korlibs.korge.fleks.utils.SerializeBase
import korlibs.korge.fleks.systems.KorgeViewSystem
import korlibs.korge.fleks.utils.random
import korlibs.math.interpolation.Easing
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
 * Add this component to an entity together with PositionShape component to randomly change the position withing
 * the specified [xVariance] and [yVariance].
 */
@Serializable @SerialName("AutomaticMoving")
data class AutomaticMoving(
    // trigger variance for start moving: (1.0) - trigger immediately when possible, (0.0) - no trigger for start moving at all
    var triggerVariance: Float = 0f,
    // terminate variance for stop moving: (1.0) - always terminate previous trigger, (0.0) - triggered moving stays forever
    var terminateVariance: Float = 0f,
    var interval: Float = 0f,          // in seconds
    var intervalVariance: Float = 0f,  // in seconds
    var targetX: Float = 0f,
    var targetY: Float = 0f,
    var xVariance: Float = 0f,
    var yVariance: Float = 0f,

    /** Final absolute move values which are applied to the [PositionShape] component's x,y properties of the entity in [KorgeViewSystem] */
    var triggered: Boolean = false,
    var x: Float = 0f,
    var y: Float = 0f,

    // Internal runtime data
    var timeProgress: Float = 0f,
    var waitTime: Float = 0f
) : Component<AutomaticMoving>, SerializeBase {
    override fun type() = AutomaticMoving

    override fun World.onAdd(entity: Entity) {

        timeProgress = 0f
        waitTime = interval + if (intervalVariance != 0f) (-intervalVariance..intervalVariance).random() else 0f

        val startX = x
        val startY = y
        val endX = targetX + if (xVariance != 0f) (-xVariance..xVariance).random() else 0f
        val endY = targetY + if (yVariance != 0f) (-yVariance..yVariance).random() else 0f
        updateAnimateComponent(this, entity, AnimateComponentType.ChangeOffsetRandomlyX, value = startX, change = endX - startX, waitTime, Easing.EASE_IN_OLD)
        updateAnimateComponent(this, entity, AnimateComponentType.ChangeOffsetRandomlyY, value = startY, change = endY - startY, waitTime, Easing.EASE_IN_OUT)

    }

    fun updateAnimateComponent(world: World, entity: Entity, componentProperty: AnimateComponentType, value: Any, change: Any = Unit, duration: Float? = null, easing: Easing? = null) = with (world) {
        entity.configure { animatedEntity ->
            animatedEntity.getOrAdd(componentProperty.type) { AnimateComponent(componentProperty) }.also {
                it.change = change
                it.value = value
                it.duration = duration ?: 0f
                it.timeProgress = 0f
                it.easing = easing ?: Easing.LINEAR
            }
        }
    }

    companion object : ComponentType<AutomaticMoving>()
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
