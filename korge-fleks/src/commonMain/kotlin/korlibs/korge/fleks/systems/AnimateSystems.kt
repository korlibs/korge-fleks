package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateAppearanceAlpha
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateAppearanceTint
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateAppearanceVisible
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateSwitchLayerVisibilityOnVariance
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateSwitchLayerVisibilityOffVariance
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimatePositionShapeX
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimatePositionShapeY
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateSpriteAnimName
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateSpriteIsPlaying
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateSpriteForwardDirection
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateSpriteLoop
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateSpriteDestroyOnPlayingFinished
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateSpawnerNumberOfObjects
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateSpawnerInterval
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateSpawnerTimeVariation
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateSpawnerPositionVariation
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateLifeCycleHealthCounter
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateOffsetX
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateOffsetY
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateSoundPosition
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateSoundStartTrigger
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateSoundStopTrigger
import korlibs.korge.fleks.components.AnimateComponent.Companion.AnimateSoundVolume
import korlibs.korge.fleks.utils.KorgeViewCache
import korlibs.korge.fleks.components.*
import korlibs.korge.parallax.ImageDataViewEx
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
class AnimateAppearanceSystem : IteratingSystem(
    family { all(Appearance).any(AnimateAppearanceAlpha, AnimateAppearanceTint, AnimateAppearanceVisible) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val appearance = entity[Appearance]
        updateProperty(entity, AnimateAppearanceAlpha, appearance::alpha)
        updateProperty(entity, AnimateAppearanceTint, appearance::tint)
        updateProperty(entity, AnimateAppearanceVisible, appearance::visible)
    }
}

class AnimateSwitchLayerVisibilitySystem : IteratingSystem(
    family { all(SwitchLayerVisibility).any(AnimateSwitchLayerVisibilityOnVariance, AnimateSwitchLayerVisibilityOffVariance) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val visibility = entity[SwitchLayerVisibility]
        updateProperty(entity, AnimateSwitchLayerVisibilityOnVariance, visibility::offVariance)
        updateProperty(entity, AnimateSwitchLayerVisibilityOffVariance, visibility::onVariance)
    }
}

class AnimatePositionShapeSystem : IteratingSystem(
    family { any(AnimatePositionShapeX, AnimatePositionShapeY, AnimateOffsetX, AnimateOffsetY) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        entity.getOrNull(PositionShape)?.let {
            updateProperty(entity, AnimatePositionShapeX, it::x)
            updateProperty(entity, AnimatePositionShapeY, it::y)
        }
        entity.getOrNull(Offset)?.let {
            updateProperty(entity, AnimateOffsetX, it::x)
            updateProperty(entity, AnimateOffsetY, it::y)
        }
    }
}

class AnimateSpriteSystem(
    private val korgeViewCache: KorgeViewCache = inject("normalViewCache")
) : IteratingSystem(
    family { all(Sprite).any(AnimateSpriteAnimName, AnimateSpriteIsPlaying, AnimateSpriteForwardDirection, AnimateSpriteLoop, AnimateSpriteDestroyOnPlayingFinished) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val sprite = entity[Sprite]
        val imageView = korgeViewCache[entity] as ImageDataViewEx
        updateProperty(entity, AnimateSpriteAnimName, sprite::animationName) { imageView.animation = sprite.animationName }
        updateProperty(entity, AnimateSpriteIsPlaying, sprite::isPlaying)
        updateProperty(entity, AnimateSpriteForwardDirection, sprite::forwardDirection)
        updateProperty(entity, AnimateSpriteLoop, sprite::loop)
        updateProperty(entity, AnimateSpriteDestroyOnPlayingFinished, sprite::destroyOnPlayingFinished) {
            if (sprite.destroyOnPlayingFinished)
            imageView.onPlayFinished = { entity.getOrAdd(LifeCycle) { LifeCycle() }.also { lifeCycle -> lifeCycle.healthCounter = 0 } }
            else
            imageView.onPlayFinished = {}
        }

        if (sprite.isPlaying) imageView.play(reverse = !sprite.forwardDirection, once = !sprite.loop)
    }
}

