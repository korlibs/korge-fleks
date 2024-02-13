package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.entity.config.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.Easing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component holds all needed details to animate properties of components of entities.
 */
@Serializable @SerialName("TweenScript")
data class TweenScript(
    var tweenSequence: List<TweenBase> = listOf(),

    // Internal runtime data
    var index: Int = 0,            // This points to the animation step which is currently in progress
    var timeProgress: Float = 0f,  // Elapsed time for the object to be animated
    var waitTime: Float = 0f,
    var active: Boolean = false,
    var initialized: Boolean = false
) : Component<TweenScript>, SerializeBase {
    override fun type(): ComponentType<TweenScript> = TweenScript

    /**
     * Initialize internal waitTime property with delay value of first tweens if available.
     */
    override fun World.onAdd(entity: Entity) {
        if (!initialized) {
            if (tweenSequence.isNotEmpty()) waitTime = tweenSequence[index].delay ?: 0f
            initialized = true
        }
    }

    override fun World.onRemove(entity: Entity) { /* not used here */ }

    companion object : ComponentType<TweenScript>()
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
@Serializable @SerialName("TweenScript.Sequence")
data class Sequence(
    val tweens: List<TweenBase> = listOf(),   // tween objects which contain entity and its properties to be animated in sequence

    override var entity: Entity = invalidEntity, // not used
    override var delay: Float? = null,        // following 3 properties not used here
    override var duration: Float? = null,
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = null
) : TweenBase

@Serializable @SerialName("TweenScript.Parallel")
data class Parallel(
    val tweens: List<TweenBase> = listOf(),           // tween objects which contain entity and its properties to be animated in parallel

    override var entity: Entity = invalidEntity,         // not used here
    override var delay: Float? = 0f,                  // in seconds
    override var duration: Float? = 0f,               // in seconds
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = Easing.LINEAR      // function to change the properties
) : TweenBase

@Serializable @SerialName("TweenScript.Wait")
data class Wait(
    override var entity: Entity = invalidEntity, // not used
    override var delay: Float? = null,   // Not used
    override var duration: Float? = 0f,
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = null  // not used
) : TweenBase


/**
 * Animation Component data classes based on TweenBaseHasEntity
 */
@Serializable @SerialName("TweenScript.DeleteEntity")
data class DeleteEntity(
    val healthCounter: Int = 0,            // set healthCounter to zero to delete the entity immediately

    override var entity: Entity,
    override var delay: Float? = null,   // not used
    override var duration: Float? = 0f,  // not used - 0f for immediately
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = null  // not used
) : TweenBase

@Serializable @SerialName("TweenScript.SpawnEntity")
data class SpawnEntity(
    var config: Identifier,             // name of config for configuring spawned entity
    var function: Identifier,           // name of function which configures the spawned entity
    var x: Float = 0.0f,                 // position where entity will be spawned
    var y: Float = 0.0f,

    override var entity: Entity = invalidEntity, // when entity is not given (=nullEntity) than it will be created
    override var delay: Float? = null,
    override var duration: Float? = 0f,  // not used - 0f for immediately
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = null  // not used
) : TweenBase

@Serializable @SerialName("TweenScript.ExecuteConfigFunction")
data class ExecuteConfigFunction(
    var config: Identifier,              // name of config for configuring spawned entity
    var function: Identifier,            // name of function which configures the spawned entity

    override var entity: Entity,          // entity needs to be provided in any case
    override var delay: Float? = null,    // not used
    override var duration: Float? = null, // not used
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = null   // not used
) : TweenBase

// Following component classes are for triggering tweens on specific properties of components
@Serializable @SerialName("TweenScript.TweenAppearance")
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

@Serializable @SerialName("TweenScript.TweenPositionShape")
data class TweenPositionShape(
    val x: Float? = null,
    val y: Float? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = null
) : TweenBase

@Serializable @SerialName("TweenScript.TweenOffset")
data class TweenOffset(
    val x: Float? = null,
    val y: Float? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingSerializer::class)
    override var easing: Easing? = null
) : TweenBase

@Serializable @SerialName("TweenScript.TweenLayout")
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

@Serializable @SerialName("TweenScript.TweenSprite")
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

@Serializable @SerialName("TweenScript.TweenSwitchLayerVisibility")
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

@Serializable @SerialName("TweenScript.TweenSpawner")
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

@Serializable @SerialName("TweenScript.TweenSound")
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
