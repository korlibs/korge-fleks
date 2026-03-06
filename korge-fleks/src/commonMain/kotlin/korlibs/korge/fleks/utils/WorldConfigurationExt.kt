package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.WorldConfiguration
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.entity.config.registerCommonEntityConfigs
import korlibs.korge.fleks.state.GameStateManager
import korlibs.korge.fleks.state.PlayerInputState
import korlibs.korge.fleks.systems.SystemRuntimeConfigs
import korlibs.korge.fleks.systems.CameraSystem
import korlibs.korge.fleks.systems.DebugSystem
import korlibs.korge.fleks.systems.EntityLinkSystem
import korlibs.korge.fleks.systems.MessagePassingSystem
import korlibs.korge.fleks.systems.GameObjectStateSystem
import korlibs.korge.fleks.systems.HealthMonitorSystem
import korlibs.korge.fleks.systems.WorldChunkSystem
import korlibs.korge.fleks.systems.LifeCycleSystem
import korlibs.korge.fleks.systems.ParallaxSystem
import korlibs.korge.fleks.systems.PositionSystem
import korlibs.korge.fleks.systems.SnapshotSerializerSystem
import korlibs.korge.fleks.systems.SoundSystem
import korlibs.korge.fleks.systems.SpawnerSystem
import korlibs.korge.fleks.systems.SpriteVisibilitySystem
import korlibs.korge.fleks.systems.SpriteSystem
import korlibs.korge.fleks.systems.TouchInputSystem
import korlibs.korge.fleks.systems.addTweenEngineSystems
import korlibs.korge.fleks.systems.collision.GridMoveSystem
import korlibs.korge.fleks.systems.collision.PlayerMoveAfterCollisionSystem
import korlibs.korge.fleks.systems.collision.PlayerMoveSystem


fun WorldConfiguration.addKorgeFleksInjectables(
    assetStore: AssetStore,
    gameState: GameStateManager,
    inputState: PlayerInputState
) {

    // Register external objects which are used by systems and in component and family hook functions
    injectables {
        add("AssetStore", assetStore)
        add("DebugPointPool", DebugPointPool())
        add("GameState", gameState)
        add("SystemRuntimeConfigs", SystemRuntimeConfigs())
        add("InputState", inputState)
    }
}

fun WorldConfiguration.addKorgeFleksSystems() {
    // Register all needed systems of the entity component system
    // The order of systems here also define the order in which the systems are called inside Fleks ECS
    systems {
        // Spawn new entities if we enter a new level chunk
        add(WorldChunkSystem())

        // Collision and player input systems
//        add(PlatformerGravitySystem())
//        add(PlatformerGroundSystem())  TODO check if we need this system - isGrounded is set in the GridMoveSystem

        add(PlayerMoveSystem())
        add(GridMoveSystem())
        add(PlayerMoveAfterCollisionSystem())
        // Debug system to move player entity to a specific position on the map overwriting player input data
        add(DebugSystem())

//        add(GridCollisionCleanupSystem())  ??? check why this is needed
        add(GameObjectStateSystem())

        add(TouchInputSystem())
        add(SpawnerSystem())
        add(MessagePassingSystem())

        // Tween engine system
        addTweenEngineSystems()

        // Systems below depend on changes of above tween engine systems
        add(LifeCycleSystem())
        add(ParallaxSystem(worldToPixelRatio = AppConfig.WORLD_TO_PIXEL_RATIO))
        add(PositionSystem())
        add(SpriteVisibilitySystem())
        add(EntityLinkSystem())
        add(SpriteSystem())

        add(SoundSystem())
        add(CameraSystem(worldToPixelRatio = AppConfig.WORLD_TO_PIXEL_RATIO))

        add(SnapshotSerializerSystem())

        add(HealthMonitorSystem())
    }

    // Make sure we have all common entity configs registered which comes with Korge-fleks
    registerCommonEntityConfigs()
}
