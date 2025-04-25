package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.Easing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Generalized Animate Component Property data class. It is used for animating properties of other components
 * via the [TweenSequenceComponent] components and one of the systems in TweenAnimationSystems.kt file.
 *
 * value:  This is set to the previous or initial value
 * change: Value with which last value needs to be changed to reach the target value of the animation step
 *
 * In case of single switch: This value is set when easing > 0.5
 */
@Serializable @SerialName("TweenProperty")
data class TweenPropertyComponent (
    var property: TweenProperty,

    @Serializable(with = AnySerializer::class) var change: Any = Unit,
    @Serializable(with = AnySerializer::class) var value: Any = Unit,

    var duration: Float = 0f,                    // in seconds
    var timeProgress: Float = 0f,                // in seconds
    @Serializable(with = EasingAsString::class) var easing: Easing = Easing.LINEAR  // Changing function
) : Poolable<TweenPropertyComponent>() {
    override fun type(): ComponentType<TweenPropertyComponent> = property.type

    /**
     * All final [TweenProperty] names are organized in this enum. This is done to easily serialize the
     * [property](TweenComponent.property) of the base [TweenPropertyComponent] data class.
     */
    enum class TweenProperty(val type: ComponentType<TweenPropertyComponent>) {
        PositionOffsetX(componentTypeOf<TweenPropertyComponent>()),
        PositionOffsetY(componentTypeOf<TweenPropertyComponent>()),
        PositionX(componentTypeOf<TweenPropertyComponent>()),
        PositionY(componentTypeOf<TweenPropertyComponent>()),

        MotionVelocityX(componentTypeOf<TweenPropertyComponent>()),

        RgbaAlpha(componentTypeOf<TweenPropertyComponent>()),
        RgbaTint(componentTypeOf<TweenPropertyComponent>()),

        SpawnerInterval(componentTypeOf<TweenPropertyComponent>()),
        SpawnerNumberOfObjects(componentTypeOf<TweenPropertyComponent>()),
        SpawnerPositionVariation(componentTypeOf<TweenPropertyComponent>()),
        SpawnerTimeVariation(componentTypeOf<TweenPropertyComponent>()),

        SpriteRunning(componentTypeOf<TweenPropertyComponent>()),
        SpriteDirection(componentTypeOf<TweenPropertyComponent>()),
        SpriteDestroyOnPlayingFinished(componentTypeOf<TweenPropertyComponent>()),
        SpriteAnimation(componentTypeOf<TweenPropertyComponent>()),

        // TODO not used yet in animation system
//        LifeCycleHealthCounter(componentTypeOf<TweenPropertyComponent>()),

        SwitchLayerVisibilityOnVariance(componentTypeOf<TweenPropertyComponent>()),
        SwitchLayerVisibilityOffVariance(componentTypeOf<TweenPropertyComponent>()),

        SoundStartTrigger(componentTypeOf<TweenPropertyComponent>()),
        SoundStopTrigger(componentTypeOf<TweenPropertyComponent>()),
        SoundPosition(componentTypeOf<TweenPropertyComponent>()),
        SoundVolume(componentTypeOf<TweenPropertyComponent>()),

//        NoisyMoveX(componentTypeOf<TweenPropertyComponent>()),
//        NoisyMoveY(componentTypeOf<TweenPropertyComponent>()),

        TextFieldText(componentTypeOf<TweenPropertyComponent>()),
        TextFieldTextRangeStart(componentTypeOf<TweenPropertyComponent>()),
        TextFieldTextRangeEnd(componentTypeOf<TweenPropertyComponent>()),

        EventPublish(componentTypeOf<TweenPropertyComponent>()),
        EventReset(componentTypeOf<TweenPropertyComponent>()),
        EventSubscribe(componentTypeOf<TweenPropertyComponent>()),

        TouchInputEnable(componentTypeOf<TweenPropertyComponent>())
    }

    companion object {
        // TODO update unit test for this mapping from enum to here
        val TweenPositionOffsetXComponent = TweenProperty.PositionOffsetX.type
        val TweenPositionOffsetYComponent = TweenProperty.PositionOffsetY.type
        val TweenPositionXComponent = TweenProperty.PositionX.type
        val TweenPositionYComponent = TweenProperty.PositionY.type

        val TweenMotionVelocityXComponent = TweenProperty.MotionVelocityX.type

        val TweenRgbaAlphaComponent = TweenProperty.RgbaAlpha.type
        val TweenRgbaTintComponent = TweenProperty.RgbaTint.type

        val TweenSpawnerIntervalComponent = TweenProperty.SpawnerInterval.type
        val TweenSpawnerNumberOfObjectsComponent = TweenProperty.SpawnerNumberOfObjects.type
        val TweenSpawnerPositionVariationComponent = TweenProperty.SpawnerPositionVariation.type
        val TweenSpawnerTimeVariationComponent = TweenProperty.SpawnerTimeVariation.type

        val TweenSpriteAnimationComponent = TweenProperty.SpriteAnimation.type
        val TweenSpriteDirectionComponent = TweenProperty.SpriteDirection.type
        val TweenSpriteDestroyOnPlayingFinishedComponent = TweenProperty.SpriteDestroyOnPlayingFinished.type
        val TweenSpriteRunningComponent = TweenProperty.SpriteRunning.type

//        val TweenLifeCycleHealthCounter = TweenProperty.LifeCycleHealthCounter.type

        val TweenSwitchLayerVisibilityOnVarianceComponent = TweenProperty.SwitchLayerVisibilityOnVariance.type
        val TweenSwitchLayerVisibilityOffVarianceComponent = TweenProperty.SwitchLayerVisibilityOffVariance.type

        val TweenSoundStartTriggerComponent = TweenProperty.SoundStartTrigger.type
        val TweenSoundStopTriggerComponent = TweenProperty.SoundStopTrigger.type
        val TweenSoundPositionComponent = TweenProperty.SoundPosition.type
        val TweenSoundVolumeComponent = TweenProperty.SoundVolume.type

//        val TweenNoisyMoveX = TweenProperty.NoisyMoveX.type
//        val TweenNoisyMoveY = TweenProperty.NoisyMoveY.type

        val TweenTextFieldTextComponent = TweenProperty.TextFieldText.type
        val TweenTextFieldTextRangeStartComponent = TweenProperty.TextFieldTextRangeStart.type
        val TweenTextFieldTextRangeEndComponent = TweenProperty.TextFieldTextRangeEnd.type

        val TweenEventPublishComponent = TweenProperty.EventPublish.type
        val TweenEventResetComponent = TweenProperty.EventReset.type
        val TweenEventSubscribeComponent = TweenProperty.EventSubscribe.type

        val TweenTouchInputEnableComponent = TweenProperty.TouchInputEnable.type
    }

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): TweenPropertyComponent =
        this.copy(
            change = change,
            value = value,
            easing = easing
        )
}
