package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.image.color.RGBA
import korlibs.image.format.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Rgba.Companion.RgbaComponent
import korlibs.korge.fleks.components.Sound.Companion.SoundComponent
import korlibs.korge.fleks.components.Spawner.Companion.SpawnerComponent
import korlibs.korge.fleks.components.Sprite.Companion.SpriteComponent
import korlibs.korge.fleks.components.SwitchVisibility.Companion.SwitchVisibilityComponent
import korlibs.korge.fleks.components.TextField.Companion.TextFieldComponent
import korlibs.korge.fleks.components.TouchInput.Companion.TouchInputComponent
import korlibs.korge.fleks.components.TweenProperty
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenPositionXComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenPositionYComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenPositionOffsetXComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenPositionOffsetYComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenRgbaAlphaComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenRgbaRedComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenMotionVelocityXComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenRgbaBlueComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenRgbaGreenComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSoundPositionComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSoundStartTriggerComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSoundStopTriggerComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSoundVolumeComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSpawnerIntervalComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSpawnerNumberOfObjectsComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSpawnerPositionVariationComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSpawnerTimeVariationComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSpriteAnimationComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSpriteDestroyOnPlayingFinishedComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSpriteDirectionComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSpriteRunningComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSwitchVisibilityOffVarianceComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenSwitchVisibilityOnVarianceComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenTextFieldTextComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenTextFieldTextRangeEndComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenTextFieldTextRangeStartComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.TweenTouchInputEnableComponent
import korlibs.korge.fleks.components.TweenSequence.Companion.TweenSequenceComponent
import kotlin.jvm.JvmName
import kotlin.math.roundToInt
import kotlin.reflect.KMutableProperty0

/**
 * All systems are configured to work on a combination of one target and one or multiple "Tween" components
 * (e.g. PositionComponent and TweenPositionXComponent or TweenPositionYComponent).
 * The "Tween" component will animate fields of the target component.
 *
 * Thus, for starting an animation for an entity it is sufficient to add the desired "Tween" component to the entity.
 * When the animation is over than the Animate component is removed again from the entity.
 * Adding "Tween" components can be done e.g. by the [TweenSequenceComponent] configuration.
 */
