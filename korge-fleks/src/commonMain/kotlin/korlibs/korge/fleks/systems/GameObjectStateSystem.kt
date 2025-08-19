package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.EntityRefsByName.Companion.EntityRefsByNameComponent
import korlibs.korge.fleks.components.Sprite.Companion.SpriteComponent
import korlibs.korge.fleks.components.State.Companion.StateComponent
import korlibs.korge.fleks.utils.nameOf


class GameObjectStateSystem : IteratingSystem(
    family = World.family {
        all(StateComponent)
            .any(SpriteComponent, EntityRefsByNameComponent)
    },
    interval = Fixed(1 / 60f)
) {
    private val assetStore = world.inject<AssetStore>("AssetStore")

    override fun onTickEntity(entity: Entity) {
        val stateComponent = entity[StateComponent]
        val stateConfig = assetStore.getGameObjectStateConfig(stateComponent.name)

        entity.getOrNull(EntityRefsByNameComponent)?.let { entityRefsByNameComponent ->
            stateConfig.states[stateComponent.current]?.let { state ->
                // Set the sprite animation based on the current state
                state.entities.forEach { (entityName, config) ->
                    val subEntity = entityRefsByNameComponent.getSubEntity(entityName)
                    subEntity[SpriteComponent].animation = config.animationName
                }
            } ?: run {
                println("ERROR: GameObjectStateSystem - No state config found for state ${stateComponent.current} in entity '${world.nameOf(entity)}'!")
            }
        }
    }
}