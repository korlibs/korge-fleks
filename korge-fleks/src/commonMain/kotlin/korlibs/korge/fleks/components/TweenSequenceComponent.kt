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
@Serializable @SerialName("TweenSequence")
data class TweenSequenceComponent(
    var tweens: List<TweenBase> = listOf(),

    // Internal runtime data
    var index: Int = 0,              // This points to the animation step which is currently in progress
    var timeProgress: Double = 0.0,  // Elapsed time for the object to be animated
    var waitTime: Double = 0.0,
    var executed: Boolean = false,
    var initialized: Boolean = false
) : Component<TweenSequenceComponent> {
    override fun type(): ComponentType<TweenSequenceComponent> = TweenSequenceComponent

    /**
     * Initialize internal waitTime property with delay value of first tweens if available.
     */
    override fun World.onAdd(entity: Entity) {
        if (!initialized) {
            if (tweens.isNotEmpty()) waitTime = tweens[index].delay ?: 0.0
            initialized = true
        }
    }

    interface TweenBase {
        var entity: Entity
        var delay: Double?
        var duration: Double?
        var easing: Easing?
    }

    /**
     * Animation Component data classes based on TweenBase
     */
    @Serializable
    @SerialName("SpawnNewTweenSequence")
    data class SpawnNewTweenSequence(
        val tweens: List<TweenBase> = listOf(),       // tween objects which contain entity and its properties to be animated in sequence

        override var entity: Entity = invalidEntity,  // not used
        override var delay: Double? = null,
        override var duration: Double? = null,
        @Serializable(with = EasingSerializer::class)
        override var easing: Easing? = null           // not used
    ) : TweenBase

    @Serializable
    @SerialName("ParallelTweens")
    data class ParallelTweens(
        val tweens: List<TweenBase> = listOf(),       // tween objects which contain entity and its properties to be animated in parallel

        override var entity: Entity = invalidEntity,  // not used here
        override var delay: Double? = 0.0,            // in seconds
        override var duration: Double? = 0.0,         // in seconds
        @Serializable(with = EasingSerializer::class)
        override var easing: Easing? = Easing.LINEAR  // function to change the properties
    ) : TweenBase

    @Serializable
    @SerialName("TweenSequence.Wait")
    data class Wait(
        override var entity: Entity = invalidEntity,  // not used
        override var delay: Double? = null,           // Not used
        override var duration: Double? = 0.0,
        @Serializable(with = EasingSerializer::class)
        override var easing: Easing? = null           // not used
    ) : TweenBase


    /**
     * Animation Component data classes based on TweenBaseHasEntity
     */
    @Serializable
    @SerialName("TweenSequence.DeleteEntity")
    data class DeleteEntity(
        val healthCounter: Int = 0,             // set healthCounter to zero to delete the entity immediately

        override var entity: Entity,
        override var delay: Double? = null,     // not used
        override var duration: Double? = null,  // not used
        @Serializable(with = EasingSerializer::class)
        override var easing: Easing? = null     // not used
    ) : TweenBase

    @Serializable
    @SerialName("TweenSequence.SpawnEntity")
    data class SpawnEntity(
        var config: Identifier,             // name of config for configuring spawned entity
        var function: Identifier,           // name of function which configures the spawned entity
        var x: Double = 0.0,                // position where entity will be spawned
        var y: Double = 0.0,

        override var entity: Entity = invalidEntity, // when entity is not given (=invalidEntity) than it will be created
        override var delay: Double? = null,
        override var duration: Double? = 0.0,  // not used - 0f for immediately
        @Serializable(with = EasingSerializer::class)
        override var easing: Easing? = null    // not used
    ) : TweenBase

    @Serializable
    @SerialName("TweenSequence.ExecuteConfigFunction")
    data class ExecuteConfigFunction(
        var function: Identifier,                     // name of function which configures the spawned entity
        var config: Identifier = nothing,             // [optional] name of config for configuring spawned entity

        override var entity: Entity = invalidEntity,  // [optional] entity can be provided if needed in the configure-function
        override var delay: Double? = null,           // not used
        override var duration: Double? = null,        // not used
        @Serializable(with = EasingSerializer::class)
        override var easing: Easing? = null           // not used
    ) : TweenBase

    // Following component classes are for triggering tweens on specific properties of components
    @Serializable
    @SerialName("TweenAppearance")
    data class TweenAppearance(
        val alpha: Double? = null,
// TODO        val tint: SpriteComponent.Rgb? = null,
        val visible: Boolean? = null,

        override var entity: Entity,
        override var delay: Double? = null,
        override var duration: Double? = null,
        @Serializable(with = EasingSerializer::class)
        override var easing: Easing? = null
    ) : TweenBase

    @Serializable
    @SerialName("TweenPosition")
    data class TweenPositionShape(
        val x: Double? = null,
        val y: Double? = null,

        override var entity: Entity,
        override var delay: Double? = null,
        override var duration: Double? = null,
        @Serializable(with = EasingSerializer::class)
        override var easing: Easing? = null
    ) : TweenBase

    @Serializable
    @SerialName("TweenOffset")
    data class TweenOffset(
        val x: Double? = null,
        val y: Double? = null,

        override var entity: Entity,
        override var delay: Double? = null,
        override var duration: Double? = null,
        @Serializable(with = EasingSerializer::class)
        override var easing: Easing? = null
    ) : TweenBase

    @Serializable
    @SerialName("TweenLayout")
    data class TweenLayout(
        val centerX: Boolean? = null,
        val centerY: Boolean? = null,
        val offsetX: Double? = null,
        val offsetY: Double? = null,

        override var entity: Entity,
        override var delay: Double? = null,
        override var duration: Double? = null,
        @Serializable(with = EasingSerializer::class)
        override var easing: Easing? = null
    ) : TweenBase

    @Serializable
    @SerialName("TweenSprite")
    data class TweenSprite(
        var animationName: String? = null,
        var isPlaying: Boolean? = null,
        var forwardDirection: Boolean? = null,
        var loop: Boolean? = null,
        var destroyOnPlayingFinished: Boolean? = null,

        override var entity: Entity,
        override var delay: Double? = null,
        override var duration: Double? = null,
        @Serializable(with = EasingSerializer::class)
        override var easing: Easing? = null
    ) : TweenBase

    @Serializable
    @SerialName("TweenSwitchLayerVisibility")
    data class TweenSwitchLayerVisibility(
        var offVariance: Double? = null,
        var onVariance: Double? = null,
        var spriteLayers: List<String>? = null,

        override var entity: Entity,
        override var delay: Double? = null,
        override var duration: Double? = null,
        @Serializable(with = EasingSerializer::class)
        override var easing: Easing? = null
    ) : TweenBase

    @Serializable
    @SerialName("TweenSpawner")
    data class TweenSpawner(
        var numberOfObjects: Int? = null,
        var interval: Int? = null,
        var timeVariation: Int? = null,
        var positionVariation: Double? = null,

        override var entity: Entity,
        override var delay: Double? = null,
        override var duration: Double? = null,
        @Serializable(with = EasingSerializer::class)
        override var easing: Easing? = null
    ) : TweenBase

    @Serializable
    @SerialName("TweenSound")
    data class TweenSound(
        var startTrigger: Boolean? = null,
        var stopTrigger: Boolean? = null,
        var position: Double? = null,
        var volume: Double? = null,

        override var entity: Entity,
        override var delay: Double? = null,
        override var duration: Double? = null,
        @Serializable(with = EasingSerializer::class)
        override var easing: Easing? = null
    ) : TweenBase

    companion object : ComponentType<TweenSequenceComponent>()
}

