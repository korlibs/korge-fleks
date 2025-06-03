package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
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
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("TweenProperty")
class TweenProperty private constructor(
    var property: TweenPropertyType = TweenPropertyType.PositionOffsetX,

    @Serializable(with = AnySerializer::class) var change: Any = Unit,
    @Serializable(with = AnySerializer::class) var value: Any = Unit,

    var duration: Float = 0f,                    // in seconds
    var timeProgress: Float = 0f,                // in seconds
    @Serializable(with = EasingAsString::class) var easing: Easing = Easing.LINEAR  // Changing function
) : PoolableComponents<TweenProperty>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: TweenProperty) {
        property = from.property
        change = from.change
        value = from.value
        duration = from.duration
        timeProgress = from.timeProgress
        easing = from.easing
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        property = TweenPropertyType.PositionOffsetX
        change = Unit
        value = Unit
        duration = 0f
        timeProgress = 0f
        easing = Easing.LINEAR
    }

    override fun type(): ComponentType<TweenProperty> = property.type

    companion object {
        // TODO update unit test for this mapping from enum to here
        val TweenPositionOffsetXComponent = TweenPropertyType.PositionOffsetX.type
        val TweenPositionOffsetYComponent = TweenPropertyType.PositionOffsetY.type
        val TweenPositionXComponent = TweenPropertyType.PositionX.type
        val TweenPositionYComponent = TweenPropertyType.PositionY.type

        val TweenMotionVelocityXComponent = TweenPropertyType.MotionVelocityX.type

        val TweenRgbaAlphaComponent = TweenPropertyType.RgbaAlpha.type
        val TweenRgbaTintComponent = TweenPropertyType.RgbaTint.type

        val TweenSpawnerIntervalComponent = TweenPropertyType.SpawnerInterval.type
        val TweenSpawnerNumberOfObjectsComponent = TweenPropertyType.SpawnerNumberOfObjects.type
        val TweenSpawnerPositionVariationComponent = TweenPropertyType.SpawnerPositionVariation.type
        val TweenSpawnerTimeVariationComponent = TweenPropertyType.SpawnerTimeVariation.type

        val TweenSpriteAnimationComponent = TweenPropertyType.SpriteAnimation.type
        val TweenSpriteDirectionComponent = TweenPropertyType.SpriteDirection.type
        val TweenSpriteDestroyOnPlayingFinishedComponent = TweenPropertyType.SpriteDestroyOnPlayingFinished.type
        val TweenSpriteRunningComponent = TweenPropertyType.SpriteRunning.type

//        val TweenLifeCycleHealthCounter = TweenPropertyType.LifeCycleHealthCounter.type

        val TweenSwitchLayerVisibilityOnVarianceComponent = TweenPropertyType.SwitchLayerVisibilityOnVariance.type
        val TweenSwitchLayerVisibilityOffVarianceComponent = TweenPropertyType.SwitchLayerVisibilityOffVariance.type

        val TweenSoundStartTriggerComponent = TweenPropertyType.SoundStartTrigger.type
        val TweenSoundStopTriggerComponent = TweenPropertyType.SoundStopTrigger.type
        val TweenSoundPositionComponent = TweenPropertyType.SoundPosition.type
        val TweenSoundVolumeComponent = TweenPropertyType.SoundVolume.type

//        val TweenNoisyMoveX = TweenPropertyType.NoisyMoveX.type
//        val TweenNoisyMoveY = TweenPropertyType.NoisyMoveY.type

        val TweenTextFieldTextComponent = TweenPropertyType.TextFieldText.type
        val TweenTextFieldTextRangeStartComponent = TweenPropertyType.TextFieldTextRangeStart.type
        val TweenTextFieldTextRangeEndComponent = TweenPropertyType.TextFieldTextRangeEnd.type

        val TweenEventPublishComponent = TweenPropertyType.EventPublish.type
        val TweenEventResetComponent = TweenPropertyType.EventReset.type
        val TweenEventSubscribeComponent = TweenPropertyType.EventSubscribe.type

        val TweenTouchInputEnableComponent = TweenPropertyType.TouchInputEnable.type

        // Use this function to create a new instance of component data as val inside another component
        fun staticTweenPropertyComponent(config: TweenProperty.() -> Unit ): TweenProperty =
            TweenProperty().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun World.TweenPropertyComponent(componentType: ComponentType<TweenProperty>, config: TweenProperty.() -> Unit ): TweenProperty =
            // All component types share the same pool - so just use the first component type to get it
            getPoolable(componentType).apply(config)
        // TODO: Check if we should use generic function above or specific one below (need to be created for each component type)
        fun World.TweenPositionOffsetXComponent(config: TweenProperty.() -> Unit ): TweenProperty =
            getPoolable(TweenPositionOffsetXComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addTweenPropertyComponentPools(preAllocate: Int = 0) {
            // Add only one common pool for all TweenProperty components - they share the same data
            val pool = Pool(preAllocate) { TweenProperty() }
            addPool(TweenPositionOffsetXComponent, pool)
            addPool(TweenPositionOffsetYComponent, pool)
            addPool(TweenPositionXComponent, pool)
            addPool(TweenPositionYComponent, pool)
            addPool(TweenMotionVelocityXComponent, pool)
            addPool(TweenRgbaAlphaComponent, pool)
            addPool(TweenRgbaTintComponent, pool)
            addPool(TweenSpawnerIntervalComponent, pool)
            addPool(TweenSpawnerNumberOfObjectsComponent, pool)
            addPool(TweenSpawnerPositionVariationComponent, pool)
            addPool(TweenSpawnerTimeVariationComponent, pool)
            addPool(TweenSpriteAnimationComponent, pool)
            addPool(TweenSpriteDirectionComponent, pool)
            addPool(TweenSpriteDestroyOnPlayingFinishedComponent, pool)
            addPool(TweenSpriteRunningComponent, pool)
//            addPool(TweenLifeCycleHealthCounter, pool)
            addPool(TweenSwitchLayerVisibilityOnVarianceComponent, pool)
            addPool(TweenSwitchLayerVisibilityOffVarianceComponent, pool)
            addPool(TweenSoundStartTriggerComponent, pool)
            addPool(TweenSoundStopTriggerComponent, pool)
            addPool(TweenSoundPositionComponent, pool)
            addPool(TweenSoundVolumeComponent, pool)
//            addPool(TweenNoisyMoveX, pool)
//            addPool(TweenNoisyMoveY, pool)
            addPool(TweenTextFieldTextComponent, pool)
            addPool(TweenTextFieldTextRangeStartComponent, pool)
            addPool(TweenTextFieldTextRangeEndComponent, pool)
            addPool(TweenEventPublishComponent, pool)
            addPool(TweenEventResetComponent, pool)
            addPool(TweenEventSubscribeComponent, pool)
            addPool(TweenTouchInputEnableComponent, pool)
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): TweenProperty =
        // All component types share the same pool - so just use the first component type to get it
        getPoolable(TweenPositionOffsetXComponent).apply { init(from = this@TweenProperty ) }

    // Cleanup/Reset the component automatically when it is removed from an entity (component will be returned to the pool eventually)
    override fun World.cleanupComponent(entity: Entity) { cleanup() }

    /**
     * All final [TweenProperty] names are organized in this enum. This is done to easily serialize the
     * [property](TweenComponent.property) of the base [TweenPropertyComponent] data class.
     */
    enum class TweenPropertyType(val type: ComponentType<TweenProperty>) {
        PositionOffsetX(componentTypeOf<TweenProperty>()),
        PositionOffsetY(componentTypeOf<TweenProperty>()),
        PositionX(componentTypeOf<TweenProperty>()),
        PositionY(componentTypeOf<TweenProperty>()),

        MotionVelocityX(componentTypeOf<TweenProperty>()),

        RgbaAlpha(componentTypeOf<TweenProperty>()),
        RgbaTint(componentTypeOf<TweenProperty>()),

        SpawnerInterval(componentTypeOf<TweenProperty>()),
        SpawnerNumberOfObjects(componentTypeOf<TweenProperty>()),
        SpawnerPositionVariation(componentTypeOf<TweenProperty>()),
        SpawnerTimeVariation(componentTypeOf<TweenProperty>()),

        SpriteRunning(componentTypeOf<TweenProperty>()),
        SpriteDirection(componentTypeOf<TweenProperty>()),
        SpriteDestroyOnPlayingFinished(componentTypeOf<TweenProperty>()),
        SpriteAnimation(componentTypeOf<TweenProperty>()),

        // TODO not used yet in animation system
//        LifeCycleHealthCounter(componentTypeOf<TweenProperty>()),

        SwitchLayerVisibilityOnVariance(componentTypeOf<TweenProperty>()),
        SwitchLayerVisibilityOffVariance(componentTypeOf<TweenProperty>()),

        SoundStartTrigger(componentTypeOf<TweenProperty>()),
        SoundStopTrigger(componentTypeOf<TweenProperty>()),
        SoundPosition(componentTypeOf<TweenProperty>()),
        SoundVolume(componentTypeOf<TweenProperty>()),

//        NoisyMoveX(componentTypeOf<TweenProperty>()),
//        NoisyMoveY(componentTypeOf<TweenProperty>()),

        TextFieldText(componentTypeOf<TweenProperty>()),
        TextFieldTextRangeStart(componentTypeOf<TweenProperty>()),
        TextFieldTextRangeEnd(componentTypeOf<TweenProperty>()),

        EventPublish(componentTypeOf<TweenProperty>()),
        EventReset(componentTypeOf<TweenProperty>()),
        EventSubscribe(componentTypeOf<TweenProperty>()),

        TouchInputEnable(componentTypeOf<TweenProperty>())
    }
}
