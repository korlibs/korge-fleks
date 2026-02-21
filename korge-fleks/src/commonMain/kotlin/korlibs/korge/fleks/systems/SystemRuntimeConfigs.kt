package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.components.WorldMap
import korlibs.korge.fleks.components.messagePassing.MessagePassingConfig

/**
 * This class holds runtime configuration data which can be used by various systems during the game loop.
 * We need to store those data in components of entities because of serialization/deserialization of the world state.
 * Also game state rewind/fast-forward needs those data to be part of the saved world state.
 *
 * Hint: This object acts as a Prefab container for entities which hold those runtime configs.
 */
class SystemRuntimeConfigs {
    var camera: Entity? = null
    var messagePassing: Entity? = null
    var worldChunk: Entity? = null

    fun getCameraPosition(world: World): Position? =
        if (camera != null) world.run { camera!![Position.PositionComponent] } else null

    fun getMessagePassingConfig(world: World): MessagePassingConfig? =
        if (messagePassing != null) world.run { messagePassing!![MessagePassingConfig.MessagePassingConfigComponent] } else null

    fun getWorldChunkConfig(world: World): WorldMap? =
        if (worldChunk != null) world.run { worldChunk!![WorldMap.WorldMapComponent] } else null
}