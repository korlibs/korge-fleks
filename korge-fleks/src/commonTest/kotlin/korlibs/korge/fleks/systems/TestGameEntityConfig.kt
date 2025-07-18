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
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.*

@Serializable @SerialName("TestGameEntityConfig")
data class TestGameEntityConfig(
    override val name: String
) : EntityConfig {

    // Function for adding components to this entity
    override fun World.entityConfigure(entity: Entity) : Entity {

        entity.configure {
            it += positionComponent {
                x = 100f
                y = 200f
            }

            it += tweenSequenceComponent {
                parallelTweens {
                    tweenPosition { target = it; x = 5000f; delay = 1f; duration = 60f; easing = Easing.EASE_IN }
                }
                wait { duration = 0.5f }
                spawnEntity { entityConfig = "test_game_object_config" }
                wait { duration = 60f }
                deleteEntity { target = it }
            }

        }
        return entity
    }

    init {
        // Register entity config into entity factory for lookup by its name
        EntityFactory.register(this)

        GameObjectConfig(name = "test_game_object_config")
    }
}

@Serializable @SerialName("GameObjectConfig")
data class GameObjectConfig(
    override val name: String
) : EntityConfig {

    // Function for adding components to this entity
    override fun World.entityConfigure(entity: Entity) : Entity {

        entity.configure {
            it += positionComponent {
                x = 100f
                y = 200f
            }
            it += sizeComponent {
                width = 100f
                height = 100f
            }

            it += tweenSequenceComponent {
                tweenPosition { target = it; x = 500f; y = 1000f; duration = 50f; easing = Easing.EASE_OUT }
                wait { duration = 1f }
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
