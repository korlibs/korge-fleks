package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.components.TweenPropertyComponent.TweenProperty
import korlibs.korge.fleks.components.TweenPropertyComponent.TweenProperty.*
import korlibs.korge.fleks.components.TweenSequenceComponent.*
import korlibs.korge.fleks.components.RgbaComponent.Rgb
import korlibs.korge.fleks.entity.EntityFactory
import korlibs.math.interpolation.Easing

/**
 * This system creates Animate... components on entities which should be animated according to the game config.
 */
class TweenSequenceSystem : IteratingSystem(
    family { all(TweenSequenceComponent) },
    // Make this fixed to not waste time if more frames are drawn per second than objects generated (from SpawnerSystem)
    interval = Fixed(1f / 60f)
) {
    // Internally used variables in createAnimateComponent function
    private lateinit var currentTween: TweenBase
    private lateinit var currentParentTween: ParallelTweens
    private val defaultTweenValues = ParallelTweens() // <-- gives default values for delay, duration and easing

    /**
     * When the system is called it checks for the [TweenSequenceComponent] component if the waitTime is over.
     * If yes, then it checks the tween at index in the tweens array which type it has.
     *
     * - On [SpawnNewTweenSequence] type the system creates a new [TweenSequenceComponent] Entity and configures it to operate on the sub-script.
     *   Then it checks all next steps and if they are an AnimationScript it spawns new AnimationScript Entities for each sub-script.
     *   The first next step which is an AnimationStep stops checking and configures the current script with the next waitTime from the next
     *   to be executed animation step. If the array of steps comes to the end before a new AnimationStep is found than the current
     *   AnimationScript Entity is destroyed. The animation of that script is finished.
     *
     * - On [ParallelTweens] type it is first checked if the animation is already active. If not then the waitTime is set to the
     *   duration of the [ParallelTweens] component and all specified animation component (e.g. [RgbaAlpha] and [PositionX]) are created for
     *   each specified sub-entity. If the animation is already active than that means that the duration of the [ParallelTweens] is over. In
     *   that case it is checked if a next step is available in the steps array. In case the next step is an AnimationStep than the waitTime
     *   is set to the delay of the next animation step.
     *
     * TODO: next is to be checked
     * Then it checks for the next animation step if the delay time was reached and creates or updates the corresponding animate component.
     * It updates the wait-time with the duration of the animation step. After finishing the animation step the delay of the next step is
     * loaded as wait-time.
     * Finally, it checks if the animation script has reached the end. If so it removes the AnimationScript component again from the entity.
     */
    override fun onTickEntity(entity: Entity) {
        val tweenSequence = entity[TweenSequenceComponent]

        if (tweenSequence.timeProgress >= tweenSequence.waitTime) {
            tweenSequence.timeProgress = 0f

            val currentTween: TweenBase =
                if (tweenSequence.index >= tweenSequence.tweens.size) {
                    // No further tweens -> remove TweenSequence component
                    entity.configure { it -= TweenSequenceComponent }
                    return
                } else if (tweenSequence.executed) {
                    // Tween was executed lately -> read next tween from the script
                    tweenSequence.executed = false
                    tweenSequence.index++
                    // Check if script of tweens has finished
                    if (tweenSequence.index >= tweenSequence.tweens.size) {
                        entity.configure { it -= TweenSequenceComponent }
                        return
                    }
                    // check for initial delay of new tween
                    val currentTween = tweenSequence.tweens[tweenSequence.index]
                    tweenSequence.waitTime = currentTween.delay ?: 0f
                    if (tweenSequence.waitTime != 0f)
                        return
                    currentTween
                } else tweenSequence.tweens[tweenSequence.index]

            tweenSequence.executed = true
            tweenSequence.waitTime = currentTween.duration ?: 0f

            when (currentTween) {
                is SpawnNewTweenSequence -> {
                    world.entity { it += TweenSequenceComponent(tweens = currentTween.tweens) }
                }
                is ParallelTweens -> {
                    currentTween.tweens.forEach { tween ->
                        if (tween.delay != null && tween.delay!! > 0f) {
                            // Tween has its own delay -> spawn a new TweenSequence for it
                            world.entity {
                                it += TweenSequenceComponent(
                                    tweens = listOf(
                                        // Put the tween into a new TweenSequence which runs independently of the parent TweenSequence
                                        tween.also { tween ->
                                            if (tween.duration == null) tween.duration = currentTween.duration ?: 0f
                                            if (tween.easing == null) tween.easing = currentTween.easing ?: Easing.LINEAR
                                        },
                                        // After finish the tween delete the entity again - it is not needed anymore
                                        DeleteEntity(entity = it)
                                    )
                                )
                            }
                        } else {
                            // No delay -> run it directly
                            checkTween(tween, currentTween)
                        }
                    }
                }
                // In case of "Wait" the duration was already set above
                else -> {
                    if (currentTween !is Wait) checkTween(currentTween, defaultTweenValues)
                }
            }
        }
        else tweenSequence.timeProgress += deltaTime
    }

    private fun checkTween(tween: TweenBase, parentTween: ParallelTweens) {
        currentTween = tween
        currentParentTween = parentTween
        when (tween) {
            is TweenRgba -> tween.entity.getOrWarning(RgbaComponent)?.let { start ->
                tween.alpha?.let { end -> createTweenPropertyComponent(RgbaAlpha, value = start.alpha, change = end - start.alpha) }
                tween.tint?.let { end ->  createTweenPropertyComponent(RgbaTint, start.tint,
                    Rgb(r = end.r - (start.tint.r), g = end.g - (start.tint.g), b = end.b - (start.tint.b))
                ) }
                tween.visible?.let { visible -> createTweenPropertyComponent(RgbaAlpha, value = if (visible) 1f else 0f, change = 0f) }
            }
            is TweenPosition -> tween.entity.getOrWarning(PositionComponent)?.let { start ->
                tween.x?.let { end -> createTweenPropertyComponent(PositionX, start.x, end - start.x) }
                tween.y?.let { end -> createTweenPropertyComponent(PositionY, start.y, end - start.y) }
                tween.offsetX?.let { end -> createTweenPropertyComponent(PositionOffsetX, start.offsetX, end - start.offsetX) }
                tween.offsetY?.let { end -> createTweenPropertyComponent(PositionOffsetY, start.offsetY, end - start.offsetY) }
            }
            is TweenSprite -> tween.entity.getOrWarning(SpriteComponent)?.let { _ ->  // make sure to-be-animated-entity is of type sprite
                tween.animation?.let { value -> createTweenPropertyComponent(SpriteAnimation, value) }
                tween.running?.let { value -> createTweenPropertyComponent(SpriteRunning, value) }
                tween.direction?.let { value -> createTweenPropertyComponent(SpriteDirection, value) }
                tween.destroyOnPlayingFinished?.let { value -> createTweenPropertyComponent(SpriteDestroyOnPlayingFinished, value) }
            }
            is TweenSwitchLayerVisibility -> tween.entity.getOrWarning(SwitchLayerVisibilityComponent)?.let { start ->
                tween.offVariance?.let { end -> createTweenPropertyComponent(SwitchLayerVisibilityOffVariance, value = start.offVariance, change = end - start.offVariance) }
                tween.onVariance?.let { end -> createTweenPropertyComponent(SwitchLayerVisibilityOnVariance, start.onVariance, end - start.onVariance) }
            }
            is TweenSpawner -> tween.entity.getOrWarning(SpawnerComponent)?.let { start ->
                tween.numberOfObjects?.let { end -> createTweenPropertyComponent(SpawnerNumberOfObjects, start.numberOfObjects, end - start.numberOfObjects) }
                tween.interval?.let { end -> createTweenPropertyComponent(SpawnerInterval, start.interval, end - start.interval) }
                tween.timeVariation?.let { end -> createTweenPropertyComponent(SpawnerTimeVariation, start.timeVariation, end - start.timeVariation) }
                tween.positionVariation?.let { end -> createTweenPropertyComponent(SpawnerPositionVariation, start.positionVariation, end - start.positionVariation) }
            }
            is TweenSound -> tween.entity.getOrWarning(SoundComponent)?.let{ start ->
                tween.startTrigger?.let { value -> createTweenPropertyComponent(SoundStartTrigger, value) }
                tween.stopTrigger?.let { value -> createTweenPropertyComponent(SoundStopTrigger, value) }
                tween.position?.let { end -> createTweenPropertyComponent(SoundPosition, start.position, end - start.position) }
                tween.volume?.let { end -> createTweenPropertyComponent(SoundVolume, start.volume, end - start.volume) }
            }
            is TweenTextField -> tween.entity.getOrWarning(TextFieldComponent)?.let { start ->
                tween.text?.let { value -> createTweenPropertyComponent(TextFieldText, value) }
                tween.textRangeStart?.let { end -> createTweenPropertyComponent(TextFieldTextRangeStart, start.textRangeStart, end - start.textRangeStart ) }
                tween.textRangeEnd?.let { end -> createTweenPropertyComponent(TextFieldTextRangeEnd, start.textRangeEnd, end - start.textRangeEnd ) }
            }
            // Creates a new entity (or uses the given entity from the tween) and configures it by running the config-function
            is SpawnEntity -> {
                val spawnedEntity = if (tween.entity == Entity.NONE) world.entity() else tween.entity
                EntityFactory.createEntity(tween.entityConfig, world, spawnedEntity)
            }
            // Directly deletes the given entity from the tween
            is DeleteEntity -> tween.entity.configure { entityToDelete -> world -= entityToDelete }
            // Runs the config-function on the given entity from the tween
            is ExecuteConfigFunction -> EntityFactory.createEntity(tween.entityConfig, world, tween.entity)
            else -> {
                when (tween) {
                    is SpawnNewTweenSequence -> println("WARNING - TweenSequenceSystem: \"SpawnNewTweenSequence\" not allowed in ParallelTweens")
                    is Wait -> println("WARNING - TweenSequenceSystem: \"Wait\" not allowed in ParallelTweens")
                    else -> println("WARNING - TweenSequenceSystem: Animate function for tween $tween not implemented!")
                }
            }
        }
    }

    private fun createTweenPropertyComponent(componentProperty: TweenProperty, value: Any, change: Any = Unit) {
        currentTween.entity.configure { animatedEntity ->
            animatedEntity.getOrAdd(componentProperty.type) { TweenPropertyComponent(componentProperty) }.also {
                it.change = change
                it.value = value
                it.duration = currentTween.duration ?: currentParentTween.duration ?: 0f
                it.timeProgress = 0f
                it.easing = currentTween.easing ?: currentParentTween.easing ?: Easing.LINEAR
            }
        }
    }

    private inline fun <reified T : Component<*>> Entity.getOrWarning(componentType: ComponentType<T>) : T? {
        if (this has componentType) return get(componentType)
        else {
            println("WARNING - TweenSequenceSystem: Entity '${this.id}' does not contain component type '${componentType}'!\n" +
                "Entity snapshot: \n${world.snapshotOf(this)}")
            return null
        }
    }
}
