package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.entity.config.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.Easing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component holds all needed details to animate an entity's component.
 */
@Serializable
@SerialName("AnimationScript")
data class AnimationScript(
    var tweens: List<TweenBase> = listOf(),

    // Internal runtime data
    var index: Int = 0,            // This points to the animation step which is currently in progress
    var timeProgress: Float = 0f,  // Elapsed time for the object to be animated
    var waitTime: Float = 0f,
    var active: Boolean = false,
    var initialized: Boolean = false
) : Component<AnimationScript>, SerializeBase {
    override fun type(): ComponentType<AnimationScript> = AnimationScript

    companion object : ComponentType<AnimationScript>() {
        /**
         * Initialize internal waitTime property with delay value of first tweens if available.
         */
        val onComponentAdded: ComponentHook<AnimationScript> = { _, component ->
            if (!component.initialized) {
                if (component.tweens.isNotEmpty()) component.waitTime = component.tweens[component.index].delay ?: 0f
                component.initialized = true
            }
        }
        val onComponentRemoved: ComponentHook<AnimationScript> = { _, _ -> /* not used here */ }
    }
}


interface TweenBase {
    var entity: Entity
    var delay: Float?
    var duration: Float?
    var easing: Easing?
}

/**
 * Animation Component data classes based on TweenBase
 */
@Serializable
@SerialName("AnimationScript.TweenSequence")
data class TweenSequence(
    val tweens: List<TweenBase> = listOf(),   // tween objects which contain entity and its properties to be animated in sequence

    override var entity: Entity = nullEntity, // not used
    override var delay: Float? = null,        // following 3 properties not used here
    override var duration: Float? = null,
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = null
) : TweenBase

@Serializable
@SerialName("AnimationScript.ParallelTweens")
data class ParallelTweens(
    val tweens: List<TweenBase> = listOf(),           // tween objects which contain entity and its properties to be animated in parallel

    override var entity: Entity = nullEntity,         // not used here
    override var delay: Float? = 0f,                  // in seconds
    override var duration: Float? = 0f,               // in seconds
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = Easing.LINEAR      // function to change the properties
) : TweenBase

@Serializable
@SerialName("AnimationScript.Wait")
data class Wait(
    override var entity: Entity = nullEntity, // not used
    override var delay: Float? = null,   // Not used
    override var duration: Float? = 0f,
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = null  // not used
) : TweenBase


/**
 * Animation Component data classes based on TweenBaseHasEntity
 */
@Serializable
@SerialName("AnimationScript.DeleteEntity")
data class DeleteEntity(
    val healthCounter: Int = 0,            // set healthCounter to zero to delete the entity immediately

    override var entity: Entity,
    override var delay: Float? = null,   // not used
    override var duration: Float? = 0f,  // not used - 0f for immediately
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = null  // not used
) : TweenBase

@Serializable
@SerialName("AnimationScript.SpawnEntity")
data class SpawnEntity(
    var entityName: String,
    @Serializable(InvokableSerializer::class)
    var configureFunction: Invokable = World::noFunction, // name of function which configures the spawned entity
    var x: Float = 0.0f,                   // position where entity will be spawned
    var y: Float = 0.0f,

    override var entity: Entity = nullEntity,
    override var delay: Float? = null,
    override var duration: Float? = 0f,  // not used - 0f for immediately
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = null  // not used
) : TweenBase

// Following component classes are for animating specific components
@Serializable
@SerialName("AnimationScript.TweenAppearance")
data class TweenAppearance(
    val alpha: Float? = null,
    val tint: Rgb? = null,
    val visible: Boolean? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = null
) : TweenBase

@Serializable
@SerialName("AnimationScript.TweenPositionShape")
data class TweenPositionShape(
    val x: Float? = null,
    val y: Float? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = null
) : TweenBase

@Serializable
@SerialName("AnimationScript.TweenOffset")
data class TweenOffset(
    val x: Float? = null,
    val y: Float? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = null
) : TweenBase

@Serializable
@SerialName("AnimationScript.TweenLayout")
data class TweenLayout(
    val centerX: Boolean? = null,
    val centerY: Boolean? = null,
    val offsetX: Float? = null,
    val offsetY: Float? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = null
) : TweenBase

@Serializable
@SerialName("AnimationScript.TweenSprite")
data class TweenSprite(
    var animationName: String? = null,
    var isPlaying: Boolean? = null,
    var forwardDirection: Boolean? = null,
    var loop: Boolean? = null,
    var destroyOnPlayingFinished: Boolean? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = null
) : TweenBase

@Serializable
@SerialName("AnimationScript.TweenSwitchLayerVisibility")
data class TweenSwitchLayerVisibility(
    var offVariance: Float? = null,
    var onVariance: Float? = null,
    var spriteLayers: List<String>? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = null
) : TweenBase

@Serializable
@SerialName("AnimationScript.TweenSpawner")
data class TweenSpawner(
    var numberOfObjects: Int? = null,
    var interval: Int? = null,
    var timeVariation: Int? = null,
    var positionVariation: Float? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = null
) : TweenBase

@Serializable
@SerialName("AnimationScript.TweenSound")
data class TweenSound(
    var startTrigger: Boolean? = null,
    var stopTrigger: Boolean? = null,
    var position: Float? = null,
    var volume: Float? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = null
) : TweenBase
