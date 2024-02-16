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

    var duration: Float = 0f,                    // in seconds
    var timeProgress: Float = 0f,                // in seconds
    @Serializable(with = EasingSerializer::class)
    var easing: Easing = Easing.LINEAR           // Changing function
) : Component<TweenPropertyComponent>, SerializeBase {
    override fun type(): ComponentType<TweenPropertyComponent> = property.type

    companion object {
        // TODO update unit test for this mapping from enum to here

        val AnimateSpriteIsPlaying = TweenProperty.SpriteIsPlaying.type
        val AnimateSpriteForwardDirection = TweenProperty.SpriteForwardDirection.type
        val AnimateSpriteLoop = TweenProperty.SpriteLoop.type
        val AnimateSpriteDestroyOnPlayingFinished = TweenProperty.SpriteDestroyOnPlayingFinished.type
        val AnimateSpriteAnimName = TweenProperty.SpriteAnimName.type

        val TweenAppearanceAlphaComponent = TweenProperty.AppearanceAlpha.type
        val AnimateAppearanceTint = TweenProperty.AppearanceTint.type
        val AnimateAppearanceVisible = TweenProperty.AppearanceVisible.type

        val AnimateSpawnerNumberOfObjects = TweenProperty.SpawnerNumberOfObjects.type
        val AnimateSpawnerInterval = TweenProperty.SpawnerInterval.type
        val AnimateSpawnerTimeVariation = TweenProperty.SpawnerTimeVariation.type
        val AnimateSpawnerPositionVariation = TweenProperty.SpawnerPositionVariation.type

        val AnimateLifeCycleHealthCounter = TweenProperty.LifeCycleHealthCounter.type

        val TweenPositionShapeXComponent = TweenProperty.PositionShapeX.type
        val TweenPositionShapeYComponent = TweenProperty.PositionShapeY.type

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

    ConfigureFunction(componentTypeOf<TweenPropertyComponent>())
}
