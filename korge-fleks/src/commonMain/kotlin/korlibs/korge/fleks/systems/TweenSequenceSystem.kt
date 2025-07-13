package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Rgba.Companion.RgbaComponent
import korlibs.korge.fleks.components.Sound.Companion.SoundComponent
import korlibs.korge.fleks.components.Spawner.Companion.SpawnerComponent
import korlibs.korge.fleks.components.Sprite.Companion.SpriteComponent
import korlibs.korge.fleks.components.SwitchLayerVisibility.Companion.SwitchLayerVisibilityComponent
import korlibs.korge.fleks.components.TextField.Companion.TextFieldComponent
import korlibs.korge.fleks.components.TouchInput.Companion.TouchInputComponent
import korlibs.korge.fleks.components.TweenProperty.Companion.tweenPropertyComponent
import korlibs.korge.fleks.components.TweenProperty.TweenPropertyType
import korlibs.korge.fleks.components.TweenProperty.TweenPropertyType.*
import korlibs.korge.fleks.components.TweenSequence.Companion.TweenSequenceComponent
import korlibs.korge.fleks.components.TweenSequence.Companion.tweenSequenceComponent
import korlibs.korge.fleks.components.data.tweenSequence.DeleteEntity
import korlibs.korge.fleks.components.data.tweenSequence.DeleteEntity.Companion.deleteEntity
import korlibs.korge.fleks.components.data.tweenSequence.ExecuteConfigFunction
import korlibs.korge.fleks.components.data.tweenSequence.Jump
import korlibs.korge.fleks.components.data.tweenSequence.LoopTweens
import korlibs.korge.fleks.components.data.tweenSequence.ParallelTweens
import korlibs.korge.fleks.components.data.tweenSequence.ParallelTweens.Companion.staticParallelTweens
import korlibs.korge.fleks.components.data.tweenSequence.ResetEvent
import korlibs.korge.fleks.components.data.tweenSequence.SendEvent
import korlibs.korge.fleks.components.data.tweenSequence.SpawnEntity
import korlibs.korge.fleks.components.data.tweenSequence.SpawnNewTweenSequence
import korlibs.korge.fleks.components.data.tweenSequence.TweenBase
import korlibs.korge.fleks.components.data.tweenSequence.TweenMotion
import korlibs.korge.fleks.components.data.tweenSequence.TweenPosition
import korlibs.korge.fleks.components.data.tweenSequence.TweenRgba
import korlibs.korge.fleks.components.data.tweenSequence.TweenSound
import korlibs.korge.fleks.components.data.tweenSequence.TweenSpawner
import korlibs.korge.fleks.components.data.tweenSequence.TweenSprite
import korlibs.korge.fleks.components.data.tweenSequence.TweenSwitchLayerVisibility
import korlibs.korge.fleks.components.data.tweenSequence.TweenTextField
import korlibs.korge.fleks.components.data.tweenSequence.TweenTouchInput
import korlibs.korge.fleks.components.data.tweenSequence.Wait
import korlibs.korge.fleks.components.data.tweenSequence.init
import korlibs.korge.fleks.entity.EntityFactory
import korlibs.korge.fleks.utils.*
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
    private val defaultTweenValues: ParallelTweens = staticParallelTweens {} // <-- gives default values for delay, duration and easing

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
//                        entity.configure { it -= TweenSequenceComponent }
                        //println("INFO: TweenSequence ended and removed for entity '${entity.id}' (${world.nameOf(entity)}).")
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
                    world.entity("SpawnNewTweenSequence") {
                        it += tweenSequenceComponent {
                            // Populate the new TweenSequenceComponent with the tweens from the list of SpawnNewTweenSequence object
                            tweens.init(currentTween.tweens)
                            deleteEntity { target = it }  // Delete the TweenSequence entity after it has been executed
                        }
                    }
                }
                is Jump -> {
                    // Currently we just have a statical branch jump without conditional check
                    tweenSequence.index += currentTween.distance - 1
                }
