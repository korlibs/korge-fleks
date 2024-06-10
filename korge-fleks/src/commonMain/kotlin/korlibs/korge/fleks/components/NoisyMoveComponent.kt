package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.systems.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.fleks.components.TweenPropertyComponent.*
import korlibs.math.interpolation.*
import kotlinx.serialization.*


// TODO check if we can implement this as Tween animation for a specific property (x or y)

/**
 * Add this component to an entity together with PositionShape component to randomly change the position withing
 * the specified [xVariance] and [yVariance].
 */
@Serializable
@SerialName("NoisyMove")
data class NoisyMoveComponent(
    // trigger variance for start moving: (1.0) - trigger immediately when possible, (0.0) - no trigger for start moving at all
    var triggerVariance: Float = 0f,
    // terminate variance for stop moving: (1.0) - always terminate previous trigger, (0.0) - triggered moving stays forever
    var terminateVariance: Float = 0f,
    var interval: Float = 0f,          // in seconds
    var intervalVariance: Float = 0f,  // in seconds
    var xTarget: Float = 0f,
    var yTarget: Float = 0f,
    var xVariance: Float = 0f,
    var yVariance: Float = 0f,

    /** Final absolute move values which are applied to the [PositionComponent]'s (x,y) properties of the entity */
    var triggered: Boolean = false,
    var x: Float = 0f,
    var y: Float = 0f,

    // Internal runtime data
    var timeProgress: Float = 0f,
    var waitTime: Float = 0f,

    // internal
    var initialized: Boolean = false
) : Component<NoisyMoveComponent> {
    override fun type() = NoisyMoveComponent

    override fun World.onAdd(entity: Entity) {
        // Make sure that initialization is skipped on world snapshot loading (deserialization of save game)
        if (initialized) return
        else initialized = true

        timeProgress = 0f
        waitTime = interval + if (intervalVariance != 0f) (-intervalVariance..intervalVariance).random() else 0f

        val startX = x
        val startY = y
        val endX = xTarget + if (xVariance != 0f) (-xVariance..xVariance).random() else 0f
        val endY = yTarget + if (yVariance != 0f) (-yVariance..yVariance).random() else 0f
// TODO        updateAnimateComponent(this, entity, TweenProperty.NoisyMoveX, value = startX, change = endX - startX, waitTime, Easing.EASE_IN_OLD)
//        updateAnimateComponent(this, entity, TweenProperty.NoisyMoveY, value = startY, change = endY - startY, waitTime, Easing.EASE_IN_OUT)

    }

    fun updateAnimateComponent(world: World, entity: Entity, componentProperty: TweenProperty, value: Any, change: Any = Unit, duration: Float? = null, easing: Easing? = null) = with (world) {
        entity.configure { animatedEntity ->
            animatedEntity.getOrAdd(componentProperty.type) { TweenPropertyComponent(componentProperty) }.also {
                it.change = change
                it.value = value
                it.duration = duration ?: 0f
                it.timeProgress = 0f
                it.easing = easing ?: Easing.LINEAR
            }
        }
    }

    companion object : ComponentType<NoisyMoveComponent>()

    // Hint to myself: Check if deep copy is needed on any change in the component!
    fun clone() : NoisyMoveComponent = this.copy()
}