class AnimateSpawnerSystem : IteratingSystem(
    family { all(Spawner).any(AnimateSpawnerNumberOfObjects, AnimateSpawnerInterval, AnimateSpawnerTimeVariation, AnimateSpawnerPositionVariation) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val spawner = entity[Spawner]
        updateProperty(entity, AnimateSpawnerNumberOfObjects, spawner::numberOfObjects)
        updateProperty(entity, AnimateSpawnerInterval, spawner::interval) {
            // Reset next spawn counter so that changed interval will be taken into account instantly
            spawner.nextSpawnIn = 0
        }
        updateProperty(entity, AnimateSpawnerTimeVariation, spawner::timeVariation)
        updateProperty(entity, AnimateSpawnerPositionVariation, spawner::positionVariation)
    }
}

class AnimateLifeCycleSystem : IteratingSystem(
    family { all(LifeCycle).any(AnimateLifeCycleHealthCounter) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val lifeCycle = entity[LifeCycle]
        updateProperty(entity, AnimateLifeCycleHealthCounter, lifeCycle::healthCounter)
    }
}

class AnimateSoundSystem : IteratingSystem(
    family { all(Sound).any(AnimateSoundStartTrigger, AnimateSoundStopTrigger, AnimateSoundPosition, AnimateSoundVolume) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val sound = entity[Sound]
        updateProperty(entity, AnimateSoundStartTrigger, sound::startTrigger)
        updateProperty(entity, AnimateSoundStopTrigger, sound::stopTrigger)
        updateProperty(entity, AnimateSoundPosition, sound::position)
        updateProperty(entity, AnimateSoundVolume, sound::volume)
    }
}

/**
 *
 */
@JvmName("updatePropertyDouble")
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<AnimateComponent>, value: KMutableProperty0<Double>) {
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
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<AnimateComponent>, value: KMutableProperty0<Float>) {
    entity.getOrNull(component)?.let {
        // Check if time of animation sequence is over - then we can remove the animation component again
        if (it.timeProgress >= it.duration) entity.configure { entity ->
            value.set(it.change as Float + it.value as Float)
            entity -= component  // remove component from entity
        } else {
            // Calculate new value for the animated property
            val time: Float = it.timeProgress / it.duration
            value.set(it.change as Float * it.easing.invoke(time) + it.value as Float)
            // Check if time of animation sequence is over - then we can remove the animation component again
            it.timeProgress += deltaTime
        }
    }
}

@JvmName("updatePropertyInt")
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<AnimateComponent>, value: KMutableProperty0<Int>, block: EntityUpdateContext.() -> Unit = {}) {
    entity.getOrNull(component)?.let {
        if (it.timeProgress >= it.duration) entity.configure { entity ->
            value.set(it.change as Int + it.value as Int)
            block.invoke(this)
            entity -= component
        } else {
            val time = it.timeProgress / it.duration
            value.set(((it.change as Int).toFloat() * it.easing.invoke(time)).toInt() + it.value as Int)
            it.timeProgress += deltaTime
        }
    }
}

@JvmName("updatePropertyRgb")
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<AnimateComponent>, value: KMutableProperty0<Rgb?>) {
    entity.getOrNull(component)?.let {
        if (it.timeProgress >= it.duration) entity.configure { entity ->
            value.set(it.change as Rgb + it.value as Rgb)
            entity -= component
        } else {
            val time = it.timeProgress / it.duration
            value.set(it.change as Rgb * it.easing.invoke(time) + it.value as Rgb)
            it.timeProgress += deltaTime
        }
    }

}

@JvmName("updatePropertyBoolean")
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<AnimateComponent>, value: KMutableProperty0<Boolean>, block: EntityUpdateContext.() -> Unit = {}) {
    entity.getOrNull(component)?.let {
        if (it.timeProgress >= it.duration || it.easing.invoke((it.timeProgress / it.duration)) > 0.5) entity.configure { entity ->
            value.set(it.value as Boolean)
            block.invoke(this)
            entity -= component
        }
    }
}

@JvmName("updatePropertyString?")
fun IteratingSystem.updateProperty(entity: Entity, component: ComponentType<AnimateComponent>, value: KMutableProperty0<String?>, block: EntityUpdateContext.() -> Unit = {}) {
    entity.getOrNull(component)?.let {
        if (it.timeProgress >= it.duration || it.easing.invoke((it.timeProgress / it.duration)) > 0.5) entity.configure { entity ->
            value.set(it.value as String)
            block.invoke(this)
            entity -= component
        }
    }
}