class TweenPositionSystem : IteratingSystem(
    family {
        all(PositionComponent)
            .any(TweenPositionXComponent, TweenPositionYComponent, TweenPositionOffsetXComponent, TweenPositionOffsetYComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val positionComponent = entity[PositionComponent]
        updateProperty(entity, TweenPositionXComponent, positionComponent::x)
        updateProperty(entity, TweenPositionYComponent, positionComponent::y)
        updateProperty(entity, TweenPositionOffsetXComponent, positionComponent::offsetX)
        updateProperty(entity, TweenPositionOffsetYComponent, positionComponent::offsetY)
    }
}

class TweenMotionSystem : IteratingSystem(
    family {
        all(MotionComponent)
            .any(TweenMotionVelocityXComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val motionComponent = entity[MotionComponent]
        updateProperty(entity, TweenMotionVelocityXComponent, motionComponent::velocityX)
    }
}

class TweenRgbaSystem : IteratingSystem(
    family { all(RgbaComponent).any(TweenRgbaAlphaComponent, TweenRgbaRedComponent, TweenRgbaGreenComponent, TweenRgbaBlueComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val rgbaComponent = entity[RgbaComponent]
        updateProperty(entity, TweenRgbaAlphaComponent, rgbaComponent::alpha)
        updateProperty(entity, TweenRgbaRedComponent, rgbaComponent::r)
        updateProperty(entity, TweenRgbaGreenComponent, rgbaComponent::g)
        updateProperty(entity, TweenRgbaBlueComponent, rgbaComponent::b)
    }
}

class TweenSpawnerSystem : IteratingSystem(
    family {
        all(SpawnerComponent)
            .any(TweenSpawnerIntervalComponent, TweenSpawnerNumberOfObjectsComponent, TweenSpawnerTimeVariationComponent, TweenSpawnerPositionVariationComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val spawnerComponent = entity[SpawnerComponent]
        updateProperty(entity, TweenSpawnerNumberOfObjectsComponent, spawnerComponent::numberOfObjects)
        updateProperty(entity, TweenSpawnerIntervalComponent, spawnerComponent::interval) {
            // Reset next spawn counter so that changed interval will be taken into account instantly
            spawnerComponent.nextSpawnIn = 0
        }
        updateProperty(entity, TweenSpawnerTimeVariationComponent, spawnerComponent::timeVariation)
        updateProperty(entity, TweenSpawnerPositionVariationComponent, spawnerComponent::positionVariation)
    }
}

class TweenSpriteSystem : IteratingSystem(
    family {
        all(SpriteComponent)
            .any(TweenSpriteAnimationComponent, TweenSpriteRunningComponent, TweenSpriteDirectionComponent, TweenSpriteDestroyOnPlayingFinishedComponent) },
    interval = EachFrame
) {
    private val assetStore: AssetStore = world.inject(name = "AssetStore")

    override fun onTickEntity(entity: Entity) {
        val spriteComponent = entity[SpriteComponent]
        updateProperty(entity, TweenSpriteAnimationComponent, spriteComponent::name) {
            spriteComponent.setFrameIndex(assetStore)
            spriteComponent.setNextFrameIn(assetStore)
        }
        updateProperty(entity, TweenSpriteDirectionComponent, spriteComponent::direction) {
            spriteComponent.setFrameIndex(assetStore)
            spriteComponent.setIncrement()
        }
        updateProperty(entity, TweenSpriteRunningComponent, spriteComponent::running) {
            spriteComponent.setIncrement()
        }
        updateProperty(entity, TweenSpriteDestroyOnPlayingFinishedComponent, spriteComponent::destroyOnAnimationFinished)
    }
}

class TweenSwitchVisibilitySystem : IteratingSystem(
    family {
        all(SwitchVisibilityComponent)
            .any(TweenSwitchVisibilityOnVarianceComponent, TweenSwitchVisibilityOffVarianceComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val visibilityComponent = entity[SwitchVisibilityComponent]
        updateProperty(entity, TweenSwitchVisibilityOffVarianceComponent, visibilityComponent::offVariance)
        updateProperty(entity, TweenSwitchVisibilityOnVarianceComponent, visibilityComponent::onVariance)
    }
}

class TweenSoundSystem : IteratingSystem(
    family {
        all(SoundComponent)
            .any(TweenSoundStartTriggerComponent, TweenSoundStopTriggerComponent, TweenSoundPositionComponent, TweenSoundVolumeComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val sound = entity[SoundComponent]
        updateProperty(entity, TweenSoundStartTriggerComponent, sound::startTrigger)
        updateProperty(entity, TweenSoundStopTriggerComponent, sound::stopTrigger)
        updateProperty(entity, TweenSoundPositionComponent, sound::position)
        updateProperty(entity, TweenSoundVolumeComponent, sound::volume)
    }
}

class TweenTextFieldSystem : IteratingSystem(
    family {
        all(TextFieldComponent)
            .any(TweenTextFieldTextComponent, TweenTextFieldTextRangeStartComponent, TweenTextFieldTextRangeEndComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val textFieldComponent = entity[TextFieldComponent]
        updateProperty(entity, TweenTextFieldTextComponent, textFieldComponent::text)
        updateProperty(entity, TweenTextFieldTextRangeStartComponent, textFieldComponent::textRangeStart)
        updateProperty(entity, TweenTextFieldTextRangeEndComponent, textFieldComponent::textRangeEnd)
    }
}

class TweenTouchInputSystem : IteratingSystem(
    family {
        all(TouchInputComponent)
            .any(TweenTouchInputEnableComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val touchInputComponent = entity[TouchInputComponent]
        updateProperty(entity, TweenTouchInputEnableComponent, touchInputComponent::enabled)
    }
}

//class AnimateLifeCycleSystem : IteratingSystem(
//    family { all(LifeCycleComponent).any(TweenLifeCycleHealthCounter) },
//    interval = EachFrame
//) {
//    override fun onTickEntity(entity: Entity) {
//        val lifeCycle = entity[LifeCycleComponent]
//        updateProperty(entity, TweenLifeCycleHealthCounter, lifeCycle::healthCounter)
//    }
//}

fun SystemConfiguration.addTweenEngineSystems() {
    // First add the tween sequence system - it creates TweenPropertyComponents
    add(TweenSequenceSystem())

    // Then add all tween animation systems - they update properties of components according to TweenPropertyComponents
    add(TweenRgbaSystem())
    add(TweenPositionSystem())
    add(TweenMotionSystem())
    add(TweenSpawnerSystem())
    add(TweenSpriteSystem())
    add(TweenSwitchVisibilitySystem())
    add(TweenSoundSystem())
    add(TweenTextFieldSystem())
    add(TweenTouchInputSystem())
}

/**
 * Below functions are updating the property of the to be animated component.
 */
@JvmName("updatePropertyDouble")
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<TweenProperty>, value: KMutableProperty0<Double>) {
    entity.getOrNull(component)?.let {
        // Check if time of animation sequence is over - then we can remove the animation component again
        if (it.timeProgress >= it.duration) entity.configure { entity ->
            value.set(it.change as Double + it.value as Double)
            entity -= component  // remove component from entity
        } else {
            // Calculate new value for the animated property
            val time: Float = it.timeProgress / it.duration
            value.set(it.change as Double * it.easing.invoke(time) + it.value as Double)
            // Check if time of animation sequence is over - then we can remove the animation component again
            it.timeProgress += deltaTime
        }
    }
}

@JvmName("updatePropertyFloat")
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<TweenProperty>, value: KMutableProperty0<Float>) {
    entity.getOrNull(component)?.let {
        // Check if time of animation sequence is over - then we can remove the animation component again
        if (it.timeProgress >= it.duration) entity.configure { entity ->
            value.set(it.change as Float + it.value as Float)
            entity -= component  // remove component from entity
        } else {
            // Calculate new value for the animated property
            val time: Float = (it.timeProgress / it.duration)
            value.set(it.change as Float * it.easing.invoke(time) + it.value as Float)
            // Check if time of animation sequence is over - then we can remove the animation component again
            it.timeProgress += deltaTime
        }
    }
}

@JvmName("updatePropertyInt")
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<TweenProperty>, value: KMutableProperty0<Int>, block: EntityUpdateContext.() -> Unit = {}) {
    entity.getOrNull(component)?.let {
        if (it.timeProgress >= it.duration) entity.configure { entity ->
            value.set(it.change as Int + it.value as Int)
            block.invoke(this)
            entity -= component
        } else {
            val time: Float = it.timeProgress / it.duration
            value.set(((it.change as Int).toFloat() * it.easing.invoke(time)).toInt() + it.value as Int)
            it.timeProgress += deltaTime
        }
    }
}

@JvmName("updatePropertyRgba")
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<TweenProperty>, value: KMutableProperty0<RGBA>) {
    entity.getOrNull(component)?.let {
        if (it.timeProgress >= it.duration) entity.configure { entity ->
            value.set(it.change as RGBA + it.value as RGBA)
            entity -= component
        } else {
            val time: Float = it.timeProgress / it.duration
            value.set(it.change as RGBA * it.easing.invoke(time) + it.value as RGBA)
            it.timeProgress += deltaTime
        }
    }
}

operator fun RGBA.times(f: Float) = RGBA(
    (r.toFloat() * f).roundToInt(),
    (g.toFloat() * f).roundToInt(),
    (b.toFloat() * f).roundToInt()
)

@JvmName("updatePropertyDirectionNullable")
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<TweenProperty>, value: KMutableProperty0<ImageAnimation.Direction>, block: EntityUpdateContext.() -> Unit = {}) {
    entity.getOrNull(component)?.let {
        if (it.timeProgress >= it.duration || it.easing.invoke((it.timeProgress / it.duration)) > 0.5) entity.configure { entity ->
            value.set(it.value as ImageAnimation.Direction)
            block.invoke(this)
            entity -= component
        }
    }
}

@JvmName("updatePropertyBoolean")
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<TweenProperty>, value: KMutableProperty0<Boolean>, block: EntityUpdateContext.() -> Unit = {}) {
    entity.getOrNull(component)?.let {
        if (it.timeProgress >= it.duration || it.easing.invoke((it.timeProgress / it.duration)) > 0.5) entity.configure { entity ->
            value.set(it.value as Boolean)
            block.invoke(this)
            entity -= component
        }
    }
}

@JvmName("updatePropertyStringNullable")
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<TweenProperty>, value: KMutableProperty0<String?>, block: EntityUpdateContext.() -> Unit = {}) {
    entity.getOrNull(component)?.let {
        if (it.timeProgress >= it.duration || it.easing.invoke((it.timeProgress / it.duration)) > 0.5) entity.configure { entity ->
            value.set(it.value as String)
            block.invoke(this)
            entity -= component
        }
    }
}

@JvmName("updatePropertyString")
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<TweenProperty>, value: KMutableProperty0<String>, block: EntityUpdateContext.() -> Unit = {}) {
    entity.getOrNull(component)?.let {
        if (it.timeProgress >= it.duration || it.easing.invoke((it.timeProgress / it.duration)) > 0.5) entity.configure { entity ->
            value.set(it.value as String)
            block.invoke(this)
            entity -= component
        }
    }
}
