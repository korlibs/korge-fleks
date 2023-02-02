package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.*
import com.soywiz.korma.interpolation.Easing
import com.soywiz.korgeFleks.entity.config.*

/**
 * This component holds all needed details to animate an entity's component.
 */
data class AnimationScript(
    var debugAnnotate: Boolean = false,  // TODO not implemented yet
    var tweens: List<TweenBase> = listOf(),

    // Internal runtime data
    var index: Int = 0,            // This points to the animation step which is currently in progress
    var timeProgress: Float = 0f,  // Elapsed time for the object to be animated
    var waitTime: Float = 0f,
    var active: Boolean = false
) : Component<AnimationScript> {
    override fun type(): ComponentType<AnimationScript> = AnimationScript

    companion object : ComponentType<AnimationScript>() {
        /**
         * Initialize internal waitTime property with delay value of first tweens if available.
         */
        val onComponentAdded: ComponentHook<AnimationScript> = { entity, component ->
            if (component.tweens.isNotEmpty()) component.waitTime = component.tweens[component.index].delay ?: 0f
        }

        val onComponentRemoved: ComponentHook<AnimationScript> = { entity, component ->
            // not used
        }
    }
}

interface TweenBase {
    var delay: Float?
    var duration: Float?
    var easing: Easing?
}
interface TweenHasEntity {
    var entity: Entity
}
interface TweenBaseHasEntity : TweenBase, TweenHasEntity

data class TweenSequence(
    val tweens: List<TweenBase> = listOf(),  // tween objects which contain entity and its properties to be animated in sequence

    override var delay: Float? = null,       // following 3 properties not used here
    override var duration: Float? = null,
    override var easing: Easing? = null
) : TweenBase

data class ParallelTweens(
    val tweens: List<TweenBaseHasEntity> = listOf(),  // tween objects which contain entity and its properties to be animated in parallel

    override var delay: Float? = 0f,                  // in seconds
    override var duration: Float? = 0f,               // in seconds
    override var easing: Easing? = Easing.LINEAR      // function to change the properties
) : TweenBase

data class DeleteEntity(
    val healthCounter: Int = 0,            // set healthCounter to zero to delete the entity immediately

    override var entity: Entity,
    override var delay: Float? = null,   // not used
    override var duration: Float? = 0f,  // not used
    override var easing: Easing? = null  // not used
) : TweenBaseHasEntity

data class SpawnEntity(
    var spawnFunction: String = "",        // name of function which spawns the new object
    var createNewEntity: Boolean = false,  // do not create a new entity, use entity which was specified with entity property below
    var x: Double = 0.0,                   // position where entity will be spawned
    var y: Double = 0.0,
    var config: Config = noConfig,

    override var entity: Entity,
    override var delay: Float? = null,   // not used
    override var duration: Float? = 0f,  // not used
    override var easing: Easing? = null  // not used
) : TweenBaseHasEntity

data class Wait(
    override var delay: Float? = null,   // Not used
    override var duration: Float? = 0f,
    override var easing: Easing? = null  // not used
) : TweenBase

// Following component classes are for animating specific components
data class TweenAppearance(
    val alpha: Double? = null,
    val tint: Rgb? = null,
    val visible: Boolean? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    override var easing: Easing? = null
) : TweenBaseHasEntity

data class TweenPositionShape(
    val x: Double? = null,
    val y: Double? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    override var easing: Easing? = null
) : TweenBaseHasEntity

data class TweenOffset(
    val x: Double? = null,
    val y: Double? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    override var easing: Easing? = null
) : TweenBaseHasEntity

data class TweenSprite(
    var animationName: String? = null,
    var isPlaying: Boolean? = null,
    var forwardDirection: Boolean? = null,
    var loop: Boolean? = null,
    var destroyOnPlayingFinished: Boolean? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    override var easing: Easing? = null
) : TweenBaseHasEntity

data class TweenSwitchLayerVisibility(
    var onVariance: Double? = null,
    var offVariance: Double? = null,
    var spriteLayers: List<String>? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    override var easing: Easing? = null
) : TweenBaseHasEntity

data class TweenSpawner(
    var numberOfObjects: Int? = null,
    var interval: Int? = null,
    var timeVariation: Int? = null,
    var positionVariation: Double? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    override var easing: Easing? = null
) : TweenBaseHasEntity

data class TweenSound(
    var startTrigger: Boolean? = null,
    var stopTrigger: Boolean? = null,
    var position: Double? = null,
    var volume: Double? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    override var easing: Easing? = null
) : TweenBaseHasEntity
