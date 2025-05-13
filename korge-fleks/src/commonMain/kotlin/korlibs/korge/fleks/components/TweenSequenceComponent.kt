package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.TweenSequence.TweenBase
import korlibs.korge.fleks.components.data.TextureRef.Companion.cleanup
import korlibs.korge.fleks.components.data.TextureRef.Companion.init
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component holds all needed details to animate properties of components of entities.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("TweenSequence")
class TweenSequence private constructor(
    var tweens: List<TweenBase> = listOf(),

    // Internal runtime data
    var index: Int = 0,              // This points to the animation step which is currently in progress
    var timeProgress: Float = 0f,    // Elapsed time for the object to be animated
    var waitTime: Float = 0f,
    var executed: Boolean = false
) : Poolable<TweenSequence>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: TweenSequence) {
        tweens = from.tweens  // List is static and elements do not change
        index = from.index
        timeProgress = from.timeProgress
        waitTime = from.waitTime
        executed = from.executed
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        // We own the static list of tweens - Thus, we need to put them back to the pool
        tweens.cleanup()
        tweens = listOf()
        index = 0
        timeProgress = 0f
        waitTime = 0f
        executed = false
    }

    override fun type() = TweenSequenceComponent

    companion object {
        val TweenSequenceComponent = componentTypeOf<TweenSequence>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticTweenSequenceComponent(config: TweenSequence.() -> Unit ): TweenSequence =
            TweenSequence().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun World.TweenSequenceComponent(config: TweenSequence.() -> Unit ): TweenSequence =
            getPoolable(TweenSequenceComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addTweenSequenceComponentPool(preAllocate: Int = 0) {
            addPool(TweenSequenceComponent, preAllocate) { TweenSequence() }
        }

        fun List<TweenBase>.cleanup() {
            forEach { tween ->
                tween.free()
            }
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): TweenSequence =
        getPoolable(TweenSequenceComponent).apply { init(from = this@TweenSequence ) }

    // Initialize the component automatically when it is added to an entity
    override fun World.initComponent(entity: Entity) {
        // Initialize internal waitTime property with delay value of first tween if available.
        if (tweens.isNotEmpty()) waitTime = tweens[index].delay ?: 0f
    }

    // Cleanup/Reset the component automatically when it is removed from an entity (component will be returned to the pool eventually)
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }

    interface TweenBase {
        var entity: Entity
        var delay: Float?
        var duration: Float?
        var easing: Easing?
        fun free()
    }
}







    @Serializable @SerialName("ParallelTweens")
    data class ParallelTweens(
        val tweens: List<TweenBase> = listOf(),       // tween objects which contain entity and its properties to be animated in parallel

        override var entity: Entity = Entity.NONE,    // not used here
        override var delay: Float? = 0f,              // in seconds
        override var duration: Float? = 0f,           // in seconds
        @Serializable(with = EasingAsString::class) override var easing: Easing? = Easing.LINEAR  // function to change the properties
    ) : TweenBase {
        override fun clone(): ParallelTweens {
            val copyOfTweens: MutableList<TweenBase> = mutableListOf()
            // Perform special deep copy of list elements
            tweens.forEach { element -> copyOfTweens.add(element.clone()) }

            return this.copy(
                tweens = copyOfTweens,
                entity = entity.clone(),
                easing = easing
            )
        }
    }

    @Serializable @SerialName("SendEvent")
    data class SendEvent(
        val event: Int = 0,                           // Set a specific event so that a Wait for event can be unlocked

        override var entity: Entity = Entity.NONE,    // not used
        override var delay: Float? = null,            // Not used
        override var duration: Float? = null,         // not used
        @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
    ) : TweenBase {
        override fun clone(): SendEvent =
            this.copy(
                entity = entity.clone(),
                easing = easing
            )
    }

    @Serializable @SerialName("ResetEvent")
    data class ResetEvent(
        val event: Int = 0,                           // Set a specific event so that a Wait for event can be unlocked

        override var entity: Entity = Entity.NONE,    // not used
        override var delay: Float? = null,            // Not used
        override var duration: Float? = null,         // not used
        @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
    ) : TweenBase {
        override fun clone(): ResetEvent =
            this.copy(
                entity = entity.clone(),
                easing = easing
            )
    }

    @Serializable @SerialName("Wait")
    data class Wait(
        override var duration: Float? = Float.MAX_VALUE,  // Use duration by setting explicitly a value
        val event: Int? = null,                       // Wait for a specific event if eventId is not "null" - need to unblock from infinite wait (Float.MAX_VALUE)

        override var entity: Entity = Entity.NONE,    // not used
        override var delay: Float? = null,            // Not used
        @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
    ) : TweenBase {
        override fun clone(): Wait =
            this.copy(
                entity = entity.clone(),
                easing = easing
            )
    }

    @Serializable @SerialName("Jump")
    data class Jump(
        var distance: Int,                            // Jump relative distance from current index (minus means jump back)
        val event: Int? = null,                       // Check for a specific event - if event is 0 than jump otherwise do not jump and tween execution continues with next tween in the list

        override var entity: Entity = Entity.NONE,    // not used
        override var delay: Float? = null,            // not used
        override var duration: Float? = null,         // not used
        @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
    ) : TweenBase {
        override fun clone(): Jump =
            this.copy(
                entity = entity.clone(),
                easing = easing
            )
    }


    @Serializable @SerialName("DeleteEntity")
    data class DeleteEntity(
        override var entity: Entity,
        override var delay: Float? = null,      // not used
        override var duration: Float? = null,   // not used
        @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
    ) : TweenBase {
        override fun clone(): DeleteEntity =
            this.copy(
                entity = entity.clone(),
                easing = easing
            )
    }

    @Serializable @SerialName("SpawnEntity")
    data class SpawnEntity(
        var entityConfig: String,           // name of the entity configuration which creates and configures the spawned entity
        var x: Float = 0f,                  // position where entity will be spawned
        var y: Float = 0f,

        override var entity: Entity = Entity.NONE, // when entity is not given (= Entity.NONE) than it will be created
        override var delay: Float? = null,
        override var duration: Float? = 0f,    // not used - 0f for immediately
        @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
    ) : TweenBase {
        override fun clone(): SpawnEntity =
            this.copy(
                entity = entity.clone(),
                easing = easing
            )
    }

    @Serializable @SerialName("ExecuteConfigFunction")
    data class ExecuteConfigFunction(
        var entityConfig: String,                     // name of entity config which contains the function to configure the spawned entity

        override var entity: Entity = Entity.NONE,    // [optional] entity can be provided if needed in the configure-function
        override var delay: Float? = null,            // not used
        override var duration: Float? = null,         // not used
        @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
    ) : TweenBase {
        override fun clone(): ExecuteConfigFunction =
            this.copy(
                entity = entity.clone(),
                easing = easing
            )
    }

    /**
     *  Following component classes are for triggering tweens on specific properties of components
     */
    @Serializable @SerialName("TweenTextField")
    data class TweenTextField(
        val text: String? = null,
        val textRangeStart: Int? = null,
        val textRangeEnd: Int? = null,

        override var entity: Entity,
        override var delay: Float? = null,
        override var duration: Float? = null,
        @Serializable(with = EasingAsString::class) override var easing: Easing? = null
    ) : TweenBase {
        override fun clone(): TweenTextField =
            this.copy(
                entity = entity.clone(),
                easing = easing
            )
    }

    @Serializable @SerialName("TweenRgba")
    data class TweenRgba(
        val alpha: Float? = null,
        val tint: Rgb? = null,
        val visible: Boolean? = null,

        override var entity: Entity,
        override var delay: Float? = null,
        override var duration: Float? = null,
        @Serializable(with = EasingAsString::class) override var easing: Easing? = null
    ) : TweenBase {
        override fun clone(): TweenRgba =
            this.copy(
                tint = tint?.clone(),
                entity = entity.clone(),
                easing = easing
            )
    }

    @Serializable @SerialName("TweenPosition")
    data class TweenPosition(
        val x: Float? = null,
        val y: Float? = null,
        val offsetX: Float? = null,
        val offsetY: Float? = null,

        override var entity: Entity,
        override var delay: Float? = null,
        override var duration: Float? = null,
        @Serializable(with = EasingAsString::class) override var easing: Easing? = null
    ) : TweenBase {
        override fun clone(): TweenPosition =
            this.copy(
                entity = entity.clone(),
                easing = easing
            )
    }

    @Serializable @SerialName("TweenMotion")
    data class TweenMotion(
        var velocityX: Float? = null,

        override var entity: Entity,
        override var delay: Float? = null,
        override var duration: Float? = null,
        @Serializable(with = EasingAsString::class) override var easing: Easing? = null
    ) : TweenBase {
        override fun clone(): TweenMotion =
            this.copy(
                entity = entity.clone(),
                easing = easing
            )
    }

    @Serializable @SerialName("TweenSprite")
    data class TweenSprite(
        var animation: String? = null,
        // do not tween the frameIndex, it is updated by the SpriteSystem
        var running: Boolean? = null,
        var direction: ImageAnimation.Direction? = null,
        var destroyOnPlayingFinished: Boolean? = null,

        override var entity: Entity,
        override var delay: Float? = null,
        override var duration: Float? = null,
        @Serializable(with = EasingAsString::class) override var easing: Easing? = null
    ) : TweenBase {
        override fun clone(): TweenSprite =
            this.copy(
                direction = direction,
                entity = entity.clone(),
                easing = easing
            )
    }

    @Serializable @SerialName("TweenSwitchLayerVisibility")
    data class TweenSwitchLayerVisibility(
        var offVariance: Float? = null,
        var onVariance: Float? = null,

        override var entity: Entity,
        override var delay: Float? = null,
        override var duration: Float? = null,
        @Serializable(with = EasingAsString::class) override var easing: Easing? = null
    ) : TweenBase {
        override fun clone(): TweenSwitchLayerVisibility =
            this.copy(
                entity = entity.clone(),
                easing = easing
            )
    }

    @Serializable @SerialName("TweenSpawner")
    data class TweenSpawner(
        var numberOfObjects: Int? = null,
        var interval: Int? = null,
        var timeVariation: Int? = null,
        var positionVariation: Float? = null,

        override var entity: Entity,
        override var delay: Float? = null,
        override var duration: Float? = null,
        @Serializable(with = EasingAsString::class) override var easing: Easing? = null
    ) : TweenBase {
        override fun clone(): TweenSpawner =
            this.copy(
                entity = entity.clone(),
                easing = easing
            )
    }

    @Serializable @SerialName("TweenSound")
    data class TweenSound(
        var startTrigger: Boolean? = null,
        var stopTrigger: Boolean? = null,
        var position: Float? = null,
        var volume: Float? = null,

        override var entity: Entity,
        override var delay: Float? = null,
        override var duration: Float? = null,
        @Serializable(with = EasingAsString::class) override var easing: Easing? = null
    ) : TweenBase {
        override fun clone(): TweenSound =
            this.copy(
                entity = entity.clone(),
                easing = easing
            )
    }

    @Serializable @SerialName("TweenTouchInput")
    data class TweenTouchInput(
        var enabled: Boolean? = null,

        override var entity: Entity,
        override var delay: Float? = null,
        override var duration: Float? = null,
        @Serializable(with = EasingAsString::class) override var easing: Easing? = null
    ) : TweenBase {
        override fun clone(): TweenTouchInput =
            this.copy(
                entity = entity.clone(),
                easing = easing
            )
    }