// TODO check if loop would be possible otherwise delete
                is LoopTweens -> {
                    TODO("LoopTweens not implemented yet!")
//                    tweenSequence.loopStart = tweenSequence.index
                }
                is ParallelTweens -> {
                    currentTween.tweens.forEach { tween ->
                        if (tween.delay != null && tween.delay!! > 0f) {
                            // Tween has its own delay -> spawn a new TweenSequence for it
                            world.entity("ParallelTween: ${tween::class.simpleName} for entity '${tween.target.id}'") { it ->
                                it += tweenSequenceComponent {
                                    // Put the tween into a new TweenSequence which runs independently of the parent TweenSequence
                                    tweens.init(
                                        tween.also { tween ->
                                            if (tween.duration == null) tween.duration = currentTween.duration ?: 0f
                                            if (tween.easing == null) tween.easing = currentTween.easing ?: Easing.LINEAR
                                        }
                                    )
                                    deleteEntity { target = it }  // Delete the TweenSequence entity after it has been executed
                                }
                            }
                        } else {
                            // No delay -> run it directly
                            checkTween(entity, tween, currentTween)
                        }
                    }
                }
                // In case of "Wait"-Tween "waitTime = duration" was already set above
                else -> checkTween(entity, currentTween, defaultTweenValues)
            }
        }
        else tweenSequence.timeProgress += deltaTime
    }

    /**
     *
     * @param baseEntity The entity which owns this TweenSequence animation
     */
    private fun checkTween(baseEntity: Entity, tween: TweenBase, parentTween: ParallelTweens) {
        currentTween = tween
        currentParentTween = parentTween
        when (tween) {
            is TweenRgba -> tween.target.getOrWarning(RgbaComponent)?.let { start ->
                tween.alpha?.let { end -> createTweenPropertyComponent(RgbaAlpha, value = start.alpha, change = end - start.alpha) }
                tween.r?.let { end ->  createTweenPropertyComponent(RgbaRed, value = start.r, change = end - start.r) }
                tween.g?.let { end ->  createTweenPropertyComponent(RgbaGreen, value = start.g, change = end - start.g) }
                tween.b?.let { end ->  createTweenPropertyComponent(RgbaBlue, value = start.b, change = end - start.b) }
                tween.visible?.let { visible -> createTweenPropertyComponent(RgbaAlpha, value = if (visible) 1f else 0f, change = 0f) }
            }
            is TweenPosition -> tween.target.getOrWarning(PositionComponent)?.let { start ->
                tween.x?.let { end -> createTweenPropertyComponent(PositionX, start.x, end - start.x) }
                tween.y?.let { end -> createTweenPropertyComponent(PositionY, start.y, end - start.y) }
                tween.offsetX?.let { end -> createTweenPropertyComponent(PositionOffsetX, start.offsetX, end - start.offsetX) }
                tween.offsetY?.let { end -> createTweenPropertyComponent(PositionOffsetY, start.offsetY, end - start.offsetY) }
            }
            is TweenMotion -> tween.target.getOrWarning(MotionComponent)?.let { start ->
                tween.velocityX?.let { end -> createTweenPropertyComponent(MotionVelocityX, start.velocityX, end - start.velocityX) }
            }
            is TweenSprite -> tween.target.getOrWarning(SpriteComponent)?.let { _ ->  // make sure to-be-animated-entity is of type sprite
                tween.animation?.let { value -> createTweenPropertyComponent(SpriteAnimation, value) }
                tween.running?.let { value -> createTweenPropertyComponent(SpriteRunning, value) }
                tween.direction?.let { value -> createTweenPropertyComponent(SpriteDirection, value) }
                tween.destroyOnPlayingFinished?.let { value -> createTweenPropertyComponent(SpriteDestroyOnPlayingFinished, value) }
            }
            is TweenSwitchLayerVisibility -> tween.target.getOrWarning(SwitchLayerVisibilityComponent)?.let { start ->
                tween.offVariance?.let { end -> createTweenPropertyComponent(SwitchLayerVisibilityOffVariance, value = start.offVariance, change = end - start.offVariance) }
                tween.onVariance?.let { end -> createTweenPropertyComponent(SwitchLayerVisibilityOnVariance, start.onVariance, end - start.onVariance) }
            }
            is TweenSpawner -> tween.target.getOrWarning(SpawnerComponent)?.let { start ->
                tween.numberOfObjects?.let { end -> createTweenPropertyComponent(SpawnerNumberOfObjects, start.numberOfObjects, end - start.numberOfObjects) }
                tween.interval?.let { end -> createTweenPropertyComponent(SpawnerInterval, start.interval, end - start.interval) }
                tween.timeVariation?.let { end -> createTweenPropertyComponent(SpawnerTimeVariation, start.timeVariation, end - start.timeVariation) }
                tween.positionVariation?.let { end -> createTweenPropertyComponent(SpawnerPositionVariation, start.positionVariation, end - start.positionVariation) }
            }
            is TweenSound -> tween.target.getOrWarning(SoundComponent)?.let{ start ->
                tween.startTrigger?.let { value -> createTweenPropertyComponent(SoundStartTrigger, value) }
                tween.stopTrigger?.let { value -> createTweenPropertyComponent(SoundStopTrigger, value) }
                tween.position?.let { end -> createTweenPropertyComponent(SoundPosition, start.position, end - start.position) }
                tween.volume?.let { end -> createTweenPropertyComponent(SoundVolume, start.volume, end - start.volume) }
            }
            is TweenTextField -> tween.target.getOrWarning(TextFieldComponent)?.let { start ->
                tween.text?.let { value -> createTweenPropertyComponent(TextFieldText, value) }
                tween.textRangeStart?.let { end -> createTweenPropertyComponent(TextFieldTextRangeStart, start.textRangeStart, end - start.textRangeStart ) }
                tween.textRangeEnd?.let { end -> createTweenPropertyComponent(TextFieldTextRangeEnd, start.textRangeEnd, end - start.textRangeEnd ) }
            }
            is TweenTouchInput -> tween.target.getOrWarning(TouchInputComponent)?.let {
                tween.enabled?.let { value -> createTweenPropertyComponent(TouchInputEnable, value) }
            }
            // Creates a new entity (or uses the given entity from the tween) and configures it by running the config-function
            is SpawnEntity -> {
                val spawnedEntity = if (tween.target == Entity.NONE) world.entity("SpawnEntity: ${tween.entityConfig}") else tween.target
                EntityFactory.configure(tween.entityConfig, world, spawnedEntity)
            }
            // Directly deletes the given entity from the tween
// TODO - check when deleting 3 topmost clouds if we need to use this method?
            is DeleteEntity -> {
                println("INFO - TweenSequenceSystem: Deleting '${tween.target}' (name: ${world.nameOf(tween.target)}) via life cycle from base" +
                    "'$baseEntity' (name: ${world.nameOf(baseEntity)}).")

//                if (tween.target.id == -1) {
//                    Pool.writeStatistics()
//                }
                world.deleteViaLifeCycle(tween.target)
            }
//            is DeleteEntity -> {  // Use this to delete the entity directly without going through the life cycle - that will crash the tween system
//                world -= tween.target
//            }
            // Runs the config-function on the given entity from the tween
            is ExecuteConfigFunction -> EntityFactory.configure(tween.entityConfig, world, tween.target)
            is Wait -> tween.event?.let { event ->
                // Wait for event, SendEvent and ResetEvent need the current entity which comes in via onTickEntity
                currentTween.target = baseEntity
                createTweenPropertyComponent(EventSubscribe, value = event)
            }
            is SendEvent -> {
                currentTween.target = baseEntity
                createTweenPropertyComponent(EventPublish, value = tween.event)
            }
            is ResetEvent -> {
                currentTween.target = baseEntity
                createTweenPropertyComponent(EventReset, value = tween.event)
            }
            else -> {
                when (tween) {
                    is SpawnNewTweenSequence -> println("WARNING - TweenSequenceSystem: \"SpawnNewTweenSequence\" not allowed in ParallelTweens!")
                    is LoopTweens -> println("WARNING - TweenSequenceSystem: \"LoopTweens\" not allowed in ParallelTweens!")
                    is Jump -> println("WARNING - TweenSequenceSystem: \"Jump\" not allowed in ParallelTweens!")
                    else -> println("WARNING - TweenSequenceSystem: Tween function for tween '$tween' not implemented!")
                }
            }
        }
    }

    private fun createTweenPropertyComponent(componentProperty: TweenPropertyType, value: Any, change: Any = Unit) {
        currentTween.target.configure { animatedEntity ->
            animatedEntity.getOrAdd(componentProperty.type) {
                tweenPropertyComponent(componentProperty) {
                    this.change = change
                    this.value = value
                    this.duration = currentTween.duration ?: currentParentTween.duration ?: 0f
                    this.timeProgress = 0f
                    this.easing = currentTween.easing ?: currentParentTween.easing ?: Easing.LINEAR
                }
            }
        }
    }

    private inline fun <reified T : Component<*>> Entity.getOrWarning(componentType: ComponentType<T>) : T? {
        if (this has componentType) return get(componentType)
        else {
            println("WARNING - TweenSequenceSystem: Entity '${this.id}' (${world.nameOf(this)}) does not contain component type '${componentType}'!\n" +
                "Entity snapshot: \n${world.snapshotOf(this)}")
            return null
        }
    }
}
