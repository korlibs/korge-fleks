package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.WorldConfiguration
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.entity.blueprints.registerCommonEntityBlueprints
import korlibs.korge.fleks.state.GameStateManager
import korlibs.korge.fleks.state.PlayerInputState
import korlibs.korge.fleks.systems.SystemRuntimeConfigs
import korlibs.korge.fleks.systems.CameraSystem
import korlibs.korge.fleks.systems.DebugSystem
import korlibs.korge.fleks.systems.EntityLinkSystem
import korlibs.korge.fleks.systems.MessagePassingSystem
import korlibs.korge.fleks.systems.HealthMonitorSystem
import korlibs.korge.fleks.systems.WorldChunkSystem
import korlibs.korge.fleks.systems.LifeCycleSystem
import korlibs.korge.fleks.systems.ParallaxSystem
import korlibs.korge.fleks.systems.StaticMoveSystem
import korlibs.korge.fleks.systems.SnapshotSerializerSystem
import korlibs.korge.fleks.systems.SoundSystem
import korlibs.korge.fleks.systems.SpawnerSystem
import korlibs.korge.fleks.systems.SpriteVisibilitySystem
import korlibs.korge.fleks.systems.SpriteSystem
import korlibs.korge.fleks.systems.addTweenEngineSystems
import korlibs.korge.fleks.systems.collision.CollisionMoveSystem
import korlibs.korge.fleks.systems.collision.BehaviorSystem
import korlibs.korge.view.Views


fun WorldConfiguration.addKorgeFleksInjectables(
    assetStore: AssetStore,
    gameState: GameStateManager,
    inputState: PlayerInputState,
    views: Views
) {

    // Register external objects which are used by systems and in component and family hook functions
    injectables {
        add("AssetStore", assetStore)
        add("DebugPointPool", DebugPointPool())
        add("GameState", gameState)
        add("SystemRuntimeConfigs", SystemRuntimeConfigs())
        add("InputState", inputState)
        add("Views", views)
    }
}

fun WorldConfiguration.addKorgeFleksSystems() {
    // Register all needed systems of the entity component system
    // The order of systems here also define the order in which the systems are called inside Fleks ECS
    systems {
        // Spawn new entities if we enter a new level chunk
        add(WorldChunkSystem())

        // Debug system to move player entity to a specific position on the map overwriting player input data
        add(DebugSystem())

        add(SpawnerSystem())
        add(MessagePassingSystem())

        // Tween engine system
        addTweenEngineSystems()

        // Systems below depend on changes of above tween engine systems
        add(LifeCycleSystem())
        add(ParallaxSystem(worldToPixelRatio = AppConfig.WORLD_TO_PIXEL_RATIO))
        add(StaticMoveSystem())
        add(SpriteVisibilitySystem())
        add(EntityLinkSystem())
        add(SpriteSystem())

        // Behavior system needs to be called after all other systems which can change the state (components)
        // of an entity (with Behavior component)
        add(BehaviorSystem())  // --> set motionComponent of entity
        add(CollisionMoveSystem())  // --> modify motionComponent of entity in case of collision and set positionComponent of entity

        add(SoundSystem())
        add(CameraSystem(worldToPixelRatio = AppConfig.WORLD_TO_PIXEL_RATIO))

        add(SnapshotSerializerSystem())

        add(HealthMonitorSystem())
    }

    // Make sure we have all common entity configs registered which comes with Korge-fleks
    registerCommonEntityBlueprints()
}
