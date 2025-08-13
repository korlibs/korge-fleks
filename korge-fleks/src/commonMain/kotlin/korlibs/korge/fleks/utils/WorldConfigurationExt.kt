package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.WorldConfiguration
import korlibs.korge.fleks.gameState.GameStateManager
import korlibs.korge.fleks.systems.CameraSystem
import korlibs.korge.fleks.systems.DebugSystem
import korlibs.korge.fleks.systems.EntityLinkSystem
import korlibs.korge.fleks.systems.EventSystem
import korlibs.korge.fleks.systems.HealthMonitorSystem
import korlibs.korge.fleks.systems.LevelChunkSystem
import korlibs.korge.fleks.systems.LifeCycleSystem
import korlibs.korge.fleks.systems.ParallaxSystem
import korlibs.korge.fleks.systems.PositionSystem
import korlibs.korge.fleks.systems.SnapshotSerializerSystem
import korlibs.korge.fleks.systems.SoundSystem
import korlibs.korge.fleks.systems.SpawnerSystem
import korlibs.korge.fleks.systems.SpriteLayersSystem
import korlibs.korge.fleks.systems.SpriteSystem
import korlibs.korge.fleks.systems.TouchInputSystem
import korlibs.korge.fleks.systems.addTweenEngineSystems
import korlibs.korge.fleks.systems.collision.GridMoveSystem
import korlibs.korge.fleks.systems.collision.PlatformerGravitySystem
import korlibs.korge.fleks.systems.collision.PlatformerGroundSystem
import korlibs.korge.fleks.systems.collision.PlayerInputSystem
import korlibs.korge.fleks.systems.collision.PlayerMoveSystem


fun WorldConfiguration.addKorgeFleksInjectables() {

    // Register external objects which are used by systems and in component and family hook functions
    injectables {
        add("AssetStore", GameStateManager.assetStore)
        add("DebugPointPool", DebugPointPool())
    }
}

fun WorldConfiguration.addKorgeFleksSystems() {
    // Register all needed systems of the entity component system
    // The order of systems here also define the order in which the systems are called inside Fleks ECS
    systems {
        // Spawn new entities if we enter a new level chunk
        add(LevelChunkSystem())

        // Collision and player input systems
//        add(PlatformerGravitySystem())
//        add(PlatformerGroundSystem())  TODO check if we need this system - isGrounded is set in the GridMoveSystem
        val playerInputSystem = PlayerInputSystem()
        add(playerInputSystem)
        add(PlayerMoveSystem(playerInputSystem))
        add(GridMoveSystem())
//        add(GridCollisionCleanupSystem())  ??? check why this is needed

        add(TouchInputSystem())
        add(DebugSystem())
        add(SpawnerSystem())
        add(EventSystem())

        // Tween engine system
        addTweenEngineSystems()

        // Systems below depend on changes of above tween engine systems
        add(LifeCycleSystem())
        add(ParallaxSystem(worldToPixelRatio = AppConfig.WORLD_TO_PIXEL_RATIO))
        add(PositionSystem())
        add(SpriteLayersSystem())
        add(EntityLinkSystem())
        add(SpriteSystem())

        add(SoundSystem())
        add(CameraSystem(worldToPixelRatio = AppConfig.WORLD_TO_PIXEL_RATIO))

        add(SnapshotSerializerSystem())

        add(HealthMonitorSystem())
    }
}
