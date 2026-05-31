package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.WorldChunk
import korlibs.korge.fleks.components.WorldChunk.Companion.WorldChunkComponent
import korlibs.korge.fleks.components.WorldMap
import korlibs.korge.fleks.components.WorldMap.Companion.WorldMapComponent
import korlibs.korge.fleks.components.messagePassing.MessagePassingConfig
import korlibs.korge.fleks.components.messagePassing.MessagePassingConfig.Companion.MessagePassingConfigComponent

/**
 * This class holds runtime configuration data which can be used by various systems during the game loop.
 * We need to store those data in components of entities because of serialization/deserialization of the world state.
 * Also game state rewind/fast-forward needs those data to be part of the saved world state.
 *
 * Hint: This object acts as a Prefab container for entities which hold those runtime configs.
 */
class SystemRuntimeConfigs {
    var cameraEntity: Entity? = null
    var messagePassingEntity: Entity? = null
    var worldMapEntity: Entity? = null

    /**
     * Get the current camera position from the world. Returns null if the camera entity or the position component does not exist.
     */
    fun getCameraPositionComponent(world: World): Position? =
        if (cameraEntity != null) world.run { cameraEntity!![PositionComponent] } else null

    /**
     * Get the current chunk from the world where the camera is located. Returns null if the camera entity or the
     * world chunk component does not exist.
     */
    fun getCameraWorldChunkComponent(world: World): WorldChunk? =
        if (cameraEntity != null) world.run { cameraEntity!![WorldChunkComponent] } else null

    /**
     * Get the message passing config from the world. Returns null if the message passing entity or the message passing
     * config component does not exist.
     */
    fun getMessagePassingConfig(world: World): MessagePassingConfig? =
        if (messagePassingEntity != null) world.run { messagePassingEntity!![MessagePassingConfigComponent] } else null

    fun getWorldMapConfig(world: World): WorldMap? =
        if (worldMapEntity != null) world.run { worldMapEntity!![WorldMapComponent] } else null
}