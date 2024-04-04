package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.AnySerializer
import korlibs.korge.fleks.utils.EasingSerializer
import korlibs.math.interpolation.Easing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Generalized Animate Component Property data class. It is used for animating properties of other components
 * via the [TweenSequenceComponent] components and one of the systems in AnimateSystems.kt file.
 *
 * value:  This is set to the previous or initial value
 * change: Value with which last value needs to be changed to reach the target value of the animation step
 *
 * In case of single switch: This value is set when easing > 0.5
 */
@Serializable
@SerialName("TweenPropertyComponent")
data class TweenPropertyComponent (
    var property: TweenProperty,

    @Serializable(with = AnySerializer::class)
    var change: Any = Unit,
    @Serializable(with = AnySerializer::class)
    var value: Any = Unit,

    var duration: Double = 0.0,                  // in seconds
    var timeProgress: Double = 0.0,              // in seconds
    @Serializable(with = EasingSerializer::class)
    var easing: Easing = Easing.LINEAR           // Changing function
) : Component<TweenPropertyComponent> {
    override fun type(): ComponentType<TweenPropertyComponent> = property.type

    /**
     * All final [TweenProperty] names are organized in this enum. This is done to easily serialize the
     * [property](TweenComponent.property) of the base [TweenPropertyComponent] data class.
     */
    enum class TweenProperty(val type: ComponentType<TweenPropertyComponent>) {
        SpriteIsPlaying(componentTypeOf<TweenPropertyComponent>()),
        SpriteForwardDirection(componentTypeOf<TweenPropertyComponent>()),
        SpriteLoop(componentTypeOf<TweenPropertyComponent>()),
        SpriteDestroyOnPlayingFinished(componentTypeOf<TweenPropertyComponent>()),
        SpriteAnimName(componentTypeOf<TweenPropertyComponent>()),

        AppearanceAlpha(componentTypeOf<TweenPropertyComponent>()),
        AppearanceTint(componentTypeOf<TweenPropertyComponent>()),
        AppearanceVisible(componentTypeOf<TweenPropertyComponent>()),

        SpawnerNumberOfObjects(componentTypeOf<TweenPropertyComponent>()),
        SpawnerInterval(componentTypeOf<TweenPropertyComponent>()),
        SpawnerTimeVariation(componentTypeOf<TweenPropertyComponent>()),
        SpawnerPositionVariation(componentTypeOf<TweenPropertyComponent>()),

        // TODO not used yet in animation system
        LifeCycleHealthCounter(componentTypeOf<TweenPropertyComponent>()),

        PositionShapeX(componentTypeOf<TweenPropertyComponent>()),
        PositionShapeY(componentTypeOf<TweenPropertyComponent>()),

        OffsetX(componentTypeOf<TweenPropertyComponent>()),
        OffsetY(componentTypeOf<TweenPropertyComponent>()),

        LayoutCenterX(componentTypeOf<TweenPropertyComponent>()),
        LayoutCenterY(componentTypeOf<TweenPropertyComponent>()),
        LayoutOffsetX(componentTypeOf<TweenPropertyComponent>()),
        LayoutOffsetY(componentTypeOf<TweenPropertyComponent>()),

        SwitchLayerVisibilityOnVariance(componentTypeOf<TweenPropertyComponent>()),
        SwitchLayerVisibilityOffVariance(componentTypeOf<TweenPropertyComponent>()),

        SoundStartTrigger(componentTypeOf<TweenPropertyComponent>()),
        SoundStopTrigger(componentTypeOf<TweenPropertyComponent>()),
        SoundPosition(componentTypeOf<TweenPropertyComponent>()),
        SoundVolume(componentTypeOf<TweenPropertyComponent>()),

        NoisyMoveX(componentTypeOf<TweenPropertyComponent>()),
        NoisyMoveY(componentTypeOf<TweenPropertyComponent>()),

        ConfigureFunction(componentTypeOf<TweenPropertyComponent>())
    }

    companion object {
        // TODO update unit test for this mapping from enum to here

        val TweenSpriteIsPlaying = TweenProperty.SpriteIsPlaying.type
        val TweenSpriteForwardDirection = TweenProperty.SpriteForwardDirection.type
        val TweenSpriteLoop = TweenProperty.SpriteLoop.type
        val TweenSpriteDestroyOnPlayingFinished = TweenProperty.SpriteDestroyOnPlayingFinished.type
        val TweenSpriteAnimName = TweenProperty.SpriteAnimName.type

        val TweenAppearanceAlphaComponent = TweenProperty.AppearanceAlpha.type
        val TweenAppearanceTint = TweenProperty.AppearanceTint.type
        val TweenAppearanceVisible = TweenProperty.AppearanceVisible.type

        val TweenSpawnerNumberOfObjects = TweenProperty.SpawnerNumberOfObjects.type
        val TweenSpawnerInterval = TweenProperty.SpawnerInterval.type
        val TweenSpawnerTimeVariation = TweenProperty.SpawnerTimeVariation.type
        val TweenSpawnerPositionVariation = TweenProperty.SpawnerPositionVariation.type

        val TweenLifeCycleHealthCounter = TweenProperty.LifeCycleHealthCounter.type

        val TweenPositionShapeXComponent = TweenProperty.PositionShapeX.type
        val TweenPositionShapeYComponent = TweenProperty.PositionShapeY.type

        val TweenOffsetX = TweenProperty.OffsetX.type
        val TweenOffsetY = TweenProperty.OffsetY.type

        val TweenLayoutCenterX = TweenProperty.LayoutCenterX.type
        val TweenLayoutCenterY = TweenProperty.LayoutCenterY.type
        val TweenLayoutOffsetX = TweenProperty.LayoutOffsetX.type
        val TweenLayoutOffsetY = TweenProperty.LayoutOffsetY.type

        val TweenSwitchLayerVisibilityOnVariance = TweenProperty.SwitchLayerVisibilityOnVariance.type
        val TweenSwitchLayerVisibilityOffVariance = TweenProperty.SwitchLayerVisibilityOffVariance.type

        val TweenSoundStartTrigger = TweenProperty.SoundStartTrigger.type
        val TweenSoundStopTrigger = TweenProperty.SoundStopTrigger.type
        val TweenSoundPosition = TweenProperty.SoundPosition.type
        val TweenSoundVolume = TweenProperty.SoundVolume.type

        val TweenNoisyMoveX = TweenProperty.NoisyMoveX.type
        val TweenNoisyMoveY = TweenProperty.NoisyMoveY.type

        val ExecuteConfigureFunction = TweenProperty.ConfigureFunction.type
    }
}
