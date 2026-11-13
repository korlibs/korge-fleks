package korlibs.korge.fleks.systems


import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.Position.Companion.positionComponent
import korlibs.korge.fleks.components.Size.Companion.sizeComponent
import korlibs.korge.fleks.components.TweenSequence.Companion.tweenSequenceComponent
import korlibs.korge.fleks.components.data.tweenSequence.DeleteEntity.Companion.deleteEntity
import korlibs.korge.fleks.components.data.tweenSequence.ParallelTweens.Companion.parallelTweens
import korlibs.korge.fleks.components.data.tweenSequence.SpawnEntity.Companion.spawnEntity
import korlibs.korge.fleks.components.data.tweenSequence.TweenPosition.Companion.tweenPosition
import korlibs.korge.fleks.components.data.tweenSequence.Wait.Companion.wait
import korlibs.korge.fleks.entity.*
import korlibs.math.interpolation.*
import kotlinx.serialization.*

@Serializable @SerialName("TestGameEntityBlueprint")
data class TestGameEntityBlueprint(
    override val name: String
) : EntityBlueprint {

    // Function for adding components to this entity
    override fun World.entityConfigure(entity: Entity) : Entity {

        entity.configure {
            it += positionComponent {
                x = 100.0
                y = 200.0
            }

            it += tweenSequenceComponent {
                parallelTweens {
                    tweenPosition { target = it; x = 5000.0; delay = 1.0; duration = 60.0; easing = Easing.EASE_IN }
                }
                wait { duration = 0.5 }
                spawnEntity { entityBlueprint = "test_game_object_Blueprint" }
                wait { duration = 60.0 }
                deleteEntity { target = it }
            }

        }
        return entity
    }

    init {
        // Register entity config into entity factory for lookup by its name
        EntityFactory.register(this)

        GameObjectBlueprint(name = "test_game_object_Blueprint")
    }
}

@Serializable @SerialName("GameObjectBlueprint")
data class GameObjectBlueprint(
    override val name: String
) : EntityBlueprint {

    // Function for adding components to this entity
    override fun World.entityConfigure(entity: Entity) : Entity {

        entity.configure {
            it += positionComponent {
                x = 100.0
                y = 200.0
            }

            it += sizeComponent {
                width = 100.0
                height = 100.0
            }

            it += tweenSequenceComponent {
                tweenPosition { target = it; x = 500.0; y = 1000.0; duration = 50.0; easing = Easing.EASE_OUT }
                wait { duration = 1.0 }
                deleteEntity { target = it }
            }
        }
        return entity
    }

    init {
        // Register entity config into entity factory for lookup by its name
        EntityFactory.register(this)
    }
}
