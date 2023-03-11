package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.soywiz.korgeFleks.utils.AnySerializer
import com.soywiz.korgeFleks.utils.EasingSerializer
import com.soywiz.korgeFleks.utils.SerializeBase
import com.soywiz.korma.interpolation.Easing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Generalized Animate Component Property data class. It is used for animating properties of other components
 * via the [AnimationScript] components and one of the systems in AnimateSystems.kt file.
 *
 * value:  This is set to the previous or initial value
 * change: Value with which last value needs to be changed to reach the target value of the animation step
 *
 * In case of single switch: This value is set when easing > 0.5
 */
@Serializable
@SerialName("AnimateComponent")
data class AnimateComponent (
    var componentProperty: AnimateComponentType,

    @Serializable(with = AnySerializer::class)
    var change: Any = Unit,
    @Serializable(with = AnySerializer::class)
    var value: Any = Unit,

    var duration: Float = 0f,                    // in seconds
    var timeProgress: Float = 0f,                // in seconds
    @Serializable(with = EasingSerializer::class)
    var easing: Easing = Easing.LINEAR           // Changing function
) : Component<AnimateComponent>, SerializeBase {
    override fun type(): ComponentType<AnimateComponent> = componentProperty.type

    companion object {
        val AnimateSpriteIsPlaying = object : ComponentType<AnimateComponent>() {}
        val AnimateSpriteForwardDirection = object : ComponentType<AnimateComponent>() {}
        val AnimateSpriteLoop = object : ComponentType<AnimateComponent>() {}
        val AnimateSpriteDestroyOnPlayingFinished = object : ComponentType<AnimateComponent>() {}
        val AnimateSpriteAnimName = object : ComponentType<AnimateComponent>() {}

        val AnimateAppearanceAlpha = object : ComponentType<AnimateComponent>() {}
        val AnimateAppearanceTint = object : ComponentType<AnimateComponent>() {}
        val AnimateAppearanceVisible = object : ComponentType<AnimateComponent>() {}

        val AnimateSpawnerNumberOfObjects = object : ComponentType<AnimateComponent>() {}
        val AnimateSpawnerInterval = object : ComponentType<AnimateComponent>() {}
        val AnimateSpawnerTimeVariation = object : ComponentType<AnimateComponent>() {}
        val AnimateSpawnerPositionVariation = object : ComponentType<AnimateComponent>() {}

        val AnimateLifeCycleHealthCounter = object : ComponentType<AnimateComponent>() {}

        val AnimatePositionShapeX = object : ComponentType<AnimateComponent>() {}
        val AnimatePositionShapeY = object : ComponentType<AnimateComponent>() {}

        val AnimateOffsetX = object : ComponentType<AnimateComponent>() {}
        val AnimateOffsetY = object : ComponentType<AnimateComponent>() {}

        val AnimateSwitchLayerVisibilityOnVariance = object : ComponentType<AnimateComponent>() {}
        val AnimateSwitchLayerVisibilityOffVariance = object : ComponentType<AnimateComponent>() {}

        val AnimateSoundStartTrigger = object : ComponentType<AnimateComponent>() {}
        val AnimateSoundStopTrigger = object : ComponentType<AnimateComponent>() {}
        val AnimateSoundPosition = object : ComponentType<AnimateComponent>() {}
        val AnimateSoundVolume = object : ComponentType<AnimateComponent>() {}
    }
}

enum class AnimateComponentType(val type: ComponentType<AnimateComponent>) {
    SpriteIsPlaying(AnimateComponent.AnimateSpriteIsPlaying),
    SpriteForwardDirection(AnimateComponent.AnimateSpriteForwardDirection),
    SpriteLoop(AnimateComponent.AnimateSpriteLoop),
    SpriteDestroyOnPlayingFinished(AnimateComponent.AnimateSpriteDestroyOnPlayingFinished),
    SpriteAnimName(AnimateComponent.AnimateSpriteAnimName),

    AppearanceAlpha(AnimateComponent.AnimateAppearanceAlpha),
    AppearanceTint(AnimateComponent.AnimateAppearanceTint),
    AppearanceVisible(AnimateComponent.AnimateAppearanceVisible),

    SpawnerNumberOfObjects(AnimateComponent.AnimateSpawnerNumberOfObjects),
    SpawnerInterval(AnimateComponent.AnimateSpawnerInterval),
    SpawnerTimeVariation(AnimateComponent.AnimateSpawnerTimeVariation),
    SpawnerPositionVariation(AnimateComponent.AnimateSpawnerPositionVariation),

    LifeCycleHealthCounter(AnimateComponent.AnimateLifeCycleHealthCounter),  // TODO not used yet in animation system

    PositionShapeX(AnimateComponent.AnimatePositionShapeX),
    PositionShapeY(AnimateComponent.AnimatePositionShapeY),

    OffsetX(AnimateComponent.AnimateOffsetX),
    OffsetY(AnimateComponent.AnimateOffsetY),

    SwitchLayerVisibilityOnVariance(AnimateComponent.AnimateSwitchLayerVisibilityOnVariance),
    SwitchLayerVisibilityOffVariance(AnimateComponent.AnimateSwitchLayerVisibilityOffVariance),

    SoundStartTrigger(AnimateComponent.AnimateSoundStartTrigger),
    SoundStopTrigger(AnimateComponent.AnimateSoundStopTrigger),
    SoundPosition(AnimateComponent.AnimateSoundPosition),
    SoundVolume(AnimateComponent.AnimateSoundVolume)
}
