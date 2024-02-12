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
        // TODO update unit test for this mapping from enum to here

        val AnimateSpriteIsPlaying = AnimateComponentType.SpriteIsPlaying.type
        val AnimateSpriteForwardDirection = AnimateComponentType.SpriteForwardDirection.type
        val AnimateSpriteLoop = AnimateComponentType.SpriteLoop.type
        val AnimateSpriteDestroyOnPlayingFinished = AnimateComponentType.SpriteDestroyOnPlayingFinished.type
        val AnimateSpriteAnimName = AnimateComponentType.SpriteAnimName.type

        val AnimateAppearanceAlpha = AnimateComponentType.AppearanceAlpha.type
        val AnimateAppearanceTint = AnimateComponentType.AppearanceTint.type
        val AnimateAppearanceVisible = AnimateComponentType.AppearanceVisible.type

        val AnimateSpawnerNumberOfObjects = AnimateComponentType.SpawnerNumberOfObjects.type
        val AnimateSpawnerInterval = AnimateComponentType.SpawnerInterval.type
        val AnimateSpawnerTimeVariation = AnimateComponentType.SpawnerTimeVariation.type
        val AnimateSpawnerPositionVariation = AnimateComponentType.SpawnerPositionVariation.type

        val AnimateLifeCycleHealthCounter = AnimateComponentType.LifeCycleHealthCounter.type

        val AnimatePositionShapeX = AnimateComponentType.PositionShapeX.type
        val AnimatePositionShapeY = AnimateComponentType.PositionShapeY.type

        val AnimateOffsetX = AnimateComponentType.OffsetX.type
        val AnimateOffsetY = AnimateComponentType.OffsetY.type

        val AnimateLayoutCenterX = AnimateComponentType.LayoutCenterX.type
        val AnimateLayoutCenterY = AnimateComponentType.LayoutCenterY.type
        val AnimateLayoutOffsetX = AnimateComponentType.LayoutOffsetX.type
        val AnimateLayoutOffsetY = AnimateComponentType.LayoutOffsetY.type

        val AnimateSwitchLayerVisibilityOnVariance = AnimateComponentType.SwitchLayerVisibilityOnVariance.type
        val AnimateSwitchLayerVisibilityOffVariance = AnimateComponentType.SwitchLayerVisibilityOffVariance.type

        val AnimateSoundStartTrigger = AnimateComponentType.SoundStartTrigger.type
        val AnimateSoundStopTrigger = AnimateComponentType.SoundStopTrigger.type
        val AnimateSoundPosition = AnimateComponentType.SoundPosition.type
        val AnimateSoundVolume = AnimateComponentType.SoundVolume.type

        val ExecuteConfigureFunction = AnimateComponentType.ConfigureFunction.type
    }
}

/**
 * All final [AnimateComponentType] names are organized in this enum. This is done to easily serialize the
 * animateProperty of the base AnimateComponent data class.
 */
enum class AnimateComponentType(val type: ComponentType<AnimateComponent>) {
    SpriteIsPlaying(componentTypeOf<AnimateComponent>()),
    SpriteForwardDirection(componentTypeOf<AnimateComponent>()),
    SpriteLoop(componentTypeOf<AnimateComponent>()),
    SpriteDestroyOnPlayingFinished(componentTypeOf<AnimateComponent>()),
    SpriteAnimName(componentTypeOf<AnimateComponent>()),

    AppearanceAlpha(componentTypeOf<AnimateComponent>()),
    AppearanceTint(componentTypeOf<AnimateComponent>()),
    AppearanceVisible(componentTypeOf<AnimateComponent>()),

    SpawnerNumberOfObjects(componentTypeOf<AnimateComponent>()),
    SpawnerInterval(componentTypeOf<AnimateComponent>()),
    SpawnerTimeVariation(componentTypeOf<AnimateComponent>()),
    SpawnerPositionVariation(componentTypeOf<AnimateComponent>()),

    // TODO not used yet in animation system
    LifeCycleHealthCounter(componentTypeOf<AnimateComponent>()),

    PositionShapeX(componentTypeOf<AnimateComponent>()),
    PositionShapeY(componentTypeOf<AnimateComponent>()),

    OffsetX(componentTypeOf<AnimateComponent>()),
    OffsetY(componentTypeOf<AnimateComponent>()),

    LayoutCenterX(componentTypeOf<AnimateComponent>()),
    LayoutCenterY(componentTypeOf<AnimateComponent>()),
    LayoutOffsetX(componentTypeOf<AnimateComponent>()),
    LayoutOffsetY(componentTypeOf<AnimateComponent>()),

    SwitchLayerVisibilityOnVariance(componentTypeOf<AnimateComponent>()),
    SwitchLayerVisibilityOffVariance(componentTypeOf<AnimateComponent>()),

    SoundStartTrigger(componentTypeOf<AnimateComponent>()),
    SoundStopTrigger(componentTypeOf<AnimateComponent>()),
    SoundPosition(componentTypeOf<AnimateComponent>()),
    SoundVolume(componentTypeOf<AnimateComponent>()),

    ConfigureFunction(componentTypeOf<AnimateComponent>())
}
