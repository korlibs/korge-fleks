package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenPositionXComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenPositionYComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenOffsetXComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenOffsetYComponent
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenRgbaAlphaComponent
import korlibs.korge.fleks.components.TweenPropertyComponent.Companion.TweenRgbaTintComponent
import korlibs.korge.fleks.components.RgbaComponent.Rgb
import kotlin.jvm.JvmName
import kotlin.reflect.KMutableProperty0

/**
 * This system is configured to work on a combination of one target and one or multiple "Animate" components (e.g. Drawable and AnimateDrawableAlpha or AnimateDrawableTint).
 * The "Animate" component will animate fields of the target component.
 *
 * Thus, for starting an animation for an entity it is sufficient to add the desired "Animate" component to the entity.
 * When the animation is over than the Animate component is removed again from the entity.
 * Adding Animate components can be done e.g. by the AnimationSequence Entity Component configuration.
 */
class TweenRgbaSystem : IteratingSystem(
    family { all(RgbaComponent).any(TweenRgbaAlphaComponent, TweenRgbaTintComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        entity.getOrNull(RgbaComponent)?.let {
            updateProperty(entity, TweenRgbaAlphaComponent, it::alpha)
            updateProperty(entity, TweenRgbaTintComponent, it::tint)
        }
    }
}

//class AnimateSwitchLayerVisibilitySystem : IteratingSystem(
//    family { all(SwitchLayerVisibilityComponent).any(TweenSwitchLayerVisibilityOnVariance, TweenSwitchLayerVisibilityOffVariance) },
//    interval = EachFrame
//) {
//    override fun onTickEntity(entity: Entity) {
//        val visibility = entity[SwitchLayerVisibilityComponent]
//        updateProperty(entity, TweenSwitchLayerVisibilityOnVariance, visibility::offVariance)
//        updateProperty(entity, TweenSwitchLayerVisibilityOffVariance, visibility::onVariance)
//    }
//}

class TweenPositionSystem : IteratingSystem(
    family { any(TweenPositionXComponent, TweenPositionYComponent, TweenOffsetXComponent, TweenOffsetYComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        entity.getOrNull(PositionComponent)?.let {
            updateProperty(entity, TweenPositionXComponent, it::x)
            updateProperty(entity, TweenPositionYComponent, it::y)
        }
        entity.getOrNull(OffsetComponent)?.let {
            updateProperty(entity, TweenOffsetXComponent, it::x)
            updateProperty(entity, TweenOffsetYComponent, it::y)
        }
    }
}

//class TweenSpriteSystem : IteratingSystem(
//    family { all(SpriteComponent).any(TweenSpriteAnimName, TweenSpriteIsPlaying, TweenSpriteForwardDirection, TweenSpriteLoop, TweenSpriteDestroyOnPlayingFinished) },
//    interval = EachFrame
//) {
//    override fun onTickEntity(entity: Entity) {
//        val sprite = entity[SpriteComponent]
//        val imageView = KorgeViewCache[entity] as ImageDataViewEx
//        updateProperty(entity, TweenSpriteAnimName, sprite::animationName) { imageView.animation = sprite.animationName }
//        updateProperty(entity, TweenSpriteIsPlaying, sprite::isPlaying)
//        updateProperty(entity, TweenSpriteForwardDirection, sprite::forwardDirection)
//        updateProperty(entity, TweenSpriteLoop, sprite::loop)
//        updateProperty(entity, TweenSpriteDestroyOnPlayingFinished, sprite::destroyOnPlayingFinished) {
// TODO
//            if (sprite.destroyOnPlayingFinished)
//            imageView.onPlayFinished = { entity.getOrAdd(LifeCycleComponent) { LifeCycleComponent() }.also { lifeCycle -> lifeCycle.healthCounter = 0 } }
//            else
//            imageView.onPlayFinished = {}
//        }
//
//        if (sprite.isPlaying) imageView.play(reverse = !sprite.forwardDirection, once = !sprite.loop)
//    }
//}

//class AnimateSpawnerSystem : IteratingSystem(
//    family { all(SpawnerComponent).any(TweenSpawnerNumberOfObjects, TweenSpawnerInterval, TweenSpawnerTimeVariation, TweenSpawnerPositionVariation) },
//    interval = EachFrame
//) {
//    override fun onTickEntity(entity: Entity) {
//        val spawner = entity[SpawnerComponent]
//        updateProperty(entity, TweenSpawnerNumberOfObjects, spawner::numberOfObjects)
//        updateProperty(entity, TweenSpawnerInterval, spawner::interval) {
//            // Reset next spawn counter so that changed interval will be taken into account instantly
//            spawner.nextSpawnIn = 0
//        }
//        updateProperty(entity, TweenSpawnerTimeVariation, spawner::timeVariation)
//        updateProperty(entity, TweenSpawnerPositionVariation, spawner::positionVariation)
//    }
//}

//class AnimateLifeCycleSystem : IteratingSystem(
//    family { all(LifeCycleComponent).any(TweenLifeCycleHealthCounter) },
//    interval = EachFrame
//) {
//    override fun onTickEntity(entity: Entity) {
//        val lifeCycle = entity[LifeCycleComponent]
//        updateProperty(entity, TweenLifeCycleHealthCounter, lifeCycle::healthCounter)
//    }
//}

//class AnimateSoundSystem : IteratingSystem(
//    family { all(SoundComponent).any(TweenSoundStartTrigger, TweenSoundStopTrigger, TweenSoundPosition, TweenSoundVolume) },
//    interval = EachFrame
//) {
//    override fun onTickEntity(entity: Entity) {
//        val sound = entity[SoundComponent]
//        updateProperty(entity, TweenSoundStartTrigger, sound::startTrigger)
//        updateProperty(entity, TweenSoundStopTrigger, sound::stopTrigger)
//        updateProperty(entity, TweenSoundPosition, sound::position)
//        updateProperty(entity, TweenSoundVolume, sound::volume)
//    }
//}

/**
 *
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
            val time: Float = (it.timeProgress / it.duration).toFloat()
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
