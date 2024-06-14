package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.image.format.*
import korlibs.korge.assetmanager.*
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenPositionXComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenPositionYComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenPositionOffsetXComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenPositionOffsetYComponent
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenRgbaAlphaComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenRgbaTintComponent
import korlibs.korge.fleks.components.RgbaComponent.Rgb
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenSoundPositionComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenSoundStartTriggerComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenSoundStopTriggerComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenSoundVolumeComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenSpawnerIntervalComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenSpawnerNumberOfObjectsComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenSpawnerPositionVariationComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenSpawnerTimeVariationComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenSpriteAnimationComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenSpriteDestroyOnPlayingFinishedComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenSpriteDirectionComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenSpriteRunningComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenSwitchLayerVisibilityOffVarianceComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenSwitchLayerVisibilityOnVarianceComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenTextFieldTextComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenTextFieldTextRangeEndComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenTextFieldTextRangeStartComponent
import kotlin.jvm.JvmName
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

class TweenRgbaSystem : IteratingSystem(
    family { all(RgbaComponent).any(TweenRgbaAlphaComponent, TweenRgbaTintComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val rgbaComponent = entity[RgbaComponent]
        updateProperty(entity, TweenRgbaAlphaComponent, rgbaComponent::alpha)
        updateProperty(entity, TweenRgbaTintComponent, rgbaComponent::tint)
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
        updateProperty(entity, TweenSpriteAnimationComponent, spriteComponent::animation) {
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

class TweenSwitchLayerVisibilitySystem : IteratingSystem(
    family {
        all(SwitchLayerVisibilityComponent)
            .any(TweenSwitchLayerVisibilityOnVarianceComponent, TweenSwitchLayerVisibilityOffVarianceComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val visibilityComponent = entity[SwitchLayerVisibilityComponent]
        updateProperty(entity, TweenSwitchLayerVisibilityOffVarianceComponent, visibilityComponent::offVariance)
        updateProperty(entity, TweenSwitchLayerVisibilityOnVarianceComponent, visibilityComponent::onVariance)
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

//class AnimateLifeCycleSystem : IteratingSystem(
//    family { all(LifeCycleComponent).any(TweenLifeCycleHealthCounter) },
//    interval = EachFrame
//) {
//    override fun onTickEntity(entity: Entity) {
//        val lifeCycle = entity[LifeCycleComponent]
//        updateProperty(entity, TweenLifeCycleHealthCounter, lifeCycle::healthCounter)
//    }
//}

fun SystemConfiguration.setupTweenEngineSystems() {
    // First add the tween sequence system - it creates TweenPropertyComponents
    add(TweenSequenceSystem())

    // Then add all tween animation systems - they update properties of components according to TweenPropertyComponents
    add(TweenRgbaSystem())
    add(TweenPositionSystem())
    add(TweenSpawnerSystem())
    add(TweenSpriteSystem())
    add(TweenSwitchLayerVisibilitySystem())
    add(TweenSoundSystem())
    add(TweenTextFieldSystem())
}

/**
 * Below functions are updating the property of the to be animated component.
 */
@JvmName("updatePropertyDouble")
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<TweenPropertyComponent>, value: KMutableProperty0<Double>) {
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
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<TweenPropertyComponent>, value: KMutableProperty0<Float>) {
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
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<TweenPropertyComponent>, value: KMutableProperty0<Int>, block: EntityUpdateContext.() -> Unit = {}) {
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

@JvmName("updatePropertyRgb")
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<TweenPropertyComponent>, value: KMutableProperty0<Rgb>) {
    entity.getOrNull(component)?.let {
        if (it.timeProgress >= it.duration) entity.configure { entity ->
            value.set(it.change as Rgb + it.value as Rgb)
            entity -= component
        } else {
            val time: Float = it.timeProgress / it.duration
            value.set(it.change as Rgb * it.easing.invoke(time) + it.value as Rgb)
            it.timeProgress += deltaTime
        }
    }
}

@JvmName("updatePropertyDirectionNullable")
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<TweenPropertyComponent>, value: KMutableProperty0<ImageAnimation.Direction?>, block: EntityUpdateContext.() -> Unit = {}) {
    entity.getOrNull(component)?.let {
        if (it.timeProgress >= it.duration || it.easing.invoke((it.timeProgress / it.duration)) > 0.5) entity.configure { entity ->
            value.set(it.value as ImageAnimation.Direction)
            block.invoke(this)
            entity -= component
        }
    }
}

@JvmName("updatePropertyBoolean")
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<TweenPropertyComponent>, value: KMutableProperty0<Boolean>, block: EntityUpdateContext.() -> Unit = {}) {
    entity.getOrNull(component)?.let {
        if (it.timeProgress >= it.duration || it.easing.invoke((it.timeProgress / it.duration)) > 0.5) entity.configure { entity ->
            value.set(it.value as Boolean)
            block.invoke(this)
            entity -= component
        }
    }
}

@JvmName("updatePropertyStringNullable")
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<TweenPropertyComponent>, value: KMutableProperty0<String?>, block: EntityUpdateContext.() -> Unit = {}) {
    entity.getOrNull(component)?.let {
        if (it.timeProgress >= it.duration || it.easing.invoke((it.timeProgress / it.duration)) > 0.5) entity.configure { entity ->
            value.set(it.value as String)
            block.invoke(this)
            entity -= component
        }
    }
}

@JvmName("updatePropertyString")
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<TweenPropertyComponent>, value: KMutableProperty0<String>) {
    entity.getOrNull(component)?.let {
        if (it.timeProgress >= it.duration || it.easing.invoke((it.timeProgress / it.duration)) > 0.5) entity.configure { entity ->
            value.set(it.value as String)
            entity -= component
        }
    }
}
