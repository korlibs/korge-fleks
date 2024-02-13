package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.AnySerializer
import korlibs.korge.fleks.utils.EasingSerializer
import korlibs.korge.fleks.utils.SerializeBase
import korlibs.math.interpolation.Easing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Generalized Animate Component Property data class. It is used for animating properties of other components
 * via the [TweenSequence] components and one of the systems in AnimateSystems.kt file.
 *
 * value:  This is set to the previous or initial value
 * change: Value with which last value needs to be changed to reach the target value of the animation step
 *
 * In case of single switch: This value is set when easing > 0.5
 */
@Serializable
@SerialName("TweenComponent")
data class TweenComponent (
    var property: TweenProperty,

    @Serializable(with = AnySerializer::class)
    var change: Any = Unit,
    @Serializable(with = AnySerializer::class)
    var value: Any = Unit,

    var duration: Float = 0f,                    // in seconds
    var timeProgress: Float = 0f,                // in seconds
    @Serializable(with = EasingSerializer::class)
    var easing: Easing = Easing.LINEAR           // Changing function
) : Component<TweenComponent>, SerializeBase {
    override fun type(): ComponentType<TweenComponent> = property.type

    companion object {
        // TODO update unit test for this mapping from enum to here

        val AnimateSpriteIsPlaying = TweenProperty.SpriteIsPlaying.type
        val AnimateSpriteForwardDirection = TweenProperty.SpriteForwardDirection.type
        val AnimateSpriteLoop = TweenProperty.SpriteLoop.type
        val AnimateSpriteDestroyOnPlayingFinished = TweenProperty.SpriteDestroyOnPlayingFinished.type
        val AnimateSpriteAnimName = TweenProperty.SpriteAnimName.type

        val AnimateAppearanceAlpha = TweenProperty.AppearanceAlpha.type
        val AnimateAppearanceTint = TweenProperty.AppearanceTint.type
        val AnimateAppearanceVisible = TweenProperty.AppearanceVisible.type

        val AnimateSpawnerNumberOfObjects = TweenProperty.SpawnerNumberOfObjects.type
        val AnimateSpawnerInterval = TweenProperty.SpawnerInterval.type
        val AnimateSpawnerTimeVariation = TweenProperty.SpawnerTimeVariation.type
        val AnimateSpawnerPositionVariation = TweenProperty.SpawnerPositionVariation.type

        val AnimateLifeCycleHealthCounter = TweenProperty.LifeCycleHealthCounter.type

        val AnimatePositionShapeX = TweenProperty.PositionShapeX.type
        val AnimatePositionShapeY = TweenProperty.PositionShapeY.type

        val AnimateOffsetX = TweenProperty.OffsetX.type
        val AnimateOffsetY = TweenProperty.OffsetY.type

        val AnimateLayoutCenterX = TweenProperty.LayoutCenterX.type
        val AnimateLayoutCenterY = TweenProperty.LayoutCenterY.type
        val AnimateLayoutOffsetX = TweenProperty.LayoutOffsetX.type
        val AnimateLayoutOffsetY = TweenProperty.LayoutOffsetY.type

        val AnimateSwitchLayerVisibilityOnVariance = TweenProperty.SwitchLayerVisibilityOnVariance.type
        val AnimateSwitchLayerVisibilityOffVariance = TweenProperty.SwitchLayerVisibilityOffVariance.type

        val AnimateSoundStartTrigger = TweenProperty.SoundStartTrigger.type
        val AnimateSoundStopTrigger = TweenProperty.SoundStopTrigger.type
        val AnimateSoundPosition = TweenProperty.SoundPosition.type
        val AnimateSoundVolume = TweenProperty.SoundVolume.type

        val ExecuteConfigureFunction = TweenProperty.ConfigureFunction.type
    }
}

/**
 * All final [TweenProperty] names are organized in this enum. This is done to easily serialize the
 * [property](TweenComponent.property) of the base [TweenComponent] data class.
 */
enum class TweenProperty(val type: ComponentType<TweenComponent>) {
    SpriteIsPlaying(componentTypeOf<TweenComponent>()),
    SpriteForwardDirection(componentTypeOf<TweenComponent>()),
    SpriteLoop(componentTypeOf<TweenComponent>()),
    SpriteDestroyOnPlayingFinished(componentTypeOf<TweenComponent>()),
    SpriteAnimName(componentTypeOf<TweenComponent>()),

    AppearanceAlpha(componentTypeOf<TweenComponent>()),
    AppearanceTint(componentTypeOf<TweenComponent>()),
    AppearanceVisible(componentTypeOf<TweenComponent>()),

    SpawnerNumberOfObjects(componentTypeOf<TweenComponent>()),
    SpawnerInterval(componentTypeOf<TweenComponent>()),
    SpawnerTimeVariation(componentTypeOf<TweenComponent>()),
    SpawnerPositionVariation(componentTypeOf<TweenComponent>()),

    // TODO not used yet in animation system
    LifeCycleHealthCounter(componentTypeOf<TweenComponent>()),

    PositionShapeX(componentTypeOf<TweenComponent>()),
    PositionShapeY(componentTypeOf<TweenComponent>()),

    OffsetX(componentTypeOf<TweenComponent>()),
    OffsetY(componentTypeOf<TweenComponent>()),

    LayoutCenterX(componentTypeOf<TweenComponent>()),
    LayoutCenterY(componentTypeOf<TweenComponent>()),
    LayoutOffsetX(componentTypeOf<TweenComponent>()),
    LayoutOffsetY(componentTypeOf<TweenComponent>()),

    SwitchLayerVisibilityOnVariance(componentTypeOf<TweenComponent>()),
    SwitchLayerVisibilityOffVariance(componentTypeOf<TweenComponent>()),

    SoundStartTrigger(componentTypeOf<TweenComponent>()),
    SoundStopTrigger(componentTypeOf<TweenComponent>()),
    SoundPosition(componentTypeOf<TweenComponent>()),
    SoundVolume(componentTypeOf<TweenComponent>()),

    ConfigureFunction(componentTypeOf<TweenComponent>())
}
